package com.autoever.mocar.ui.common.component.molecules

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun FavoriteCarousel(
    navController: NavController,
    cars: List<CarUi>,
    horizontalSpacingDp: Int = 12
) {
    LazyRow(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(horizontalSpacingDp.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(cars, key = { it.id }) { car ->
            CarCard(
                navController = navController,
                car = car,
            )
        }
    }
}