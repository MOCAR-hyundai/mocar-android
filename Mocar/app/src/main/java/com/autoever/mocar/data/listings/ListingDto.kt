package com.autoever.mocar.data.listings

import androidx.annotation.Keep
import java.security.Timestamp

@Keep
data class ListingDto(
    val listingId: String = "",
    val sellerId: String = "",
    val title: String = "",
    val brand: String = "",
    val model: String = "",
    val carType: String = "",
    val displacement: Int = 0,
    val trim: String = "",
    val year: Int = 0,
    val mileage: Long = 0,
    val fuel: String = "",
    val transmission: String = "",
    val price: Long = 0,
    val region: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val status: String = "on_sale",
    val createdAt: String? = null
)