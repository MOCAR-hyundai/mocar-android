package com.autoever.mocar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SearchFilterState(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..10000f,
    val yearRange: ClosedFloatingPointRange<Float> = 2006f..2025f,
    val mileageRange: ClosedFloatingPointRange<Float> = 0f..200000f,
    val selectedTypes: List<String> = emptyList(),
    val selectedFuels: List<String> = emptyList(),
    val selectedRegions: List<String> = emptyList()
)

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

    // 초기화
    fun resetAllFilters() {
        _filterState.value = SearchFilterState()
    }
}

data class ListingData(
    val brand: String = "",
    val images: List<String> = emptyList(),
    val model: String = "",
    val fuel: String = "",
    val carType: String? = null, // ← 여기를 nullable로
    val region: String = "",
)


class ListingViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _listings = MutableStateFlow<List<ListingData>>(emptyList())
    val listings: StateFlow<List<ListingData>> = _listings

    init {
        fetchListings()
    }

    private fun fetchListings() {
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

                    // 로그로 각 필드 출력
                    Log.d("Firestore", "🧩 doc: brand=$brand, model=$model, fuel=$fuel, carType=$carType, region=$region")

                    if (brand == null || model == null || fuel == null || region == null) {
                        Log.w("Firestore", "⚠️ Skipped doc ${doc.id} due to null required fields")
                        return@mapNotNull null
                    }

                    ListingData(
                        brand = brand,
                        images = imageList,
                        model = model,
                        fuel = fuel,
                        carType = carType,
                        region = region
                    )
                }

                Log.d("Firestore", "✅ Parsed listings: ${parsed.size}")
                _listings.value = parsed
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Failed to fetch listings", e)
            }

    }

}
