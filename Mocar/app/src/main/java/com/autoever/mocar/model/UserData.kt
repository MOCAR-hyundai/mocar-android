package com.autoever.mocar.model
import com.google.firebase.Timestamp

data class UserData(
    var name : String,
    var email: String,
    var phone: String? = null,
    var photoUrl: String? = null,
    var dob: String? = null,
    var rating: Int = 0,
    var ratingCount: Int = 0,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
)