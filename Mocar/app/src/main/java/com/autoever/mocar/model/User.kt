package com.autoever.mocar.model

data class User(
    val id: String,
    val displayName: String,
    val phone: String? = null,
    val photoUrl: String? = null,
    val city: String? = null,
    val district: String? = null,
    val role: String = "buyer"   // buyer, seller, admin
)