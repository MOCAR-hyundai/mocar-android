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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val uid: String? get() = auth.currentUser?.uid

    private val favoritesFlow: Flow<Set<String>> =
        uid?.let { repo.myFavoriteListingIds(it) } ?: flowOf(emptySet())

    private val listingsFlow = repo.listingsOnSale()

    private val brandsFlow: Flow<List<BrandUi>> =
        repo.brands()
            .map { list -> list.map { it.toUi() } }
            .catch { emit(emptyList()) }

    val uiState: StateFlow<HomeUiState> =
        combine(listingsFlow, favoritesFlow, brandsFlow) { listings, favIds, brandUis ->
            val cars = listings.map { dto ->
                dto.toCar(isFavorite = favIds.contains(dto.listingId))
            }
            HomeUiState(
                cars = cars,
                brands = brandUis,
                loading = false
            )
        }
            .catch { e -> emit(HomeUiState(error = e.message, loading = false)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleFavorite(listingId: String) {
        val u = uid ?: return
        viewModelScope.launch { repo.toggleFavorite(u, listingId) }
    }
}