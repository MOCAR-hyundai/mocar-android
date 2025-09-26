package com.autoever.mocar.ui.search

import ROUTE_SEARCH
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoever.mocar.R
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchManufacturerViewModel

// 모델
@Composable
fun ModelSelect(
    brandName: String,
    allListings: List<ListingDto>,
    navController: NavController,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    listingViewModel: ListingViewModel,
    onBack: () -> Unit = {},
) {
    val filteredModels = allListings
        .filter { it.brand == brandName }
        .groupBy { it.model }
        .map { (model, items) -> model to items.size }
        .sortedByDescending { it.second } // 개수 기준 정렬 (선택사항)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .navigationBarsPadding()
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(38.dp)) {
                Icon(painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(text = brandName, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 경로 텍스트
        Text("제조사 > 모델", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredModels.size) { index ->
                val (model, count) = filteredModels[index]
                ModelRow(
                    name = model,
                    count = count,
                    onSelect = {
                        navController.navigate("sub_model_select/${brandName}/${model}")
                    }
                )
                if (index < filteredModels.size - 1) {
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
fun ModelRow(
    name: String,
    count: Int,
    onSelect: () -> Unit
) {
    val isEnabled = count > 0
    val contentColor = if (isEnabled) Color.Black else Color(0xFFBDBDBD) // 비활성 텍스트 색

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isEnabled)
                    Modifier.clickable { onSelect() }
                else
                    Modifier  // 아무 처리 안 함 (클릭 비활성)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .height(30.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                color = contentColor,
            )

            Text(
                text = "%,d".format(count),
                fontSize = 14.sp,
                color = contentColor
            )
        }

    }
}

// 서브 모델
@Composable
fun SubModelSelect(
    navController: NavController,
    brandName: String,
    modelName: String,
    allListings: List<ListingDto>,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    onBack: () -> Unit = {},
    onConfirm: (List<ListingDto>) -> Unit = {}
) {
    val subModels = allListings
        .filter { it.brand == brandName && it.model == modelName }
        .groupBy { it.title }
        .map { (title, list) ->
            Triple(title, list.firstOrNull()?.year ?: 0, list)
        }

    val selectedItems = remember { mutableStateListOf<ListingDto>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .navigationBarsPadding(),
    ) {
        // 상단 경로 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(38.dp)) {
                androidx.compose.material3.Icon(
                    painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(text = modelName, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 경로 텍스트
        Text("제조사 > 모델 > 세부모델", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn (
            modifier = Modifier.weight(1f)
        ) {
            items(subModels.size) { index ->
                val (title, year, list) = subModels[index]
                val isSelected = selectedItems.containsAll(list)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedItems.removeAll(list)
                            else selectedItems.addAll(list)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 16.sp)
                        Text("$year", fontSize = 13.sp, color = Color.Gray)
                    }

                    Text("${list.size}대", color = Color.Gray)

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3058EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "선택됨",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                if (index < subModels.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 0.7.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                searchManufacturerViewModel.selectedBrand = brandName
                searchManufacturerViewModel.selectedModel = modelName
                searchManufacturerViewModel.selectedSubModels.clear()
                searchManufacturerViewModel.selectedSubModels.addAll(selectedItems.map { it.title }.distinct())

                navController.navigate(ROUTE_SEARCH) {
                    popUpTo("sub_model_select/$brandName/$modelName") { inclusive = true }
                }
            },
            enabled = selectedItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("선택 완료 (${selectedItems.size})")
        }
    }
}