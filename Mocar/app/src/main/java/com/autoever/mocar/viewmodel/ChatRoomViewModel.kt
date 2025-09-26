package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.chats.MessageDto
import com.autoever.mocar.domain.model.Message
import com.autoever.mocar.domain.model.Seller
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatRoomViewModel(
    private val repo: MocarRepository,
    private val chatId: String,
    private val uid: String
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    val messages: StateFlow<List<Message>> =
        repo.chatMessages(chatId) // Flow<List<Message>>
            .map { list ->
                list
                    .map { it.copy(mine = it.senderId == uid) }
                    .sortedBy { it.createdAt }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 채팅방에서 상대방 uid 구하기 (내 uid 기준으로 buyer/seller 중 상대를 고름) */
    private val partnerId: StateFlow<String?> =
        callbackFlow<String?> {
            val reg = db.collection("chats").document(chatId)
                .addSnapshotListener { snap, _ ->
                    if (snap == null || !snap.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val buyerId = snap.getString("buyerId")
                    val sellerId = snap.getString("sellerId")
                    val pid = when (uid) {
                        buyerId -> sellerId
                        sellerId -> buyerId
                        else -> null
                    }
                    trySend(pid)
                }
            awaitClose { reg.remove() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 상대방 프로필(Seller) 스트림 */
    val partner: StateFlow<Seller?> =
        partnerId.flatMapLatest { pid ->
            if (pid.isNullOrBlank()) {
                flowOf(null)
            } else {
                callbackFlow<Seller?> {
                    val reg = db.collection("users").document(pid)
                        .addSnapshotListener { snap, _ ->
                            if (snap != null && snap.exists()) {
                                trySend(
                                    Seller(
                                        id = snap.id,
                                        name = snap.getString("name") ?: "",
                                        photoUrl = snap.getString("photoUrl") ?: "",
                                        rating = snap.getDouble("rating") ?: 0.0,
                                        ratingCount = (snap.getLong("ratingCount") ?: 0L).toInt()
                                    )
                                )
                            } else {
                                trySend(null)
                            }
                        }
                    awaitClose { reg.remove() }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun send(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.sendMessage(chatId, uid, text.trim())
        }
    }

    companion object {
        fun factory(
            chatId: String,
            repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
            auth: FirebaseAuth = FirebaseAuth.getInstance()
        ): ViewModelProvider.Factory {
            val me = requireNotNull(auth.currentUser?.uid) { "ChatRoomViewModel: 로그인 필요" }
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatRoomViewModel(repo, chatId, me) as T
                }
            }
        }
    }
}