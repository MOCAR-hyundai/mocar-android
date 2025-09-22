package com.autoever.mocar.ui.common.component.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CarGrid(
    cars: List<CarUi>,
    onFavoriteToggle: (String) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gutter = 12.dp

    // 2개씩 chunk 처리해서 Row 생성
    cars.chunked(2).forEach { rowCars ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(gutter),
            modifier = modifier.fillMaxWidth().padding(bottom = gutter)
        ) {
            for (car in rowCars) {
                CarCard(
                    car = car,
                    onFavoriteToggle = { onFavoriteToggle(car.id) },
                    onClick = { onCardClick(car.id) },
                    modifier = Modifier.weight(1f)
                )
            }
            // 짝수 맞추기 위해 비어있는 칸 Spacer 추가
            if (rowCars.size < 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
