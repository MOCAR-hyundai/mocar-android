package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.brands.toUi
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.autoever.mocar.ui.common.component.atoms.BrandUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val cars: List<Car> = emptyList(),
    val brands: List<BrandUi> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val repo: MocarRepository = FirebaseMocarRepository(
        FirebaseFirestore.getInstance()
    ),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    /** Auth 상태를 Flow로 (로그인/로그아웃/계정전환 대응) */
    private val userIdFlow: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fb -> trySend(fb.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.uid) // 초기값
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    /** 내 즐겨찾기 listingId 집합 */
    private val favoritesFlow: Flow<Set<String>> =
        userIdFlow.flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptySet()) else repo.myFavoriteListingIds(uid)
        }.catch { emit(emptySet()) }

    /** UI에서 바로 구독할 수 있도록 StateFlow로 공개 */
    val favoriteIds: StateFlow<Set<String>> =
        favoritesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** 3) 매물/브랜드 */
    private val listingsFlow = repo.listingsOnSale()
    private val brandsFlow: Flow<List<BrandUi>> =
        repo.brands()
            .map { it.map { b -> b.toUi() } }
            .catch { emit(emptyList()) }

    /** 화면 상태 */
    val uiState: StateFlow<HomeUiState> =
        combine(listingsFlow, brandsFlow) { listings, brandUis ->
            HomeUiState(
                cars = listings.map { it.toCar() },
                brands = brandUis,
                loading = false
            )
        }
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