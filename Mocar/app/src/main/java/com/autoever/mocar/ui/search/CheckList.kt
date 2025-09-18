package com.autoever.mocar.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

data class SelectableItem(
    val name: String,
    val count: Int
)

@Composable
fun CheckList(
    title: String,
    items: List<SelectableItem>,
    selectedItems: List<String>,
    onToggle: (String) -> Unit
) {
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

        LazyColumn {
            items(items.size) { index ->

                val item = items[index]

                SelectableItemRow(
                    item = item,
                    isSelected = selectedItems.contains(item.name),
                    onToggle = { onToggle(item.name) }
                )

                if (index < items.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 0.7.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableItemRow(
    item: SelectableItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val isEnabled = item.count > 0
    val iconTint = if (!isEnabled) Color.LightGray else if (isSelected) Color(0xFF3058EF) else Color.Gray
    val textColor = if (isEnabled) Color.Unspecified else Color.LightGray

    val rowModifier = if (isEnabled) {
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onToggle() }
            .padding(vertical = 10.dp, horizontal = 10.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 10.dp, horizontal = 10.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isSelected && isEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = iconTint
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = textColor
        )

        Text(
            text = "%,d".format(item.count),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CarType(viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()

    val allTypes = listOf(
        SelectableItem("경차", 14235),
        SelectableItem("소형", 5424),
        SelectableItem("준중형", 0),
        SelectableItem("중형", 33211),
        SelectableItem("대형", 26573),
        SelectableItem("스포츠카", 2634),
        SelectableItem("SUV", 73424),
        SelectableItem("RV", 5623),
        SelectableItem("승합", 5234),
        SelectableItem("트럭", 17423),
        SelectableItem("버스", 253),
        SelectableItem("중기", 0)
    )

    CheckList(
        title = "차종",
        items = allTypes,
        selectedItems = state.selectedTypes,
        onToggle = { viewModel.toggleType(it) }
    )
}


@Composable
fun Fuel(viewModel: SearchFilterViewModel = viewModel()) {

    val state by viewModel.filterState.collectAsState()

    val allFuels = listOf(
        SelectableItem("가솔린(휘발유)", 92754),
        SelectableItem("디젤(경유)", 11180),
        SelectableItem("전기", 5524),
        SelectableItem("LPI/LPG(가스)", 14249),
        SelectableItem("하이브리드(가솔린)", 0),
        SelectableItem("하이브리드(디젤)", 2152),
        SelectableItem("하이브리드(LPG)", 31),
        SelectableItem("가솔린+LPG(바이퓨얼)", 74),
        SelectableItem("CNG(압축천연가스)", 5),
        SelectableItem("기타", 275)
    )

    CheckList(
        title = "연료",
        items = allFuels,
        selectedItems = state.selectedFuels,
        onToggle = { viewModel.toggleFuel(it) }
    )
}

@Composable
fun Region(viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()

    val allRegions = listOf(
        SelectableItem("서울", 9275),
        SelectableItem("인천", 11180),
        SelectableItem("대전", 5524),
        SelectableItem("대구", 5449),
        SelectableItem("광주", 10334),
        SelectableItem("부산", 12152),
        SelectableItem("울산", 2831),
        SelectableItem("세종", 1),
        SelectableItem("경기", 71406),
        SelectableItem("강원", 9275),
        SelectableItem("경남", 11180),
        SelectableItem("경북", 123),
        SelectableItem("전남", 0),
        SelectableItem("전북", 346),
        SelectableItem("충남", 0),
        SelectableItem("충북", 567),
        SelectableItem("제주", 43),
        )

    CheckList(
        title = "지역",
        items = allRegions,
        selectedItems = state.selectedRegions,
        onToggle = { viewModel.toggleRegion(it) }
    )
}