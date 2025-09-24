package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.autoever.mocar.data.brands.toUi
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.autoever.mocar.ui.common.component.atoms.BrandUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val uid: String? get() = auth.currentUser?.uid

    /** Auth 상태를 Flow로 (로그인/로그아웃/계정전환 대응) */
    private val userIdFlow: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fb -> trySend(fb.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.uid) // 초기값
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    // 선택된 브랜드 ID
    private val _selectedBrandId = MutableStateFlow<String?>(null)
    fun selectBrand(id: String?) { _selectedBrandId.value = id }

    // 즐겨찾기 목록
    val favoritesFlow: Flow<Set<String>> =
        uid?.let { repo.myFavoriteListingIds(it) } ?: flowOf(emptySet())
    /** 내 즐겨찾기 listingId 집합 */
    private val favoritesFlow: Flow<Set<String>> =
        userIdFlow.flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptySet()) else repo.myFavoriteListingIds(uid)
        }.catch { emit(emptySet()) }

    // 브랜드 목록
    val brandsFlow: Flow<List<BrandUi>> =
    /** UI에서 바로 구독할 수 있도록 StateFlow로 공개 */
    val favoriteIds: StateFlow<Set<String>> =
        favoritesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** 3) 매물/브랜드 */
    private val listingsFlow = repo.listingsOnSale()
    private val brandsFlow: Flow<List<BrandUi>> =
        repo.brands()
            .map { it.map { dto -> dto.toUi() } }
            .map { it.map { b -> b.toUi() } }
            .catch { emit(emptyList()) }

    // id → name 매핑 활용해서 brandName 추출
    private val selectedBrandName: Flow<String?> =
        _selectedBrandId.map { id -> id?.let { brandsMap[it] } }

    // 브랜드 매핑
    private val brandsMap = mutableMapOf<String, String>()
    init {
        viewModelScope.launch {
            repo.brands().collect { list ->
                brandsMap.clear()
                list.forEach { dto -> brandsMap[dto.id] = dto.name }
            }
    /** 화면 상태 */
    val uiState: StateFlow<HomeUiState> =
        combine(listingsFlow, brandsFlow) { listings, brandUis ->
            HomeUiState(
                cars = listings.map { it.toCar() },
                brands = brandUis,
                loading = false
            )
        }
    }

    // 페이징된 Car 흐름 (브랜드 변경 시 서버 필터링)
    @OptIn(ExperimentalCoroutinesApi::class)
    val carPagingFlow: Flow<PagingData<Car>> =
        _selectedBrandId
            .flatMapLatest { brandId ->
                repo.listingsOnSalePagedByBrand(20, brandId)
                    .map { paging -> paging.map { dto -> dto.toCar(isFavorite = false) } }
            }
            .cachedIn(viewModelScope)
            .catch { e -> emit(HomeUiState(error = e.message ?: "Unknown error", loading = false)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** 5) 토글 (미로그인 시 무시) */
    fun toggleFavorite(listingId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repo.toggleFavorite(uid, listingId)  // 레포에서 `${uid}_${listingId}` 문서 생성/삭제 + createdAt(String) 저장
            } catch (t: Throwable) {
                // TODO: 스낵바/에러 상태 처리
            }
        }
    }
}