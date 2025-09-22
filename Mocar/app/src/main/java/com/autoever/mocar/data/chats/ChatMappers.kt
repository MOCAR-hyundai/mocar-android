package com.autoever.mocar.data.chats

import com.autoever.mocar.domain.model.ChatRoom
import com.autoever.mocar.domain.model.Message

fun ChatRoomDto.toDomain(myUid: String): ChatRoom =
    ChatRoom(
        id = chatId,
        listingTitle = listingTitle.ifBlank { listingId },
        lastMessage = lastMessage,
        lastAt = lastAt?.toDate()?.time ?: 0L,
        partnerId = if (buyerId == myUid) sellerId else buyerId
    )

fun MessageDto.toDomain(myUid: String): Message =
    Message(
        id = msgId,
        senderId = senderId,
        text = text,
        imageUrl = imageUrl,
        createdAt = createdAt?.toDate()?.time ?: 0L,
        mine = senderId == myUid
    )