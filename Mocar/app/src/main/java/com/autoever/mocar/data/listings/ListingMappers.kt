package com.autoever.mocar.data.listings

import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.viewmodel.ListingStatus

fun ListingDto.toCar(): Car = Car(
    id = listingId,
    plateNo = plateNo,
    title = title.ifBlank { listOf(brand, model, trim).filter { it.isNotBlank() }.joinToString(" ") },
    yearDesc = "${year}년식",
    mileageKm = mileage.toInt(),
    transmission = transmission,
    carType = carType,
    displacement = displacement,
    fuel = fuel,
    region = region,
    priceKRW = price,
    images = images,
    imageUrl = images.firstOrNull(),
    brandId = "",
    brandName = brand,
    description = description.ifBlank { null },
    status = (status?.ifBlank { ListingStatus.ON_SALE } ?: ListingStatus.ON_SALE),
    sellerId = sellerId ?: ""
)