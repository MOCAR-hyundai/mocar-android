package com.autoever.mocar.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.RangeSlider
import androidx.compose.material.SliderDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RangeInputSlider(
    title: String,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f
) {
    var sliderRange by remember { mutableStateOf(valueRange) }
    var minInput by remember { mutableStateOf(sliderRange.start.toInt().toString()) }
    var maxInput by remember { mutableStateOf(sliderRange.endInclusive.toInt().toString()) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp
        )

        RangeSlider(
            value = sliderRange,
            onValueChange = {
                sliderRange = it
                minInput = it.start.toInt().toString()
                maxInput = it.endInclusive.toInt().toString()},
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF3058EF),
                inactiveTrackColor = Color.LightGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = minInput,
                onValueChange = {
                    minInput = it
                    val num = it.toFloatOrNull()
                    if (num != null && num in valueRange && num <= sliderRange.endInclusive) {
                        sliderRange = num..sliderRange.endInclusive
                    }
                },
                readOnly = false,
                placeholder = { Text("최소 $title") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                suffix = { Text(unit, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text("~", modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = maxInput,
                onValueChange = {
                    maxInput = it
                    val num = it.toFloatOrNull()
                    if (num != null && num in valueRange && num >= sliderRange.start) {
                        sliderRange = sliderRange.start..num
                    }
                },
                readOnly = false,
                placeholder = { Text("최대 $title") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                suffix = { Text(unit, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
        }
    }
}

@Composable
fun Price() {
    RangeInputSlider(title = "가격", unit = "만원", valueRange = 0f..10000f)
}

@Composable
fun Year() {
    RangeInputSlider(title = "연식", unit = "년", valueRange = 2000f..2025f)
}

@Composable
fun Mileage() {
    RangeInputSlider(title = "주행거리", unit = "km", valueRange = 0f..200000f)
}
