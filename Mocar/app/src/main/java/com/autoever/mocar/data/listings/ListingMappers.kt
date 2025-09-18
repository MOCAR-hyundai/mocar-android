package com.autoever.mocar.data.listings

import com.autoever.mocar.domain.model.Car

fun ListingDto.toCar(isFavorite: Boolean): Car = Car(
    id = listingId,
    plateNo = "", // Firestore에 없으면 빈값
    title = title.ifBlank { listOf(brand, model, trim).filter { it.isNotBlank() }.joinToString(" ") },
    yearDesc = "${year}년식",
    mileageKm = mileage.toInt(),
    transmission = transmission,
    carType = carType,
    displacement = displacement,
    fuel = fuel,
    region = region,
    priceKRW = price,
    imageUrl = images.firstOrNull(),
    isFavorite = isFavorite,
    brandId = brand
)