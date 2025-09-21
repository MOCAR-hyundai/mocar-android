package com.autoever.mocar.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.autoever.mocar.data.listings.ListingDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.text.contains

data class SearchFilterState(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..10000f,
    val yearRange: ClosedFloatingPointRange<Float> = 2006f..2025f,
    val mileageRange: ClosedFloatingPointRange<Float> = 0f..200000f,
    val selectedTypes: List<String> = emptyList(),
    val selectedFuels: List<String> = emptyList(),
    val selectedRegions: List<String> = emptyList()
)

// 화면 로딩
data class ListingUiState(
    val isLoading: Boolean = true,
    val listings: List<ListingDto> = emptyList()
)


class SearchSharedViewModel : ViewModel() {

    var isTransitionLoading by mutableStateOf(false)
    var selectedBrand by mutableStateOf<String?>(null)
    var selectedModel by mutableStateOf<String?>(null)
    var selectedSubModels = mutableStateListOf<String>()

    fun clearAll() {
        selectedBrand = null
        selectedModel = null
        selectedSubModels.clear()
    }
}

class SearchFilterViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState

    // 가격, 연식, 주행거리 업데이트
    fun updatePrice(range: ClosedFloatingPointRange<Float>) {
        _filterState.update { it.copy(priceRange = range) }
    }

    fun updateYear(range: ClosedFloatingPointRange<Float>) {
        _filterState.update { it.copy(yearRange = range) }
    }

    fun updateMileage(range: ClosedFloatingPointRange<Float>) {
        _filterState.update { it.copy(mileageRange = range) }
    }

    // 체크박스 목록들 업데이트
    fun toggleType(type: String) {
        _filterState.update {
            val updated = if (type in it.selectedTypes) it.selectedTypes - type else it.selectedTypes + type
            it.copy(selectedTypes = updated)
        }
    }

    fun toggleFuel(fuel: String) {
        _filterState.update {
            val updated = if (fuel in it.selectedFuels) it.selectedFuels - fuel else it.selectedFuels + fuel
            it.copy(selectedFuels = updated)
        }
    }

    fun toggleRegion(region: String) {
        _filterState.update {
            val updated = if (region in it.selectedRegions) it.selectedRegions - region else it.selectedRegions + region
            it.copy(selectedRegions = updated)
        }
    }

    fun clearAll() {
        _filterState.value = SearchFilterState()
    }
}


class ListingViewModel : ViewModel() {
    private var hasFetched = false
    private val db = Firebase.firestore
    private val _listings = MutableStateFlow<List<ListingDto>>(emptyList())
    val listings: StateFlow<List<ListingDto>> = _listings

    private val _uiState = MutableStateFlow(ListingUiState())
    val uiState: StateFlow<ListingUiState> = _uiState


    init {
        fetchListings()
    }

    private fun fetchListings() {
        if (hasFetched) return
        hasFetched = true

        _uiState.update { it.copy(isLoading = true) }
        db.collection("listings").get()
            .addOnSuccessListener { result ->
                 Log.d("Firestore", "📦 Fetched documents: ${result.size()}")

                val parsed = result.mapNotNull { doc ->
                    val brand = doc.getString("brand")
                    val model = doc.getString("model")
                    val fuel = doc.getString("fuel")
                    val carType = doc.getString("carType")
                    val region = doc.getString("region")
                    val imageList = doc.get("images") as? List<String> ?: emptyList()

                    val title = doc.getString("title") ?: ""
                    val year = doc.getLong("year")?.toInt() ?: 0

                    if (brand == null || model == null || fuel == null || region == null) {
                        Log.w("Firestore", "⚠️ Skipped doc ${doc.id} due to null required fields")
                        return@mapNotNull null
                    }

                    try {
                        doc.toObject(ListingDto::class.java)
                    } catch (e: Exception) {
                        Log.w("Firestore", "⚠️ Failed to parse doc ${doc.id}", e)
                        null
                    }
                }

                _listings.value = parsed
                _uiState.value = ListingUiState(
                    isLoading = false,
                    listings = parsed
                )

                Log.d("Firestore", "Parsed listings: ${parsed.size}")
                _listings.value = parsed
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch listings", e)
            }
    }

}
