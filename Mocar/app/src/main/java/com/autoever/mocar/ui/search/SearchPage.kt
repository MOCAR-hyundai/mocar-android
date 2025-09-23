package com.autoever.mocar.ui.search

import android.app.Application
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoever.mocar.R
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchBarViewModel
import com.autoever.mocar.viewmodel.SearchFilterState
import com.autoever.mocar.viewmodel.SearchFilterViewModel
import com.autoever.mocar.viewmodel.SearchManufacturerViewModel
import com.autoever.mocar.viewmodel.SearchUiState
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    navController: NavController,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    searchFilterViewModel: SearchFilterViewModel,
    listingViewModel: ListingViewModel,
    onBack: () -> Unit,
) {
    var selectedMenu by remember { mutableStateOf("제조사") }
    val listings: List<ListingDto> by listingViewModel.listings.collectAsState()
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
    val focusManager = LocalFocusManager.current

    val state by searchFilterViewModel.filterState.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            searchBarViewModel.loadRecentKeywords(userId)
        }
    }

    // 바텀시트 열기
    if (showSheet) {
        SearchHistoryBottomSheet(
            userId = userId,
            searchFilterViewModel = searchFilterViewModel,
            searchManufacturerViewModel = searchManufacturerViewModel,
            onDismiss = { showSheet = false }
        )
    }

    // 필터 개수 계산
    val totalFilteredCount by remember(
        listings,
        state,
        searchManufacturerViewModel.selectedBrand,
        searchManufacturerViewModel.selectedModel,
        searchManufacturerViewModel.selectedSubModels) {
        derivedStateOf {
            getFilteredListings(
                allListings = listings,
                filter = state,
                brand = searchManufacturerViewModel.selectedBrand,
                model = searchManufacturerViewModel.selectedModel,
                subModels = searchManufacturerViewModel.selectedSubModels
            ).size
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isSearchActive) {
                BottomButtons(
                    userId = userId,
                    searchFilterViewModel = searchFilterViewModel,
                    searchManufacturerViewModel = searchManufacturerViewModel,
                    totalFilteredCount = totalFilteredCount,
                    listings = listings,
                    searchBarViewModel = searchBarViewModel
                )
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
                IconButton(
                    onClick = {
                        if (isSearchActive) {
                            focusManager.clearFocus()
                            searchBarViewModel.updateQuery("")
                            searchBarViewModel.deactivateSearch()
                        } else {
                        searchManufacturerViewModel.clearAll()
                        searchFilterViewModel.clearAll()
                        onBack()
                    }
                              },
                    modifier = Modifier.size(38.dp)) {
                    Icon(painterResource(id = R.drawable.ic_back), contentDescription = "뒤로",
                        modifier = Modifier.size(18.dp), tint = Color.Black)
                }

                Spacer(modifier = Modifier.width(8.dp))

                SearchBar(
                    value = searchState.query,
                    onValueChange = { searchBarViewModel.updateQuery(it) },
                    onClick = { searchBarViewModel.activateSearch() },  // 클릭 시 전체화면 전환
                )
            }

            if (isSearchActive) {
                val focusManager = LocalFocusManager.current
                SearchFullScreen(
                    searchState = searchState,
                    onQueryChange = { searchBarViewModel.updateQuery(it) },
                    onBack = {
                        focusManager.clearFocus()
                        searchBarViewModel.deactivateSearch() },
                    onKeywordClick = {
                        searchBarViewModel.updateQuery(it)
                        searchBarViewModel.submitSearch(userId)
                    },
                    onRemoveKeyword = { searchBarViewModel.removeKeyword(it) },
                    onClearAll = { searchBarViewModel.clearAllKeywords(userId) },
                    onSearchSubmit = {
                        searchBarViewModel.submitSearch(userId)
                        focusManager.clearFocus()
                        searchBarViewModel.deactivateSearch()
                                     },
                    onCarClick = {
                        searchBarViewModel.selectCar(it, userId)
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
                    Row(
                        modifier = Modifier
                            .clickable {
                                showSheet = true
                            },
                    ) {
                        Text("최근 검색 기록 ")
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "최근검색기록",
                            tint = Color.Gray
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 4.dp)
                        .drawBehind {
                            val strokeWidth = 0.5.dp.toPx()
                            val color = Color(0xFFD7D7D7)

                            // 상단
                            drawLine(
                                color = color,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = strokeWidth
                            )
                        }
                ) {
                    LeftMenu(
                        selected = selectedMenu,
                        onSelect = { selectedMenu = it },
                        viewModel = searchFilterViewModel,
                        searchManufacturerViewModel =searchManufacturerViewModel
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedMenu) {
                            "제조사" -> Manufacturer(navController, "", searchManufacturerViewModel, listingViewModel)
                            "가격" -> Price(viewModel = searchFilterViewModel)
                            "연식" -> Year(viewModel = searchFilterViewModel)
                            "주행거리" -> Mileage(viewModel = searchFilterViewModel)
                            "차종" -> CarType(listings = listings, viewModel = searchFilterViewModel)
                            "연료" -> Fuel(listings = listings, viewModel = searchFilterViewModel)
                            "지역" -> Region(listings = listings, viewModel = searchFilterViewModel)
                        }
                    }
                }
            }
        }
    }
}

