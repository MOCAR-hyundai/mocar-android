package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import com.autoever.mocar.data.favorites.FavoriteDto
import com.autoever.mocar.data.listings.ListingDto
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


// 검색 결과 viewmodel
class SearchResultViewModel : ViewModel() {
    private val _results = MutableStateFlow<List<ListingDto>>(emptyList())
    val results: StateFlow<List<ListingDto>> = _results

    // 찜 리스트 관리
    private val _favorites = MutableStateFlow<List<FavoriteDto>>(emptyList())
    val favorites: StateFlow<List<FavoriteDto>> = _favorites

    // Firebase
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "favorites"

    // 리스너 관리
    private var favListener: ListenerRegistration? = null
    private val authListener = FirebaseAuth.AuthStateListener { fa ->
        // 로그인 상태가 바뀌면 구독도 갱신
        startOrRestartFavoritesListener(fa.currentUser?.uid)
    }

    init {
        // 앱 시작 시 현재 유저로 구독 시작
        startOrRestartFavoritesListener(auth.currentUser?.uid)
        // 추후 로그인/로그아웃 대응
        auth.addAuthStateListener(authListener)
    }


    // 검색 결과 저장
    fun setResults(listings: List<ListingDto>) {
        _results.value = listings
    }

    fun addFavorite(listing: ListingDto) {
        val uid = auth.currentUser?.uid ?: return
        val fid = "${uid}_${listing.listingId}"

        val dto = FavoriteDto(
            fid = fid,
            userId = uid,
            listingId = listing.listingId
        )

        // 낙관적 UI 업데이트 (리스너로 곧 동기화됨)
        if (_favorites.value.none { it.fid == fid }) {
            _favorites.value += dto
        }

        db.collection(collectionName)
            .document(fid)
            .set(dto)
            .addOnFailureListener {
                // 실패 시 롤백
                _favorites.value = _favorites.value.filterNot { it.fid == fid }
            }
    }

    /** 즐겨찾기 제거 */
    fun removeFavorite(listingId: String) {
        val uid = auth.currentUser?.uid ?: return
        val fid = "${uid}_${listingId}"

        // 낙관적 UI 업데이트
        _favorites.value = _favorites.value.filterNot { it.fid == fid }

        db.collection(collectionName)
            .document(fid)
            .delete()
            .addOnFailureListener {
                // 실패 시 되돌릴 수 있으면 복구(옵션)
                val restored = FavoriteDto(fid, uid, listingId)
                _favorites.value = (_favorites.value + restored).distinctBy { it.fid }
            }
    }
    /** 현재 사용자 uid로 favorites 실시간 구독 시작/재시작 */
    private fun startOrRestartFavoritesListener(uid: String?) {
        favListener?.remove()
        favListener = null

        if (uid == null) {
            _favorites.value = emptyList()
            return
        }

        favListener = db.collection(collectionName)
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener

                val list = snap?.documents?.mapNotNull { d ->
                    FavoriteDto(
                        fid = d.id,
                        userId = d.getString("userId") ?: return@mapNotNull null,
                        listingId = d.getString("listingId") ?: return@mapNotNull null
                    )
                }.orEmpty()

                _favorites.value = list
            }
    }

    override fun onCleared() {
        super.onCleared()
        favListener?.remove()
        auth.removeAuthStateListener(authListener)
    }
}