package com.autoever.mocar.domain.model

data class Seller(
    val id: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0
)