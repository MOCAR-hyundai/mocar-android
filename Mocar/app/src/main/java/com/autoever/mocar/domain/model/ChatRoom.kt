package com.autoever.mocar.domain.model

data class ChatRoom(
    val id: String,
    val listingTitle: String,
    val lastMessage: String,
    val lastAt: Long,     // Timestamp → millis 변환해서 UI용
    val partnerId: String // 상대방 uid
)