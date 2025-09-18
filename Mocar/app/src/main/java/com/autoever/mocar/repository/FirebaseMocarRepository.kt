package com.autoever.mocar.repository

import com.autoever.mocar.data.favorites.FavoriteDto
import com.autoever.mocar.data.listings.ListingDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseMocarRepository(
    private val db: FirebaseFirestore
) : MocarRepository {

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
}