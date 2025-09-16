package com.autoever.mocar.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun Manufacturer(searchQuery: String) {
    // 예시로 보여줄 제조사 목록
    val allManufacturers = listOf(
        "현대", "기아", "BMW", "벤츠", "아우디", "테슬라", "폭스바겐", "쉐보레", "르노", "볼보", "현대", "기아", "BMW", "벤츠", "아우디", "테슬라", "폭스바겐", "쉐보레", "르노", "볼보"
    )


    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(allManufacturers) { maker ->
            ManufacturerCard(maker)
        }
    }
}

@Composable
fun ManufacturerCard(name: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable {
            }
            .border(
                width = 0.2.dp,
                color = Color(0xFFD7D7D7),
                shape = RectangleShape
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )

    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Row() {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                Text("22222",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                )

            }
        }
    }
}
