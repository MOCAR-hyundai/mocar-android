package com.autoever.mocar.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.listings.ListingDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SearchUiState(
    val query: String = "",
    val results: List<String> = emptyList(),
    val recentKeywords: List<String> = emptyList(),
    val searchResults: List<ListingDto> = emptyList()
)

class SearchBarViewModel(
    private val listingViewModel: ListingViewModel,
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SearchUiState(
            recentKeywords = loadKeywordsFromPrefs()  // 초기 로드
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    init {
        viewModelScope.launch {
            listingViewModel.listings.collect { listings ->
                _uiState.update {
                    it.copy(searchResults = listings)
                }
            }
        }
    }

    fun activateSearch() {
        println("📣 activateSearch called")
        _isSearchActive.value = true
    }

    fun deactivateSearch() {
        _isSearchActive.value = false
    }

    fun updateQuery(query: String) {
        val listings = listingViewModel.listings.value
        _uiState.update {
            it.copy(
                query = query,
                searchResults = if (query.isBlank()) {
                    emptyList()
                } else {
                    listings.filter { listing ->
                        listing.brand.contains(query, ignoreCase = true) ||
                                listing.model.contains(query, ignoreCase = true)
                    }
                }
            )
        }
    }

    private fun loadKeywordsFromPrefs(): List<String> {
        val set = prefs.getStringSet("recent_keywords", emptySet()) ?: emptySet()
        return set.toList()
    }

    private fun saveKeywordsToPrefs(keywords: List<String>) {
        prefs.edit().putStringSet("recent_keywords", keywords.toSet()).apply()
    }

    fun submitSearch() {
        val keyword = _uiState.value.query.trim()
        if (keyword.isNotEmpty()) {
            val updated = (listOf(keyword) + _uiState.value.recentKeywords).distinct().take(10)
            _uiState.update { it.copy(recentKeywords = updated) }
            saveKeywordsToPrefs(updated) // 저장
        }
    }

    fun removeKeyword(keyword: String) {
        val updated = _uiState.value.recentKeywords - keyword
        _uiState.update { it.copy(recentKeywords = updated) }
        saveKeywordsToPrefs(updated)  // 저장
    }

    fun clearAllKeywords() {
        _uiState.update { it.copy(recentKeywords = emptyList()) }
        saveKeywordsToPrefs(emptyList())  // 저장
    }

    fun selectCar(car: ListingDto) {
        val keyword = "${car.brand} ${car.model}"
        val updated = (listOf(keyword) + _uiState.value.recentKeywords).distinct().take(10)
        _uiState.update {
            it.copy(recentKeywords = updated)
        }
        saveKeywordsToPrefs(updated)
    }
}
