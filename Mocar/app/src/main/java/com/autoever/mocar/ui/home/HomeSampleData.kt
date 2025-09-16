package com.autoever.mocar.ui.home

import com.autoever.mocar.R
import com.autoever.mocar.model.Brand
import com.autoever.mocar.model.Car

object HomeSampleData {
    val cars = listOf(
        Car(id = "1", title = "Ferrari-FF", mileageKm = 171_614, region = "서울", priceKRW = 138_600_000, imageRes = R.drawable.sample_car_1),
        Car(id = "2", title = "Tesla Model S", mileageKm = 98_000, region = "서울", priceKRW = 100_000_000, imageRes = R.drawable.sample_car_2),
        Car(id = "3", title = "BMW 530i", mileageKm = 82_300, region = "부산", priceKRW = 63_900_000, imageRes = R.drawable.sample_car_1),
        Car(id = "4", title = "Hyundai IONIQ 5", mileageKm = 24_500, region = "인천", priceKRW = 49_800_000, imageRes = R.drawable.sample_car_2),
        Car(id = "5", title = "Ferrari 488", mileageKm = 12_100, region = "서울", priceKRW = 354_000_000, imageRes = R.drawable.sample_car_1),
        Car(id = "6", title = "Tesla Model 3", mileageKm = 45_700, region = "대구", priceKRW = 52_300_000, imageRes = R.drawable.sample_car_2),
    )

    val brands = listOf(
        Brand("tesla", "Tesla", R.drawable.brand_tesla),
        Brand("bmw", "BMW", R.drawable.brand_bmw),
        Brand("ferrari", "Ferrari", R.drawable.brand_ferrari),
        Brand("hyundai", "Hyundai", R.drawable.brand_hyundai),
        Brand("audi", "Audi", R.drawable.brand_audi),
        Brand("kia", "Kia", R.drawable.brand_kia),
    )
}