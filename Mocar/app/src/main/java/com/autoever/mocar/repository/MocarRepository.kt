package com.autoever.mocar.repository

import com.autoever.mocar.data.brands.BrandDto
import com.autoever.mocar.data.listings.ListingDto
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
}