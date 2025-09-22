package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.repository.FirebaseMocarRepository
import com.autoever.mocar.repository.MocarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ChatEntryViewModel(
    private val repo: MocarRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun openChatAndNavigate(
        listingId: String,
        sellerId: String,
        onReady: (String) -> Unit,
        onFail: (Throwable) -> Unit = {}
    ) {
        val myUid = auth.currentUser?.uid
            ?: return onFail(IllegalStateException("로그인이 필요합니다."))

        viewModelScope.launch {
            try {
                val chatId = repo.openChatForListing(
                    listingId = listingId,
                    buyerId = myUid,
                    sellerId = sellerId
                )
                onReady(chatId)
            } catch (t: Throwable) {
                onFail(t)
            }
        }
    }

    companion object {
        fun factory(
            repo: MocarRepository = FirebaseMocarRepository(FirebaseFirestore.getInstance()),
            auth: FirebaseAuth = FirebaseAuth.getInstance()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatEntryViewModel(repo, auth) as T
            }
        }
    }
}