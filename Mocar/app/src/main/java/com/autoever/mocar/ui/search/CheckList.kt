package com.autoever.mocar.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            items(items) { item ->
                SelectableItemRow(
                    item = item,
                    isSelected = selectedItems.contains(item.name),
                    onToggle = { onToggle(item.name) }
                )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF3058EF) else Color.LightGray
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )

        Text(
            text = "%,d".format(item.count),
            color = Color.Gray,
            fontSize = 14.sp
        )

        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "다음",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(16.dp),
            tint = Color.Gray
        )
    }
}

@Composable
fun CarType() {
    val allTypes = listOf(
        SelectableItem("경차", 14235),
        SelectableItem("소형", 5424),
        SelectableItem("준중형", 26342),
        SelectableItem("중형", 33211),
        SelectableItem("대형", 26573),
        SelectableItem("스포츠카", 2634),
        SelectableItem("SUV", 73424),
        SelectableItem("RV", 5623),
        SelectableItem("승합", 5234),
        SelectableItem("트럭", 17423),
        SelectableItem("버스", 253),
        SelectableItem("중기", 5)
    )

    var selectedType by remember { mutableStateOf(listOf<String>()) }

    CheckList(
        title = "차종 선택",
        items = allTypes,
        selectedItems = selectedType,
        onToggle = { type ->
            selectedType = if (type in selectedType) {
                selectedType - type
            } else {
                selectedType + type
            }
        }
    )
}

@Composable
fun Fuel() {
    val allFuels = listOf(
        SelectableItem("가솔린(휘발유)", 92754),
        SelectableItem("디젤(경유)", 11180),
        SelectableItem("전기", 5524),
        SelectableItem("LPI/LPG(가스)", 14249),
        SelectableItem("하이브리드(가솔린)", 13334),
        SelectableItem("하이브리드(디젤)", 2152),
        SelectableItem("하이브리드(LPG)", 31),
        SelectableItem("가솔린+LPG(바이퓨얼)", 74),
        SelectableItem("CNG(압축천연가스)", 5),
        SelectableItem("기타", 275)
    )

    var selectedFuel by remember { mutableStateOf(listOf<String>()) }

    CheckList(
        title = "연료 선택",
        items = allFuels,
        selectedItems = selectedFuel,
        onToggle = { fuel ->
            selectedFuel = if (fuel in selectedFuel) {
                selectedFuel - fuel
            } else {
                selectedFuel + fuel
            }
        }
    )
}

@Composable
fun Region() {
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
        SelectableItem("전남", 34),
        SelectableItem("전북", 346),
        SelectableItem("충남", 634),
        SelectableItem("충북", 567),
        SelectableItem("제주", 43),
        )

    var selectedRegions by remember { mutableStateOf(listOf<String>()) }

    CheckList(
        title = "지역 선택",
        items = allRegions,
        selectedItems = selectedRegions,
        onToggle = { region ->
            selectedRegions = if (region in selectedRegions) {
                selectedRegions - region
            } else {
                selectedRegions + region
            }
        }
    )
}