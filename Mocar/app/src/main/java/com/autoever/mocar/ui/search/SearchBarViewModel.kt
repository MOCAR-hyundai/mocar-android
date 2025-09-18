package com.autoever.mocar.ui.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SearchUiState(
    val query: String = "",
    val results: List<String> = emptyList(),
    val recentKeywords: List<String> = emptyList(),
    val searchResults: List<CarSeed> = emptyList()
)

class SearchBarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val allModels = listOf("아반떼", "그랜저", "쏘나타", "포터", "싼타페", "i30", "i40", "K3", "K5", "K7")

    private val carSeeds = listOf(
        CarSeed("현대", "아반떼", "준중형", listOf("가솔린(휘발유)", "LPG"), 2800, 160, 9000, 14000),
        CarSeed("현대2", "아반떼", "준중형", listOf("가솔린(휘발유)", "LPG"), 2800, 160, 9000, 14000),
        CarSeed("현대", "그랜저", "대형", listOf("가솔린(휘발유)", "하이브리드", "LPG"), 5200, 220, 15000, 17000),
        CarSeed("기아", "K5", "중형", listOf("가솔린(휘발유)"), 3200, 180, 10000, 15000),
        CarSeed("기아", "레이", "경차", listOf("가솔린(휘발유)"), 1800, 120, 7000, 12000)
    )
    fun activateSearch() {
        _isSearchActive.value = true
    }

    fun deactivateSearch() {
        _isSearchActive.value = false
    }

    fun updateQuery(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                searchResults = if (query.isBlank()) emptyList()
                else carSeeds.filter { car -> car.model.contains(query) }
            )
        }
    }

    fun submitSearch() {
        val keyword = _uiState.value.query.trim()
        if (keyword.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    recentKeywords = (listOf(keyword) + it.recentKeywords).distinct().take(10)
                    // ✅ query, searchResults는 유지 (지우지 않기!)
                )
            }
            // 🔁 굳이 닫지 말고 유지
            // deactivateSearch()
        }
    }
    fun removeKeyword(keyword: String) {
        _uiState.update {
            it.copy(recentKeywords = it.recentKeywords - keyword)
        }
    }

    fun clearAllKeywords() {
        _uiState.update {
            it.copy(recentKeywords = emptyList())
        }
    }

    fun selectCar(car: CarSeed) {
        val keyword = "${car.maker} ${car.model}"
        _uiState.update {
            it.copy(
                recentKeywords = (listOf(keyword) + it.recentKeywords).distinct().take(10)
            )
        }
    }
}

data class CarSeed(
    val maker: String,
    val model: String,
    val category: String,
    val fuels: List<String>,
    val basePrice: Int,
    val priceStep: Int,
    val baseMileage: Int,
    val mileageStep: Int
)
