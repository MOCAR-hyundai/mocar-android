package com.autoever.mocar.model

data class Car(
    val id: String,              // 내부 고유 ID
    val plateNo: String,         // 차량번호 (예: "21나4827")
    val title: String,           // 차량명 (예: "Tesla Model S")
    val yearDesc: String,        // 연식 (예: "22년12월(23년형)")
    val mileageKm: Int,          // 주행거리 (km)
    val transmission: String,    // 변속기 (예: "오토")
    val carType: String,         // 차종 (예: "대형", "중형", "SUV")
    val displacement: Int,       // 배기량 (cc)
    val fuel: String,            // 연료 (예: "하이브리드(가솔린)")
    val region: String,          // 지역 (예: "서울")
    val priceKRW: Long,          // 가격 (원)
    val imageRes: Int,           // drawable 리소스
    var isFavorite: Boolean = false,
    val brandId: String          // 브랜드 ID
)