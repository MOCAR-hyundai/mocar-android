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

    // 선택된 브랜드 ID
    private val _selectedBrandId = MutableStateFlow<String?>(null)
    fun selectBrand(id: String?) { _selectedBrandId.value = id }

    // 즐겨찾기 목록
    val favoritesFlow: Flow<Set<String>> =
        uid?.let { repo.myFavoriteListingIds(it) } ?: flowOf(emptySet())

    // 브랜드 목록
    val brandsFlow: Flow<List<BrandUi>> =
        repo.brands()
            .map { it.map { dto -> dto.toUi() } }
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

    fun toggleFavorite(listingId: String) {
        val u = uid ?: return
        viewModelScope.launch { repo.toggleFavorite(u, listingId) }
    }
}