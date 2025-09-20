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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoever.mocar.viewmodel.SearchFilterViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RangeInputSlider(
    title: String,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    currentRange: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    var minInput by remember(currentRange) { mutableStateOf(currentRange.start.toInt().toString()) }
    var maxInput by remember(currentRange) { mutableStateOf(currentRange.endInclusive.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(title, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))

        RangeSlider(
            value = currentRange,
            onValueChange = {
                onValueChange(it)
                minInput = it.start.toInt().toString()
                maxInput = it.endInclusive.toInt().toString()
            },
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
                    if (num != null && num in valueRange && num <= currentRange.endInclusive) {
                        onValueChange(num..currentRange.endInclusive)
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("최소") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                suffix = { Text(unit, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
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
                    if (num != null && num in valueRange && num >= currentRange.start) {
                        onValueChange(currentRange.start..num)
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("최대") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                suffix = { Text(unit, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun Price(viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()
    RangeInputSlider(
        title = "가격",
        unit = "만원",
        valueRange = 0f..10000f,
        currentRange = state.priceRange,
        onValueChange = { viewModel.updatePrice(it) }
    )
}

@Composable
fun Year(viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()
    RangeInputSlider(
        title = "연식",
        unit = "년",
        valueRange = 2006f..2025f,
        currentRange = state.yearRange,
        onValueChange = { viewModel.updateYear(it) }
    )
}

@Composable
fun Mileage(viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()
    RangeInputSlider(
        title = "주행거리",
        unit = "km",
        valueRange = 0f..200000f,
        currentRange = state.mileageRange,
        onValueChange = { viewModel.updateMileage(it) }
    )
}