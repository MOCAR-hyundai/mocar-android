package com.autoever.mocar.ui.home

import com.autoever.mocar.R
import com.autoever.mocar.model.Brand
import com.autoever.mocar.model.Car

object HomeSampleData {
    val cars = listOf(
        Car("t1","Tesla Model S", 98000,"서울",100_000_000,R.drawable.sample_car_2, brandId="tesla"),
        Car("t2","Tesla Model 3", 45700,"대구", 52_300_000,R.drawable.sample_car_2, brandId="tesla"),
        Car("l1","Lamborghini Huracán",12100,"서울",345_000_000,R.drawable.sample_car_1, brandId="lamborghini"),
        Car("b1","BMW 530i",82300,"부산",63_900_000,R.drawable.sample_car_1, brandId="bmw"),
        Car("f1","Ferrari-FF",171_614,"서울",138_600_000,R.drawable.sample_car_1, brandId="ferrari"),
        Car("h1","Hyundai Grandeur 2.5",23_214,"경기",35_900_000,R.drawable.sample_car_2, brandId="hyundai"),
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