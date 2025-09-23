package com.autoever.mocar.repository

import com.autoever.mocar.data.brands.BrandDto
import com.autoever.mocar.data.chats.ChatRoomDto
import com.autoever.mocar.data.chats.MessageDto
import com.autoever.mocar.data.chats.toDomain
import com.autoever.mocar.data.favorites.FavoriteDto
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.price.PriceIndexDto
import com.autoever.mocar.domain.model.ChatRoom
import com.autoever.mocar.domain.model.Message
import com.autoever.mocar.domain.model.Seller
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

class FirebaseMocarRepository(
    private val db: FirebaseFirestore
) : MocarRepository {

    private val listings = db.collection("listings")

    override fun listingsOnSale(): Flow<List<ListingDto>> = callbackFlow {
        val reg = db.collection("listings")
            .whereEqualTo("status", "on_sale")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject(ListingDto::class.java) } ?: emptyList()
                val sorted = list.sortedByDescending { it.createdAt ?: "" }
                trySend(sorted)
            }
        awaitClose { reg.remove() }
    }

    override fun myFavoriteListingIds(userId: String): Flow<Set<String>> = callbackFlow {
        val reg = db.collection("favorites")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val ids = snap?.documents?.mapNotNull { it.getString("listingId") }?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun toggleFavorite(userId: String, listingId: String) {
        val fid = "${userId}_${listingId}"
        val favRef = db.collection("favorites").document(fid)
        val snap = favRef.get().await()
        if (snap.exists()) {
            favRef.delete().await()
        } else {
            val dto = FavoriteDto(fid = fid, userId = userId, listingId = listingId)
            favRef.set(dto).await()
        }
    }

    override fun listingById(listingId: String): Flow<ListingDto?> = callbackFlow {
        val reg = db.collection("listings").document(listingId)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(null); return@addSnapshotListener }
                trySend(snap?.toObject(ListingDto::class.java))
            }
        awaitClose { reg.remove() }
    }

    override fun brands(): Flow<List<BrandDto>> = callbackFlow {
        val sub = db.collection("car_brand")
            .orderBy("order")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val result = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(BrandDto::class.java)
                    }
                    trySend(result)
                }
            }
        awaitClose { sub.remove() }
    }

    override fun priceIndexById(id: String): Flow<PriceIndexDto?> =
        callbackFlow {
            val ref = db.collection("priceIndex").document(id)
            val reg = ref.addSnapshotListener { snap, err ->
                if (err != null) { trySend(null); return@addSnapshotListener }
                if (snap != null && snap.exists()) {
                    trySend(
                        PriceIndexDto(
                            id = snap.getString("id") ?: snap.id,
                            minPrice = (snap.getLong("minPrice") ?: 0).toLong(),
                            avgPrice = (snap.getLong("avgPrice") ?: 0).toLong(),
                            maxPrice = (snap.getLong("maxPrice") ?: 0).toLong(),
                            mileageBucket = (snap.get("mileageBucket") as? Map<String, Long?>) ?: emptyMap()
                        )
                    )
                } else trySend(null)
            }
            awaitClose { reg.remove() }
        }

    override suspend fun findListingByPlateAndOwner(
        plateNo: String,
        ownerName: String
    ): ListingDto? {
        val q = listings
            .whereEqualTo("plateNo", plateNo)
            .whereEqualTo("ownerName", ownerName)
            .limit(1)
            .get()
            .await()
        return q.documents.firstOrNull()?.toObject(ListingDto::class.java)
    }

    override suspend fun startOrUpdateSale(
        listingId: String,
        mileageKm: Int?,
        priceKRW: Long?,
        description: String?,
        images: List<String>?
    ): StartSaleResult {
        val ref = listings.document(listingId)
        val snap = ref.get().await()
        if (!snap.exists()) return StartSaleResult.NotFound("listing not found")
        val status = snap.getString("status") ?: ""
        if (status == "on_sale") return StartSaleResult.AlreadyOnSale(listingId)

        val upd = hashMapOf<String, Any>(
            "status" to "on_sale",
            "updatedAt" to com.google.firebase.Timestamp.now()
        ).apply {
            mileageKm?.let { put("mileage", it) }
            priceKRW?.let { put("price", it) }
            description?.let { put("description", it) }
            images?.takeIf { it.isNotEmpty() }?.let { put("images", it) }
        }

        ref.update(upd).await()
        return StartSaleResult.Updated(listingId)
    }

    private fun chatDoc(chatId: String) = db.collection("chats").document(chatId)

    override fun chatMessages(chatId: String, limit: Int): Flow<List<Message>> = callbackFlow {
        val sub = chatDoc(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, e ->
                if (e != null) { trySend(emptyList()); return@addSnapshotListener }
                val msgs = snap?.documents?.mapNotNull { d ->
                    d.toObject(MessageDto::class.java)?.copy(msgId = d.id)?.toDomain("")
                } ?: emptyList()
                trySend(msgs)
            }
        awaitClose { sub.remove() }
    }

    override suspend fun sendMessage(chatId: String, fromUid: String, text: String, imageUrl: String?) {
        val now = Timestamp.now()
        val msgRef = chatDoc(chatId).collection("messages").document()
        db.runBatch { b ->
            b.set(msgRef, MessageDto(
                msgId = msgRef.id,
                senderId = fromUid,
                text = text,
                imageUrl = imageUrl,
                createdAt = now,
                readBy = listOf(fromUid)
            )
            )
            b.update(chatDoc(chatId), mapOf(
                "lastMessage" to text,
                "lastAt" to now
            ))
        }.await()
    }

    override suspend fun openChatForListing(listingId: String, buyerId: String, sellerId: String): String {
        val a = minOf(buyerId, sellerId)
        val b = maxOf(buyerId, sellerId)
        val chatId = "chat_${a}_${b}_$listingId"
        val ref = chatDoc(chatId)
        val snap = ref.get().await()
        if (!snap.exists()) {
            val listing = listings.document(listingId).get().await()
            val title = listing.getString("title") ?: ""
            ref.set(
                ChatRoomDto(
                chatId = chatId,
                listingId = listingId,
                listingTitle = title,
                buyerId = buyerId,
                sellerId = sellerId,
                lastMessage = "",
                lastAt = Timestamp.now()
            )
            ).await()
        }
        return chatId
    }

    override fun chatRooms(myUid: String): Flow<List<ChatRoom>> = callbackFlow {
        val map = LinkedHashMap<String, ChatRoomDto>() // id 중복 방지 + 정렬 유지
        val regs = mutableListOf<ListenerRegistration>()

        fun push() {
            val list = map.values
                .sortedByDescending { it.lastAt ?: Timestamp(0,0) }
                .map { dto -> dto.toDomain(myUid) }   // ← chats/ChatRoomDto → domain.ChatRoom
            trySend(list)
        }

        regs += db.collection("chats")
            .whereEqualTo("buyerId", myUid)
            .addSnapshotListener { snap, _ ->
                snap?.documents?.forEach { d ->
                    d.toObject(ChatRoomDto::class.java)
                        ?.let { map[d.id] = it.copy(chatId = d.id) }
                }
                push()
            }

        regs += db.collection("chats")
            .whereEqualTo("sellerId", myUid)
            .addSnapshotListener { snap, _ ->
                snap?.documents?.forEach { d ->
                    d.toObject(ChatRoomDto::class.java)
                        ?.let { map[d.id] = it.copy(chatId = d.id) }
                }
                push()
            }

        awaitClose { regs.forEach { it.remove() } }
    }

    override fun sellerById(uid: String): Flow<Seller?> = callbackFlow {
        val reg = db.collection("users").document(uid)
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
                } else trySend(null)
            }
        awaitClose { reg.remove() }
    }

}