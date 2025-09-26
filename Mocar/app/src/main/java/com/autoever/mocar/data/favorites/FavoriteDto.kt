package com.autoever.mocar.data.favorites

import androidx.annotation.Keep

@Keep
data class FavoriteDto(
    val fid: String = "",
    val userId: String = "",
    val listingId: String = ""
)