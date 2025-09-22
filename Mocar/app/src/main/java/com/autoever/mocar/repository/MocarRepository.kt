package com.autoever.mocar.repository

import com.autoever.mocar.data.brands.BrandDto
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.price.PriceIndexDto
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
}

sealed class StartSaleResult {
    data class Updated(val id: String): StartSaleResult()
    data class AlreadyOnSale(val id: String): StartSaleResult()
    data class NotFound(val reason: String): StartSaleResult()
}