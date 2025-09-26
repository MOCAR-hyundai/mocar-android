package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.brands.toUi
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.autoever.mocar.ui.common.component.atoms.BrandUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val cars: List<Car> = emptyList(),
    val brands: List<BrandUi> = emptyList(),
    val loading: Boolean = false,
    val pageLoading: Boolean = false,
    val endReached: Boolean = false,
    val selectedBrand: String? = null,
    val error: String? = null
)

class HomeViewModel(
    private val repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // ---------- 좋아요 ----------
    private val userIdFlow: Flow<String?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(l)
        trySend(auth.currentUser?.uid)
        awaitClose { auth.removeAuthStateListener(l) }
    }.distinctUntilChanged()

    private val favoritesFlow: Flow<Set<String>> =
        userIdFlow.flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptySet()) else repo.myFavoriteListingIds(uid)
        }.catch { emit(emptySet()) }

    val favoriteIds: StateFlow<Set<String>> =
        favoritesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    // ---------- 찜 차량 별도 조회 ----------
    private val _favoriteCars = MutableStateFlow<List<Car>>(emptyList())
    val favoriteCars: StateFlow<List<Car>> = _favoriteCars

    init {
        viewModelScope.launch {
            favoriteIds.collect { ids ->
                if (ids.isEmpty()) {
                    _favoriteCars.value = emptyList()
                } else {
                    runCatching {
                        val docs = repo.fetchListingsByIds(ids.take(10).toList()) // whereIn 10개 제한
                        docs.mapNotNull { it.toObject(ListingDto::class.java)?.toCar() }
                    }.onSuccess { cars -> _favoriteCars.value = cars }
                        .onFailure { _favoriteCars.value = emptyList() }
                }
            }
        }
    }

    // ---------- 브랜드 목록 ----------
    private val brandsFlow: StateFlow<List<BrandUi>> =
        repo.brands()
            .map { it.map { b -> b.toUi() } }
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------- 페이징 ----------
    private val pageSize = 20
    private var lastDoc: DocumentSnapshot? = null
    private var currentBrand: String? = null
    private var endReached: Boolean = false
    private val carsCache = mutableListOf<Car>()
    private var pendingReset: Boolean = false

    private val _ui = MutableStateFlow(HomeUiState(loading = true, brands = emptyList()))
    val uiState: StateFlow<HomeUiState> =
        combine(_ui, brandsFlow) { state, brands -> state.copy(brands = brands) }
            .conflate()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(loading = true))

    init {
        // 초기 1회 로드
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            fetchPage(reset = true, brand = null)
        }
    }

    // ---------- 공개 API ----------
    fun refresh() = viewModelScope.launch { fetchPage(reset = true, brand = currentBrand) }

    fun selectBrand(brandName: String?) {
        if (currentBrand == brandName) return
        currentBrand = brandName
        viewModelScope.launch { fetchPage(reset = true, brand = brandName) }
    }

    fun loadNextPage() = viewModelScope.launch {
        if (endReached || _ui.value.pageLoading || _ui.value.loading) return@launch
        fetchPage(reset = false, brand = currentBrand)
    }

    fun toggleFavorite(listingId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                repo.toggleFavorite(uid, listingId)
            }.onFailure { e ->
                _ui.update { state ->
                    state.copy(error = e.message ?: "즐겨찾기 실패")
                }
            }
        }
    }

    // ---------- 내부: 페이지 페치 ----------
    private suspend fun fetchPage(reset: Boolean, brand: String?) {
        if (reset) {
            // ❌ 기존처럼 carsCache.clear(), loading=true 하지 않음
            lastDoc = null
            endReached = false
            pendingReset = true            // ✅ 최초 페이지 도착 시점에만 갈아끼우기
            _ui.update { it.copy(
                // 리스트는 유지하고 상단에만 로딩 표시
                loading = false,
                pageLoading = true,
                endReached = false,
                selectedBrand = brand,
                error = null
            ) }
        } else {
            _ui.update { it.copy(pageLoading = true, error = null) }
        }

        val result = runCatching {
            repo.fetchListingsPage(
                limit = pageSize,
                startAfter = lastDoc,
                brandEquals = brand,
                orderByField = "createdAt",
                descending = true
            )
        }

        result.onSuccess { (docs, newLast) ->
            val fetched = docs.mapNotNull { it.toObject(ListingDto::class.java)?.toCar() }
            if (pendingReset) {
                // ✅ 첫 페이지가 도착한 이 타이밍에만 갈아끼우기(흰 화면 X)
                carsCache.clear()
                pendingReset = false
            }
            carsCache += fetched

            val reached = docs.size < pageSize
            endReached = reached
            lastDoc = newLast

            _ui.update {
                it.copy(
                    cars = carsCache.toList(),
                    loading = false,
                    pageLoading = false,
                    endReached = reached,
                    selectedBrand = brand
                )
            }
        }.onFailure { e ->
            _ui.update {
                it.copy(
                    loading = false,
                    pageLoading = false,
                    error = e.message ?: "불러오기 실패"
                )
            }
        }
    }
}