package com.autoever.mocar.data.chats

import com.google.firebase.Timestamp


data class ChatRoomDto(
    val chatId: String = "",
    val listingId: String = "",
    val listingTitle: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val lastMessage: String = "",
    val lastAt: Timestamp? = null
)