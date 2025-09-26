package com.autoever.mocar.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoever.mocar.R
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchFilterState
import com.autoever.mocar.viewmodel.SearchFilterViewModel
import com.autoever.mocar.viewmodel.SearchManufacturerViewModel
import com.autoever.mocar.viewmodel.SearchResultViewModel


// 최근 검색기록
@Composable
fun SearchHistoryScreen(
    navController: NavController,
    userId: String,
    listingViewModel: ListingViewModel,
    searchFilterViewModel: SearchFilterViewModel,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    searchResultViewModel: SearchResultViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        searchFilterViewModel.loadSearchHistory(userId)
    }
    val history by searchFilterViewModel.filterHistory.collectAsState()
    val default = SearchFilterState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text("최근 검색 기록", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.padding(end = 20.dp)
            ) {
                Text(
                    text = "전체 삭제",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable { searchFilterViewModel.clearAllHistory(userId)
                        }
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("최근 검색 기록이 없습니다.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
            ) {
                itemsIndexed(history) { index, item ->
                    val displayFilters = mutableListOf<String>()

                    if (item.subModels.isNotEmpty())
                        displayFilters.add("세부모델: ${item.subModels.joinToString(", ")}")
                    if (item.priceRange != default.priceRange)
                        displayFilters.add("가격: ${item.priceRange.start.toInt()}~${item.priceRange.endInclusive.toInt()}만원")
                    if (item.yearRange != default.yearRange)
                        displayFilters.add("연식: ${item.yearRange.start.toInt()}~${item.yearRange.endInclusive.toInt()}년")
                    if (item.mileageRange != default.mileageRange)
                        displayFilters.add("주행: ${item.mileageRange.start.toInt()}~${item.mileageRange.endInclusive.toInt()}km")
                    if (item.selectedTypes.isNotEmpty())
                        displayFilters.add("차종: ${item.selectedTypes.joinToString(", ")}")
                    if (item.selectedFuels.isNotEmpty())
                        displayFilters.add("연료: ${item.selectedFuels.joinToString(", ")}")
                    if (item.selectedRegions.isNotEmpty())
                        displayFilters.add("지역: ${item.selectedRegions.joinToString(", ")}")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0xFFE9ECEF), RoundedCornerShape(12.dp))
                            .clickable {
                                // 1. 저장된 검색 기록 상태 복원
                                searchFilterViewModel.restoreFrom(item)
                                searchManufacturerViewModel.restoreFrom(item)

                                // 2. 전체 매물 기준으로 필터 적용
                                val filteredListings = getFilteredListings(
                                    allListings = listingViewModel.listings.value,
                                    filter = searchFilterViewModel.filterState.value,
                                    brand = searchManufacturerViewModel.selectedBrand,
                                    model = searchManufacturerViewModel.selectedModel,
                                    subModels = searchManufacturerViewModel.selectedSubModels
                                )

                                // 3. 결과 뷰모델에 저장
                                searchResultViewModel.setResults(filteredListings)

                                // 4. 현재 필터 상태 저장
                                searchFilterViewModel.setFilterParamsFromCurrentState(
                                    brand = searchManufacturerViewModel.selectedBrand,
                                    model = searchManufacturerViewModel.selectedModel,
                                    subModels = searchManufacturerViewModel.selectedSubModels,
                                    filterState = searchFilterViewModel.filterState.value
                                )

                                // 5. 검색 결과 화면으로 이동
                                navController.navigate("result")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    buildString {
                                        if (!item.brand.isNullOrBlank()) append("제조사: ${item.brand}") else append("제조사: 전체")
                                        if (!item.model.isNullOrBlank()) append(" / 모델: ${item.model}")
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )

                                if (displayFilters.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    displayFilters.forEach {
                                        Text(text = it, fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                val mutable = history.toMutableList()
                                mutable.removeAt(index)
                                searchFilterViewModel.deleteEachHistory(item)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)

                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "삭제",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}