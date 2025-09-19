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
import com.autoever.mocar.viewmodel.ListingData
import com.autoever.mocar.viewmodel.SearchFilterViewModel
import kotlin.collections.component1
import kotlin.collections.component2

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
fun CarType(listings: List<ListingData>, viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()

    val carTypes = listings
        .groupBy { it.carType ?: "--" }
        .map { (type, items) -> SelectableItem(type, items.size) }
        .sortedByDescending { it.count }

    CheckList(
        title = "차종",
        items = carTypes,
        selectedItems = state.selectedTypes,
        onToggle = { viewModel.toggleType(it) }
    )
}

@Composable
fun Fuel(listings: List<ListingData>, viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()

    val fuels = listings
        .groupBy { it.fuel }
        .map { (type, items) -> SelectableItem(type, items.size) }
        .sortedByDescending { it.count }

    CheckList(
        title = "연료",
        items = fuels,
        selectedItems = state.selectedFuels,
        onToggle = { viewModel.toggleFuel(it) }
    )
}

@Composable
fun Region(listings: List<ListingData>, viewModel: SearchFilterViewModel = viewModel()) {
    val state by viewModel.filterState.collectAsState()

    val regions = listings
        .groupBy { it.region }
        .map { (type, items) -> SelectableItem(type, items.size) }
        .sortedByDescending { it.count }

    CheckList(
        title = "지역",
        items = regions,
        selectedItems = state.selectedRegions,
        onToggle = { viewModel.toggleRegion(it) }
    )
}

fun List<ListingData>.toSelectableMap(keySelector: (ListingData) -> String): List<SelectableItem> {
    return this.groupBy(keySelector).map { (key, items) ->
        SelectableItem(name = key, count = items.size)
    }
}

fun List<ListingData>.toModelMapPerBrand(): Map<String, List<SelectableItem>> {
    return this.groupBy { it.brand }
        .mapValues { (_, brandItems) ->
            brandItems.groupBy { it.model }
                .map { (model, items) ->
                    SelectableItem(model, items.size)
                }
        }
}
