package com.autoever.mocar.ui.common.component.molecules

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteCarousel(
    cars: List<CarUi>,
    onToggleFav: (CarUi) -> Unit,
    onCardClick: (CarUi) -> Unit,
    horizontalSpacingDp: Int = 12
) {
    LazyRow(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(horizontalSpacingDp.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(cars, key = { it.id }) { car ->
            CarCard(
                car = car,
                onFavoriteToggle = { onToggleFav(car) },
                modifier = Modifier.width(260.dp),
                onClick = { onCardClick(car) }
            )
        }
    }
}