package com.autoever.mocar.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.autoever.mocar.R

data class carDatas(
    val name: String,
    val count: Int,
    val imageRes: String,
    val isKorean: Boolean
)

fun isKoreanBrand(brand: String): Boolean {
    return brand.any { it in '가'..'힣' } // 한글 포함 여부로 판단
}

fun String.isKorean(): Boolean {
    return this.any { it in '가'..'힣' }
}

fun List<ListingData>.toBrandMap(): List<carDatas> {
    return this.groupBy { it.brand }
        .map { (brand, items) ->
            val count = items.size
            val imageUrl = items.firstOrNull()?.images?.firstOrNull().orEmpty() // ✅ 올바르게 수정
            carDatas(
                name = brand,
                count = count,
                imageRes = imageUrl,
                isKorean = brand.isKorean()
            )
        }
}


@Composable
fun Manufacturer(navController: NavController, searchQuery: String, viewModel: ListingViewModel = viewModel()) {

    val listings by viewModel.listings.collectAsState()
    val allBrands = listings.toBrandMap()

    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var showModelSheet by remember { mutableStateOf(false) }

    // ✅ BottomSheet 표시 여부
    if (showModelSheet && selectedBrand != null) {
        ModelSelectBottomSheet(
            brandName = selectedBrand!!,
            allListings = listings,
            onDismiss = { showModelSheet = false },
            onConfirm = {
                selectedModel = it.firstOrNull()
                showModelSheet = false
                println("선택된 모델: $selectedModel")
            }
        )
    }
    if (selectedBrand != null) {
        // 제조사 + 모델 선택 완료 상태 → 배경 변경 + 요약 박스 표시
        SelectedFilterSummary(
            brand = selectedBrand!!,
            model = selectedModel.orEmpty(),
            onBrandClear = {
                selectedBrand = null
                selectedModel = null
            },
            onModelClear = {
                selectedModel = null
            },
            onModelClick = {
                showModelSheet = true
            }
        )
    } else {
        // 제조사 리스트 보여주기
        val koreanBrands = allBrands.filter { it.isKorean }
        val foreignBrands = allBrands.filterNot { it.isKorean }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            item { CategoryLabel("국산차") }
            items(koreanBrands.size) { index ->
                val (name, count, imageUrl) = koreanBrands[index]
                ManufacturerCard(name, count, imageUrl) {
                    selectedBrand = name
                    showModelSheet = true
                }
                if (index < koreanBrands.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 0.7.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            item { CategoryLabel("수입차") }
            items(foreignBrands.size) { index ->
                val (name, count, imageUrl) = foreignBrands[index]
                ManufacturerCard(name, count, imageUrl) {
                    selectedBrand = name
                    showModelSheet = true
                }
                if (index < foreignBrands.size - 1) {
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
fun CategoryLabel(text: String) {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .wrapContentSize()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(50) // 양 끝 둥글게
            )
            .border(
                width = 0.5.dp,
                color = Color(0xFFD7D7D7),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp), // 내부 여백 (글자보다 조금 크게)
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun ManufacturerCard(
    name: String,
    count: Int,
    imageRes: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 로고 이미지 + 이름
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
//                AsyncImage(
//                    model = imageRes,
//                    contentDescription = "$name 로고",
//                    modifier = Modifier
//                        .height(30.dp)
//                        .width(70.dp)
//                        .padding(end = 4.dp),
//                    contentScale = ContentScale.Fit
//                )
//                if (imageRes.isNotBlank()) {
//                    AsyncImage(
//                        model = imageRes,
//                        contentDescription = "$name 로고",
//                        modifier = Modifier
//                            .height(30.dp)
//                            .width(70.dp)
//                            .padding(end = 4.dp),
//                        contentScale = ContentScale.Fit
//                    )
//                } else {
//                    Image(
//                        painter = painterResource(id = R.drawable.brand_hyundai),
//                        contentDescription = "기본 로고",
//                        modifier = Modifier
//                            .height(30.dp)
//                            .width(70.dp)
//                            .padding(end = 4.dp),
//                        contentScale = ContentScale.Fit
//                    )
//                }
                Image(
                    painter = painterResource(id = R.drawable.brand_hyundai),
                    contentDescription = "기본 로고",
                    modifier = Modifier
                        .height(30.dp)
                        .width(70.dp)
                        .padding(end = 4.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 개수 + 화살표
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%,d".format(count),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "다음",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}


@Composable
fun SelectedFilterSummary(
    brand: String,
    model: String,
    onBrandClear: () -> Unit,
    onModelClear: () -> Unit,
    onModelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )

        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 왼쪽: 라벨
                    Text("제조사", fontWeight = FontWeight.Bold)

                    // 오른쪽: 회색 박스 (텍스트 + X 버튼) + → 아이콘
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFEDEDED), RoundedCornerShape(25))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(brand, fontSize = 14.sp)

                            IconButton(
                                onClick = onBrandClear,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = "제조사 초기화",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onBrandClear,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "제조사 다시 선택",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 왼쪽 텍스트
                    Text("모델", fontWeight = FontWeight.Bold)

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(model != "") {
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFEDEDED), RoundedCornerShape(25))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(model, fontSize = 14.sp)

                                IconButton(
                                    onClick = onModelClear,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = "모델 초기화",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        else {
                            Text("선택해 주세요.", fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onModelClick,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "모델 다시 선택",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
               }
            }
        }

    }
}



