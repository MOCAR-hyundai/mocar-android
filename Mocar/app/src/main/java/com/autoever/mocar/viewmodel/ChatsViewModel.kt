package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.domain.model.ChatRoom
import com.autoever.mocar.domain.model.Seller
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.autoever.mocar.ui.chat.ChatRowUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChatsViewModel(
    private val repo: MocarRepository,
    private val uid: String
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private fun sellerFlow(userId: String): Flow<Seller?> = callbackFlow {
        var reg: ListenerRegistration? = null
        if (userId.isBlank()) {
            trySend(null)
        } else {
            reg = db.collection("users").document(userId)
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
        }
        awaitClose { reg?.remove() }
    }

    // ChatRoom 리스트 -> 각 방별 Seller join -> ChatRowUi 리스트
    val rooms: StateFlow<List<ChatRowUi>> =
        repo.chatRooms(uid) // Flow<List<ChatRoom>>
            .flatMapLatest { roomList ->
                if (roomList.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    // 방별로 partner 구독 -> combine으로 합치기
                    val flows: List<Flow<ChatRowUi>> = roomList.map { room ->
                        sellerFlow(room.partnerId).map { seller ->
                            ChatRowUi(
                                id = room.id,
                                avatarUrl = seller?.photoUrl.orEmpty(),
                                partnerName = seller?.name ?: "사용자",
                                listingTitle = room.listingTitle,
                                lastMessage = room.lastMessage,
                                lastAt = room.lastAt
                            )
                        }
                    }
                    combine(flows) { it.toList() }
                }
            }
            .map { list ->
                list.distinctBy { it.id }
                    .sortedByDescending { it.lastAt }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(
            repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
            auth: FirebaseAuth = FirebaseAuth.getInstance()
        ): ViewModelProvider.Factory {
            val me = requireNotNull(auth.currentUser?.uid) { "ChatsViewModel: 로그인 필요" }
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatsViewModel(repo, me) as T
                }
            }
        }
    }
}