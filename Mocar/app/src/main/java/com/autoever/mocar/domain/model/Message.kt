package com.autoever.mocar.domain.model

data class Message(
    val id: String,
    val senderId: String,
    val text: String?,
    val imageUrl: String?,
    val createdAt: Long,  // Timestamp → millis
    val mine: Boolean
)