// 검색바
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit = {}
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var hasFocusedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused && !hasFocusedOnce) {
            hasFocusedOnce = true
            onClick()
        }else if (!isFocused && hasFocusedOnce) {
            hasFocusedOnce = false
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            onClick() },

        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            ,
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
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "지우기",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource,
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

// 좌측 메뉴 박스
@Composable
fun LeftMenu(selected: String,
             onSelect: (String) -> Unit,
             viewModel: SearchFilterViewModel = viewModel(),
             searchManufacturerViewModel: SearchManufacturerViewModel
) {
    val state by viewModel.filterState.collectAsState()

    val default = SearchFilterState() // 기본값

    val menuItems = listOf("제조사", "가격", "연식", "주행거리", "차종", "연료", "지역")

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxSize()
            .background(Color(0xFFEDEDED))
            .padding(start = 0.dp)
            .drawBehind {
                val strokeWidth = 0.5.dp.toPx()
                val color = Color(0xFFD7D7D7)
                // 우측
                drawLine(
                    color = color,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            ,
        verticalArrangement = Arrangement.Top,
    ) {
        menuItems.forEach { item ->
            val isChanged = when (item) {
                "제조사"   -> searchManufacturerViewModel.selectedBrand != null
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

// 초기화 및 선택 버튼
@Composable
fun BottomButtons(
    userId: String,
    searchFilterViewModel: SearchFilterViewModel,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    totalFilteredCount: Int,
    listings: List<ListingDto>,
    searchBarViewModel: SearchBarViewModel
) {
    val state by searchFilterViewModel.filterState.collectAsState()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = {
                searchFilterViewModel.clearAll()
                searchManufacturerViewModel.clearAll()
                      },
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
            onClick = {
                searchFilterViewModel.saveSearchHistory(
                    userId = userId, // FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    brand = searchManufacturerViewModel.selectedBrand,
                    model = searchManufacturerViewModel.selectedModel,
                    subModels = searchManufacturerViewModel.selectedSubModels,
                    filterState = searchFilterViewModel.filterState.value
                )
            },
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
                text = "${totalFilteredCount} 대 보기",
                fontSize = 16.sp,
            )
        }
    }
}

// 검색 화면
@Composable
fun SearchFullScreen(
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onKeywordClick: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onClearAll: () -> Unit,
    onSearchSubmit: () -> Unit,
    onCarClick: (ListingDto) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("최근 검색 키워드", fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    text = "전체 삭제",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable { onClearAll() }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (searchState.recentKeywords.isEmpty() && searchState.query.isBlank()) {
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

        if (searchState.query.isNotBlank() && searchState.searchResults.isNotEmpty()) {
            val groupedResults = searchState.searchResults
                .groupBy { it.title }
                .mapNotNull { (title, items) ->
                    if (items.isNotEmpty()) {
                        Triple(title, items.first().year, items)
                    } else null
                }
            LazyColumn {
                itemsIndexed(groupedResults) { index, (title, year, items) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCarClick(items.first()) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(end = 20.dp))
                            Text("$year", fontSize = 13.sp, color = Color.Gray)
                        }
                        Text("${items.size}대", color = Color.Gray)
                    }

                    if (index < groupedResults.lastIndex) {
                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                }
            }

        } else {
            // 검색 결과 없음
            Spacer(modifier = Modifier.height(32.dp))
            Text("검색 결과가 없습니다.", color = Color.Gray)
        }
    }
}

// 필터
fun getFilteredListings(
    allListings: List<ListingDto>,
    filter: SearchFilterState,
    brand: String?,
    model: String?,
    subModels: List<String>
): List<ListingDto> {
    println("[필터링 시작]")
    println("현재 필터 조건:")
    println(" - 브랜드: $brand")
    println(" - 모델: $model")
    println(" - 서브모델: $subModels")
    println(" - 차종: ${filter.selectedTypes}")
    println(" - 연료: ${filter.selectedFuels}")
    println(" - 지역: ${filter.selectedRegions}")
    println(" - 가격: ${filter.priceRange.start} ~ ${filter.priceRange.endInclusive}")
    println(" - 연식: ${filter.yearRange.start} ~ ${filter.yearRange.endInclusive}")
    println(" - 주행거리: ${filter.mileageRange.start} ~ ${filter.mileageRange.endInclusive}")

    val filtered = allListings.filter { car ->
        val carType = car.carType?.lowercase()?.trim() ?: ""
        val fuel = car.fuel?.lowercase()?.trim() ?: ""
        val region = car.region?.lowercase()?.trim() ?: ""
        val brandName = car.brand?.lowercase()?.trim() ?: ""
        val modelName = car.model?.lowercase()?.trim() ?: ""
        val titleName = car.title?.lowercase()?.trim() ?: ""

        val brandMatches = brand == null || brand.lowercase().trim() == brandName
        val modelMatches = model == null || model.lowercase().trim() == modelName
        val subModelMatches = subModels.isEmpty() || subModels.map { it.lowercase().trim() }.contains(titleName)

        val priceMatches = ((car.price?.toFloat() ?: 0f) / 10000f) in filter.priceRange
        val yearMatches = (car.year?.toFloat() ?: 0f) in filter.yearRange
        val mileageMatches = (car.mileage?.toFloat() ?: 0f) in filter.mileageRange

        val typeMatches = filter.selectedTypes.isEmpty() || filter.selectedTypes.any {
            carType.contains(it.lowercase().trim())
        }

        val fuelMatches = filter.selectedFuels.isEmpty() || filter.selectedFuels.any {
            fuel.contains(it.lowercase().trim())
        }

        val regionMatches = filter.selectedRegions.isEmpty() || filter.selectedRegions.any {
            region.contains(it.lowercase().trim())
        }

        brandMatches &&
                modelMatches &&
                subModelMatches &&
                priceMatches &&
                yearMatches &&
                mileageMatches &&
                typeMatches &&
                fuelMatches &&
                regionMatches
    }

    println("필터링 결과: ${filtered.size}대")

    return filtered
}

// 최근 검색기록
@Composable
fun SearchHistoryScreen(
    searchFilterViewModel: SearchFilterViewModel,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    userId: String,
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
                    .padding(horizontal = 25.dp)
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
                            .background(Color(0xFFF4F4F4), RoundedCornerShape(12.dp))
                            .clickable {
                                searchFilterViewModel.restoreFrom(item)
                                searchManufacturerViewModel.restoreFrom(item)
                                onBack()
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
                                imageVector = Icons.Default.Close,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHistoryBottomSheet(
    userId: String,
    searchFilterViewModel: SearchFilterViewModel,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = Color(0xFFF8F8F8),
        modifier = Modifier.fillMaxHeight() // 최대한 채우기
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            SearchHistoryScreen(
                userId = userId,
                searchFilterViewModel = searchFilterViewModel,
                searchManufacturerViewModel = searchManufacturerViewModel,
                onBack = onDismiss
            )
        }
    }
}
