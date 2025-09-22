package com.autoever.mocar.data.chats

import com.google.firebase.Timestamp


data class MessageDto(
    val msgId: String = "",
    val senderId: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val createdAt: Timestamp? = null,
    val readBy: List<String> = emptyList()
)