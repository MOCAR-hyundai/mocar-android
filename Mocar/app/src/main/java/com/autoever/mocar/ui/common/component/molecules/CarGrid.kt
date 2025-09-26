package com.autoever.mocar.ui.common.component.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CarGrid(
    navController: NavController,
    cars: List<CarUi>,
) {
    // 2개씩 chunk 처리해서 Row 생성
    cars.chunked(2).forEach { rowCars ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            for (car in rowCars) {
                CarCard(
                    navController = navController,
                    car = car,
                    modifier = Modifier.weight(1f)
                )
            }
            if (rowCars.size < 2) {
                Spacer(modifier = Modifier.weight(1f)) // 짝 맞추기
            }
        }
    }
}
