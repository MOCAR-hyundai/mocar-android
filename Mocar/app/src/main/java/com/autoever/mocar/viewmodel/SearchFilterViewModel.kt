package com.autoever.mocar.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.listings.ListingDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.contains

data class SearchFilterState(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..10000f,
    val yearRange: ClosedFloatingPointRange<Float> = 2006f..2025f,
    val mileageRange: ClosedFloatingPointRange<Float> = 0f..300000f,
    val selectedTypes: List<String> = emptyList(),
    val selectedFuels: List<String> = emptyList(),
    val selectedRegions: List<String> = emptyList()
)

// 화면 로딩
data class ListingUiState(
    val isLoading: Boolean = true,
    val listings: List<ListingDto> = emptyList()
)

// 검색기록
data class SearchRecordItem(
    val brand: String?,
    val model: String?,
    val subModels: List<String>,
    val priceRange: ClosedFloatingPointRange<Float>,
    val yearRange: ClosedFloatingPointRange<Float>,
    val mileageRange: ClosedFloatingPointRange<Float>,
    val selectedTypes: List<String>,
    val selectedFuels: List<String>,
    val selectedRegions: List<String>
)

// 필터 > 상세정보
data class FilterParams(
    val brand: String?,
    val model: String?,
    val subModels: List<String>,
    val minPrice: Float,
    val maxPrice: Float,
    val minYear: Float,
    val maxYear: Float,
    val minMileage: Float,
    val maxMileage: Float,
    val types: List<String>,
    val fuels: List<String>,
    val regions: List<String>
)

class SearchManufacturerViewModel : ViewModel() {

    var isTransitionLoading by mutableStateOf(false)
    var selectedBrand by mutableStateOf<String?>(null)
    var selectedModel by mutableStateOf<String?>(null)
    var selectedSubModels = mutableStateListOf<String>()

    fun clearAll() {
        selectedBrand = null
        selectedModel = null
        selectedSubModels.clear()
    }
    fun restoreFrom(item: SearchRecordItem) {
        selectedBrand = item.brand
        selectedModel = item.model
        selectedSubModels.clear()
        selectedSubModels.addAll(item.subModels)
    }

}

class SearchFilterViewModel(application: Application) : AndroidViewModel(application) {
    // 필터링
    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState

    // 필터링 검색 결과 기록
    private val prefs = application.getSharedPreferences("filter_prefs", Context.MODE_PRIVATE)
    private val _filterHistory = MutableStateFlow<List<SearchRecordItem>>(loadHistoryFromPrefs())
    val filterHistory: StateFlow<List<SearchRecordItem>> = _filterHistory

    var filterParams by mutableStateOf<FilterParams?>(null)
        private set

    fun setFilterParamsFromCurrentState(
        brand: String?,
        model: String?,
        subModels: List<String>,
        filterState: SearchFilterState
    ) {
        filterParams = FilterParams(
            brand = brand,
            model = model,
            subModels = subModels,
            minPrice = filterState.priceRange.start,
            maxPrice = filterState.priceRange.endInclusive,
            minYear = filterState.yearRange.start,
            maxYear = filterState.yearRange.endInclusive,
            minMileage = filterState.mileageRange.start,
            maxMileage = filterState.mileageRange.endInclusive,
            types = filterState.selectedTypes,
            fuels = filterState.selectedFuels,
            regions = filterState.selectedRegions
        )
        println(filterParams)
    }

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
    fun clearAll() {
        _filterState.value = SearchFilterState()
    }

    // 최근 필터링 검색 기록
    fun saveSearchRecord(item: SearchRecordItem) {
        val updated = (listOf(item) + _filterHistory.value).distinct().take(10)
        _filterHistory.value = updated
        saveHistoryToPrefs(updated)
    }

    fun serializeRecord(item: SearchRecordItem): String {
        return buildString {
            append("brand=${item.brand ?: ""};")
            append("model=${item.model ?: ""};")
            append("subModels=${item.subModels.joinToString(",")};")
            append("price=${item.priceRange.start},${item.priceRange.endInclusive};")
            append("year=${item.yearRange.start},${item.yearRange.endInclusive};")
            append("mileage=${item.mileageRange.start},${item.mileageRange.endInclusive};")
            append("types=${item.selectedTypes.joinToString(",")};")
            append("fuels=${item.selectedFuels.joinToString(",")};")
            append("regions=${item.selectedRegions.joinToString(",")}")
        }
    }

    fun deserializeRecord(raw: String): SearchRecordItem? {
        try {
            val map = raw.split(";")
                .map { it.split("=", limit = 2) }
                .associate { it[0] to it.getOrElse(1) { "" } }

            return SearchRecordItem(
                brand = map["brand"]?.ifBlank { null },
                model = map["model"]?.ifBlank { null },
                subModels = map["subModels"]?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                priceRange = map["price"]?.split(",")?.let { it[0].toFloat()..it[1].toFloat() } ?: (0f..10000f),
                yearRange = map["year"]?.split(",")?.let { it[0].toFloat()..it[1].toFloat() } ?: (2006f..2025f),
                mileageRange = map["mileage"]?.split(",")?.let { it[0].toFloat()..it[1].toFloat() } ?: (0f..300000f),
                selectedTypes = map["types"]?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                selectedFuels = map["fuels"]?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                selectedRegions = map["regions"]?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            )
        } catch (e: Exception) {
            return null
        }
    }


    fun saveHistoryToPrefs(list: List<SearchRecordItem>) {
        val rawSet = list.map { serializeRecord(it) }.toSet()
        prefs.edit().putStringSet("filter_history_set", rawSet).apply()

        _filterHistory.value = list
    }

    private fun loadHistoryFromPrefs(): List<SearchRecordItem> {
        val rawSet = prefs.getStringSet("filter_history_set", emptySet()) ?: return emptyList()
        return rawSet.mapNotNull { deserializeRecord(it) }
    }

    fun restoreFrom(item: SearchRecordItem) {
        _filterState.value = SearchFilterState(
            priceRange = item.priceRange,
            yearRange = item.yearRange,
            mileageRange = item.mileageRange,
            selectedTypes = item.selectedTypes,
            selectedFuels = item.selectedFuels,
            selectedRegions = item.selectedRegions
        )
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

class SearchFilterViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchFilterViewModel::class.java)) {
            return SearchFilterViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}