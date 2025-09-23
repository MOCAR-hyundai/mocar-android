package com.autoever.mocar.ui.chat

data class ChatRowUi(
    val id: String,
    val avatarUrl: String,
    val partnerName: String,
    val listingTitle: String,
    val lastMessage: String,
    val lastAt: Long
)