package com.autoever.mocar.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.autoever.mocar.R

@Composable
fun ModelSelect(
    brandName: String,
    allListings: List<ListingData>,
    onBack: () -> Unit = {},
    onConfirm: (List<String>) -> Unit = {}
) {
//    val exampleModels = listOf(
//        "그랜저" to 8354,
//        "포터" to 6911,
//        "아반떼" to 5543,
//        "쏘나타" to 4625,
//        "싼타페" to 4272,
//        "i30" to 363,
//        "i40" to 254,
//        "ST1" to 3,
//        "갤로퍼" to 44,
//        "그라나다" to 0,
//    )

    val filteredModels = allListings
        .filter { it.brand == brandName }
        .groupBy { it.model }
        .map { (model, items) -> model to items.size }
        .sortedByDescending { it.second } // 개수 기준 정렬 (선택사항)


    var selectedModel by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(38.dp)) {
                Icon(painterResource(id = R.drawable.ic_back), contentDescription = "뒤로",
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

        // 전체 모델 리스트

//        LazyColumn () {
//            items(exampleModels.size) { index ->
//                val (model, count) = exampleModels[index]
//                ModelRow(
//                    name = model,
//                    count = count,
//                    selected = selectedModel == model,
//                    onSelect = {
//                        selectedModel = model
//                    }
//                )
//                if (index < exampleModels.size - 1) {
//                    HorizontalDivider(
//                        color = Color(0xFFE0E0E0),
//                        thickness = 0.7.dp,
//                        modifier = Modifier.padding(horizontal = 8.dp)
//                    )
//                }
//            }
//        }
        LazyColumn {
            items(filteredModels.size) { index ->
                val (model, count) = filteredModels[index]
                ModelRow(
                    name = model,
                    count = count,
                    selected = selectedModel == model,
                    onSelect = {
                        selectedModel = model
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


        Spacer(modifier = Modifier.weight(1f))

        // 선택 버튼
        Button(
            onClick = {
                if (selectedModel != null) {
                    onConfirm(listOf(selectedModel!!))
                } else {
                    onConfirm(listOf("전체")) // 아무것도 선택 안 했으면 "전체"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("선택")
        }
    }
}

@Composable
fun ModelRow(
    name: String,
    count: Int,
    selected: Boolean,
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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                color = contentColor
            )

            Text(
                text = "%,d".format(count),
                fontSize = 14.sp,
                color = contentColor
            )

            if (selected && isEnabled) {
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

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectBottomSheet(
    brandName: String,
    allListings: List<ListingData>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = Color(0xFFF8F8F8)
    ) {
        ModelSelect(
            brandName = brandName,
            allListings = allListings,
            onBack = onDismiss,
            onConfirm = {
                onConfirm(it)
                onDismiss()
            }
        )
    }
}
