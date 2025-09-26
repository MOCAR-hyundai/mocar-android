package com.autoever.mocar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.listings.ListingDto
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
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

    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private var keywordDocMap = mutableMapOf<String, String>()

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
                        val brandModel = "${listing.brand} ${listing.model}".lowercase()
                        brandModel.contains(query.lowercase().trim()) ||
                                listing.brand.contains(query, ignoreCase = true) ||
                                listing.model.contains(query, ignoreCase = true)
                    }
                }
            )
        }
    }

    fun loadRecentKeywords(userId: String) {
        db.collection("recent_keyword")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val sortedDocs =
                    result.sortedByDescending { it.getTimestamp("timestamp")?.toDate() }
                val keywords = sortedDocs.mapNotNull { doc ->
                    val keyword = doc.getString("keyword")
                    if (keyword != null) {
                        keywordDocMap[keyword] = doc.id
                    }
                    keyword
                }
                _uiState.update { it.copy(recentKeywords = keywords.take(10)) }
            }
    }

    fun submitSearch(userId: String) {
        val keyword = _uiState.value.query.trim()
        if (keyword.isBlank()) return

        val collection = db.collection("recent_keyword")

        collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("keyword", keyword)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // 추가
                    val data = mapOf(
                        "userId" to userId,
                        "keyword" to keyword,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    collection.add(data).addOnSuccessListener {
                        loadRecentKeywords(userId)
                        trimKeywordCount(userId) // 개수 유지
                    }
                } else {
                    // 갱신
                    val docId = result.first().id
                    collection.document(docId)
                        .update("timestamp", FieldValue.serverTimestamp())
                        .addOnSuccessListener {
                            loadRecentKeywords(userId)
                        }
                }
            }
    }

    fun removeKeyword(keyword: String) {
        val docId = keywordDocMap[keyword]
        if (docId == null) {
            Log.e("Firestore", "삭제 실패: docId 없음 for keyword: $keyword")
            return
        }

        db.collection("recent_keyword").document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "✅ 삭제 성공: $keyword ($docId)")
                keywordDocMap.remove(keyword)
                _uiState.update {
                    it.copy(recentKeywords = it.recentKeywords - keyword)
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Firestore 삭제 실패", it)
            }
    }

    private fun trimKeywordCount(userId: String) {
        db.collection("recent_keyword")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val toDelete = result.drop(10)
                toDelete.forEach { doc ->
                    db.collection("recent_keyword").document(doc.id).delete()
                }
            }
    }

    fun clearAllKeywords(userId: String) {
        db.collection("recent_keyword")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                result.forEach { doc ->
                    db.collection("recent_keyword").document(doc.id).delete()
                }
                keywordDocMap.clear()
                _uiState.update { it.copy(recentKeywords = emptyList()) }
            }
    }

    fun selectCar(car: ListingDto, userId: String) {
        val keyword = "${car.brand} ${car.model}"
        _uiState.update {
            it.copy(query = keyword)
        }
        submitSearch(userId)
    }
}