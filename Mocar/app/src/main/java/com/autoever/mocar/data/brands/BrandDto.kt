package com.autoever.mocar.data.brands

import androidx.annotation.Keep

@Keep
data class BrandDto(
    val id: String = "",
    val name: String = "",
    val logoUrl: String? = null,
    val countryType: String = "", // domestic | imported
    val order: Int = 0,
    val isActive: Boolean = true
)