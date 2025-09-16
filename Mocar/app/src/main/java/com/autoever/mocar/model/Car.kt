package com.autoever.mocar.model

data class Car(
    val id: String,
    val title: String,          // 차량명
    val mileageKm: Int,         // 주행거리(킬로미터)
    val region: String,         // 지역(예: "서울", "부산")
    val priceKRW: Long,         // 가격(원)
    val imageRes: Int,          // drawable 리소스 (샘플용)
    var isFavorite: Boolean = false,
    val brandId: String
)