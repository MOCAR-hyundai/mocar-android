package com.autoever.mocar.ui.search

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.autoever.mocar.data.brands.BrandDto
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.viewmodel.BrandViewModel
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchManufacturerViewModel

data class carDatas(
    val name: String,
    val count: Int,
    val imageRes: String?,
    val isKorean: Boolean
)

fun List<ListingDto>.toBrandMap(brandDtos: List<BrandDto>): List<carDatas> {
    return this.groupBy { it.brand }
        .mapNotNull { (brandName, items) ->
            val matchedBrand = brandDtos.find { it.name == brandName }
            if (matchedBrand != null) {
                carDatas(
                    name = brandName,
                    count = items.size,
                    imageRes = matchedBrand.logoUrl,
                    isKorean = matchedBrand.countryType == "domestic"
                )
            } else null
        }
}


@Composable
fun Manufacturer(navController: NavController,
                 searchQuery: String,
                 searchManufacturerViewModel: SearchManufacturerViewModel,
                 listingViewModel: ListingViewModel,
                 brandViewModel: BrandViewModel = viewModel()
){
    val uiState by listingViewModel.uiState.collectAsState()
    val listings by listingViewModel.listings.collectAsState()
    val brandDtos by brandViewModel.brands.collectAsState()

    val allBrands = listings.toBrandMap(brandDtos)
    val domesticBrands = allBrands.filter { it.isKorean }
    val foreignBrands = allBrands.filter { !it.isKorean }

    val selectedBrand = searchManufacturerViewModel.selectedBrand
    val selectedModel = searchManufacturerViewModel.selectedModel
    val selectedSubModels = searchManufacturerViewModel.selectedSubModels

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("로딩 중…")
        }
    } else {
        if (selectedBrand != null) {
            // 제조사 + 모델 선택 완료 상태 → 배경 변경 + 요약 박스 표시
            SelectedFilterSummary(
                brand = selectedBrand,
                model = selectedModel.orEmpty(),
                subModels = selectedSubModels,
                onBrandClear = {
                    searchManufacturerViewModel.selectedBrand = null
                    searchManufacturerViewModel.selectedModel = null
                    searchManufacturerViewModel.selectedSubModels.clear()
                },
                onModelClear = {
                    searchManufacturerViewModel.selectedModel = null
                    searchManufacturerViewModel.selectedSubModels.clear()
                },
                onSubModelClear = {subModelToRemove ->
                    searchManufacturerViewModel.selectedSubModels.remove(subModelToRemove)
                },
                onModelClick = {
                    navController.navigate("model_select/${selectedBrand}")
                },

                onSubModelClick = {
                    navController.navigate("sub_model_select/${selectedBrand}/${selectedModel}")
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item { CategoryLabel("국산차") }
                items(domesticBrands.size) { index ->
                    val (name, count, imageUrl) = domesticBrands[index]
                    ManufacturerCard(name, count, imageUrl) {
                        searchManufacturerViewModel.isTransitionLoading = true
                        navController.navigate("model_select/$name")
                        searchManufacturerViewModel.selectedBrand = name
                    }
                    if (index < domesticBrands.size - 1) {
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
                        searchManufacturerViewModel.isTransitionLoading = true
                        navController.navigate("model_select/$name")
                        searchManufacturerViewModel.selectedBrand = name
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
}

// 카테고리 레이블 박스
@Composable
fun CategoryLabel(text: String) {
    Box(
        modifier = Modifier
            .padding(10.dp)
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
    imageRes: String?,
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
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageRes,
                    contentDescription = "$name 로고",
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


// 모델, 서브 모델 선택 결과
@Composable
fun SelectedFilterSummary(
    brand: String,
    model: String,
    subModels: List<String>,
    onBrandClear: () -> Unit,
    onModelClear: () -> Unit,
    onSubModelClear: (String) -> Unit,
    onModelClick: () -> Unit,
    onSubModelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 제조사
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("제조사", fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextWithClearBox(brand, onClear = onBrandClear)
                        Spacer(modifier = Modifier.width(8.dp))
                        ArrowIcon(onClick = onBrandClear)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(onClick = onModelClick),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("모델", fontWeight = FontWeight.Bold)

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        if (model.isNotBlank()) {
                            ModelBox(
                                text = model,
                                onDelete = onModelClear,
                            )
                        } else {
                            Text(
                                "선택해 주세요.",
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ArrowIcon(onClick = onModelClick)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 세부모델 라벨 + 화살표
                if (model.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable(onClick = onSubModelClick),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("세부모델", fontWeight = FontWeight.Bold)
                        Row() {
                            if (subModels.isEmpty()) {
                                Text(
                                    "선택해 주세요.",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            ArrowIcon(onClick = onSubModelClick)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (subModels.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            subModels.forEach { subModel ->
                                ModelBox(text = subModel) {
                                    onSubModelClear(subModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelBox(text: String, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEDEDED), RoundedCornerShape(25))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 20.dp), // 아이콘 공간 확보
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, fontSize = 14.sp)
        }

        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = "삭제",
            tint = Color.Gray,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(16.dp)
                .clickable { onDelete() }
        )
    }
}

@Composable
fun TextWithClearBox(text: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFFEDEDED), RoundedCornerShape(25))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 14.sp)
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "삭제",
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ArrowIcon(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(18.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "이동",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}
