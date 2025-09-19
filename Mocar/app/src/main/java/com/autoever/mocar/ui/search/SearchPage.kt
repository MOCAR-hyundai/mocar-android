package com.autoever.mocar.ui.search

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoever.mocar.R
import com.autoever.mocar.viewmodel.ListingData
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchBarViewModel
import com.autoever.mocar.viewmodel.SearchFilterState
import com.autoever.mocar.viewmodel.SearchFilterViewModel
import com.autoever.mocar.viewmodel.SearchUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    navController: NavController,
    onBack: () -> Unit,
    filterViewModel: SearchFilterViewModel = viewModel()
) {
    var selectedMenu by remember { mutableStateOf("제조사") }
    val listingViewModel: ListingViewModel = viewModel()
    val listings by listingViewModel.listings.collectAsState()
    val context = LocalContext.current.applicationContext as Application

    val searchBarViewModel: SearchBarViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchBarViewModel(listingViewModel, context) as T
            }
        }
    )

    val searchState by searchBarViewModel.uiState.collectAsState()
    val isSearchActive by searchBarViewModel.isSearchActive.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isSearchActive) {
                BottomButtons()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(innerPadding)
        ) {
            // 상단 검색창
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                    Icon(painterResource(id = R.drawable.ic_back), contentDescription = "뒤로",
                        modifier = Modifier.size(18.dp), tint = Color.Black)
                }

                Spacer(modifier = Modifier.width(8.dp))

                SearchBar(
                    value = searchState.query,
                    onValueChange = { searchBarViewModel.updateQuery(it) },
                    onClick = { searchBarViewModel.activateSearch() },  // 🔁 클릭 시 전체화면 전환
                )
            }

            // 이 부분에 조건문 넣기!
            if (isSearchActive) {
                SearchFullScreen(
                    searchState = searchState,
                    onQueryChange = { searchBarViewModel.updateQuery(it) },
                    onBack = { searchBarViewModel.deactivateSearch() },
                    onKeywordClick = { searchBarViewModel.updateQuery(it) },
                    onRemoveKeyword = { searchBarViewModel.removeKeyword(it) },
                    onClearAll = { searchBarViewModel.clearAllKeywords() },
                    onSearchSubmit = { searchBarViewModel.submitSearch() },
                    onCarClick = {
                        searchBarViewModel.selectCar(it)
                        searchBarViewModel.deactivateSearch()
                        navController.popBackStack()
                    }
                )
            } else {
                // 최근 검색 기록
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("최근 검색기록 ")
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "최근검색기록",
                        tint = Color.Gray
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(0.5.dp, Color(0xFFD7D7D7))
                ) {
                    LeftMenu(
                        selected = selectedMenu,
                        onSelect = { selectedMenu = it },
                        viewModel = filterViewModel
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedMenu) {
                            "제조사" -> Manufacturer(navController, "")
                            "가격" -> Price(viewModel = filterViewModel)
                            "연식" -> Year(viewModel = filterViewModel)
                            "주행거리" -> Mileage(viewModel = filterViewModel)
                            "차종" -> CarType(listings = listings, viewModel = filterViewModel)
                            "연료" -> Fuel(listings = listings, viewModel = filterViewModel)
                            "지역" -> Region(listings = listings, viewModel = filterViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit = {}
) {
    var hasClicked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable {
                onClick()
                hasClicked = true
            },
        contentAlignment = Alignment.CenterStart
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (!hasClicked) onClick() // 첫 입력 시도 시에도 SearchFullScreen
                onValueChange(it)
            },
            modifier = Modifier
                .fillMaxSize(),
            singleLine = true,
            placeholder = { Text("제조사, 모델을 검색해보세요") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color(0xFF6B7280)
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3058EF),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color(0xFF111827),
                unfocusedTextColor = Color(0xFF111827),
                focusedPlaceholderColor = Color(0xFF9CA3AF),
                unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                cursorColor = Color(0xFF2A5BFF)
            )
        )
    }
}


@Composable
fun LeftMenu(selected: String,
             onSelect: (String) -> Unit,
             viewModel: SearchFilterViewModel = viewModel()
) {
    val state by viewModel.filterState.collectAsState()
    val default = SearchFilterState() // 기본값

    val menuItems = listOf("제조사", "가격", "연식", "주행거리", "차종", "연료", "지역")

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxSize()
            .padding(start = 0.dp)
            .border(
                width = 0.5.dp,
                color = Color(0xFFD7D7D7),
                shape = RectangleShape
            ),
        verticalArrangement = Arrangement.Top,
    ) {
        menuItems.forEach { item ->
            val isChanged = when (item) {
                "가격"     -> state.priceRange != default.priceRange
                "연식"     -> state.yearRange != default.yearRange
                "주행거리" -> state.mileageRange != default.mileageRange
                "차종"     -> state.selectedTypes.isNotEmpty()
                "연료"     -> state.selectedFuels.isNotEmpty()
                "지역"     -> state.selectedRegions.isNotEmpty()
                else       -> false
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(if (selected == item) Color.White else Color(0xFFEDEDED))
                    .clickable { onSelect(item) },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 12.dp),
                        color = if (selected == item) Color(0xFF3058EF) else Color.Black,
                        fontWeight = if (selected == item) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    )

                    if (isChanged) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                                .background(Color.Red, shape = RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

// 버튼
@Composable
fun BottomButtons() {
    val viewModel: SearchFilterViewModel = viewModel()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { viewModel.resetAllFilters() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
        ) {
            Text(text = "초기화",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = { /* 차량 보기 */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(2f)
                .height(60.dp)
        ) {
            Text(
                text = "선택",
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun SearchFullScreen(
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onKeywordClick: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onClearAll: () -> Unit,
    onSearchSubmit: () -> Unit,
    onCarClick: (ListingData) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("최근 검색 키워드", fontWeight = FontWeight.SemiBold)
            Text(
                text = "전체 삭제",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onClearAll() }
                    .padding(end = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (searchState.recentKeywords.isEmpty() && searchState.query.isBlank()) {
                // 검색어가 없을 때 안내문구
            Text(
                "키워드 검색하고, 원하는 차량을 찾아보세요",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        LazyRow {
            items(searchState.recentKeywords) { keyword ->
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = keyword,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                onKeywordClick(keyword)
                                onSearchSubmit()
                            }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onRemoveKeyword(keyword) }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (searchState.searchResults.isNotEmpty()) {
            LazyColumn {
                items(searchState.searchResults) { listing ->
                    Column(Modifier
                        .padding(vertical = 12.dp)
                        .clickable { onCarClick(listing) }) {
                        Text("${listing.brand} ${listing.model}", fontWeight = FontWeight.Bold)
                        Text("연료: ${listing.fuel}  |  차종: ${listing.carType}", color = Color.Gray)
                    }
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                }
            }
        } else {
            // 검색 결과 없음
            Spacer(modifier = Modifier.height(32.dp))
            Text("검색 결과가 없습니다.", color = Color.Gray)
        }
    }
}
