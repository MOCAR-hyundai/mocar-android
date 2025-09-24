package com.autoever.mocar.repository

import com.autoever.mocar.data.brands.BrandDto
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.price.PriceIndexDto
import com.autoever.mocar.domain.model.ChatRoom
import com.autoever.mocar.domain.model.Message
import com.autoever.mocar.domain.model.Seller
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface MocarRepository {
    /** 판매중 매물 스트림 */
    fun listingsOnSale(): Flow<List<ListingDto>>
    /** 내 찜 스트림 */
    fun myFavoriteListingIds(userId: String): Flow<Set<String>>
    /** 토글 찜 */
    suspend fun toggleFavorite(userId: String, listingId: String)

    fun listingById(listingId: String): Flow<ListingDto?>

    fun brands(): Flow<List<BrandDto>>

    fun priceIndexById(id: String): Flow<PriceIndexDto?>

    suspend fun findListingByPlateAndOwner(plateNo: String, ownerName: String): ListingDto?
    suspend fun startOrUpdateSale(
        listingId: String,
        mileageKm: Int?,       // null이면 유지
        priceKRW: Long?,
        description: String?,
        images: List<String>?, // 스토리지 업로드 후 URL 리스트
    ): StartSaleResult

    // ---------------- 채팅 ----------------
    /** 특정 채팅방 메시지 스트림 */
    fun chatMessages(chatId: String, limit: Int = 200): Flow<List<Message>>

    /** 메시지 전송 */
    suspend fun sendMessage(chatId: String, fromUid: String, text: String, imageUrl: String? = null)

    /** 채팅방 오픈 (없으면 새로 만들고, 있으면 기존거 리턴) */
    suspend fun openChatForListing(listingId: String, buyerId: String, sellerId: String): String

    /** 내가 속한 채팅방 스트림 */
    fun chatRooms(myUid: String): Flow<List<ChatRoom>>

    fun sellerById(uid: String): Flow<Seller?>

    suspend fun updateListingStatus(listingId: String, status: String)

    suspend fun fetchListingsPage(
        limit: Int,
        startAfter: DocumentSnapshot? = null,
        brandEquals: String? = null,
        orderByField: String = "createdAt",
        descending: Boolean = true
    ): Pair<List<com.google.firebase.firestore.DocumentSnapshot>, DocumentSnapshot?>
    suspend fun fetchListingsByIds(ids: List<String>): List<DocumentSnapshot>
}

sealed class StartSaleResult {
    data class Updated(val id: String): StartSaleResult()
    data class AlreadyOnSale(val id: String): StartSaleResult()
    data class NotFound(val reason: String): StartSaleResult()
}