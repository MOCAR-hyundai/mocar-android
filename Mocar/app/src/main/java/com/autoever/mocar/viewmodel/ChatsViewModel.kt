package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.domain.model.ChatRoom
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChatsViewModel(
    private val repo: MocarRepository,
    private val uid: String
) : ViewModel() {

    val rooms: StateFlow<List<ChatRoom>> =
        repo.chatRooms(uid)
            .map { list ->
                list
                    .distinctBy { it.id }
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