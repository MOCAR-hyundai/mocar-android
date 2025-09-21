package com.autoever.mocar.viewmodel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.domain.model.Seller
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.autoever.mocar.ui.detail.CarDetailScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CarDetailUiState(
    val car: Car? = null,
    val seller:Seller? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class CarDetailViewModel(
    private val listingId: String,
    private val repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val uid = auth.currentUser?.uid

    private val favs: Flow<Set<String>> =
        uid?.let { repo.myFavoriteListingIds(it) } ?: flowOf(emptySet())

    private val listing = repo.listingById(listingId)

    private val sellerFlow: Flow<Seller?> =
        listing.flatMapLatest { dto ->
            val sellerId = dto?.sellerId ?: return@flatMapLatest flowOf(null)
            callbackFlow<Seller?> {
                val reg = db.collection("users").document(sellerId)
                    .addSnapshotListener { snap, _ ->
                        if (snap != null && snap.exists()) {
                            trySend(
                                Seller(
                                    id = snap.id,
                                    name = snap.getString("name") ?: "",
                                    photoUrl = snap.getString("photoUrl") ?: "",
                                    rating = snap.getDouble("rating") ?: 0.0,
                                    ratingCount = (snap.getLong("ratingCount") ?: 0).toInt()
                                )
                            )
                        } else {
                            trySend(null)
                        }
                    }
                awaitClose { reg.remove() }
            }
        }

    val uiState: StateFlow<CarDetailUiState> =
        combine(listing, favs, sellerFlow) { dto, favIds, seller ->
            if (dto == null) {
                CarDetailUiState(car = null, seller = null, loading = false)
            } else {
                CarDetailUiState(
                    car = dto.toCar(isFavorite = favIds.contains(dto.listingId)),
                    seller = seller,
                    loading = false
                )
            }
        }
            .catch { e -> emit(CarDetailUiState(error = e.message, loading = false)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CarDetailUiState())

    fun toggleFavorite() {
        val u = uid ?: return
        viewModelScope.launch { repo.toggleFavorite(u, listingId) }
    }
}

@Composable
fun CarDetailRoute(
    carId: String,
    onBack: () -> Unit
) {
    // 간단한 factory
    val vm:CarDetailViewModel = viewModel(
        key = "detail-$carId",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CarDetailViewModel(carId) as T
            }
        }
    )

    val state by vm.uiState.collectAsState()
    val car = state.car
    val seller = state.seller

    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("로딩 중…") }
        state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("오류: ${state.error}") }
        car == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("차량을 찾을 수 없어요.") }
        else -> CarDetailScreen(
            car = car,
            seller = seller,
            onBack = onBack,
            onToggleFavorite = { vm.toggleFavorite() }
        )
    }
}
