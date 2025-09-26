package com.autoever.mocar.data.price

data class PriceIndexDto(
    val id: String = "",
    val minPrice: Long = 0,
    val avgPrice: Long = 0,
    val maxPrice: Long = 0,
    val mileageBucket: Map<String, Long?> = emptyMap()
)