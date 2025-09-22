import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import androidx.navigation.NavController
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.ui.search.getFilteredListings
import com.autoever.mocar.viewmodel.ListingViewModel
import com.autoever.mocar.viewmodel.SearchFilterViewModel
import com.autoever.mocar.viewmodel.SearchManufacturerViewModel

// 데이터 모델
data class Car(
    val id: String,
    val imageUrl: String,
    val trim: String,
    val year: Int,
    val mileage: Int,
    val fuelType: String,
    val region: String,
    val price: Int,
    var isFavorite: Boolean = false
)

data class CarFilter(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minMileage: Int? = null,
    val maxMileage: Int? = null,
    val fuelType: String? = null,
    val region: String? = null
)

// 메인 화면
@Composable
fun SearchResultScreen(
    cars: List<Car>,
    filter: CarFilter = CarFilter(),
    onCarClick: (Car) -> Unit = {},
    onFavoriteClick: (Car) -> Unit = {}
) {
    var appliedFilter by remember { mutableStateOf(filter) }

    fun applyFilter(list: List<Car>, filter: CarFilter): List<Car> {
        return list.filter { car ->
            (filter.minPrice == null || car.price >= filter.minPrice) &&
                    (filter.maxPrice == null || car.price <= filter.maxPrice) &&
                    (filter.minYear == null || car.year >= filter.minYear) &&
                    (filter.maxYear == null || car.year <= filter.maxYear) &&
                    (filter.minMileage == null || car.mileage >= filter.minMileage) &&
                    (filter.maxMileage == null || car.mileage <= filter.maxMileage) &&
                    (filter.fuelType == null || car.fuelType == filter.fuelType) &&
                    (filter.region == null || car.region == filter.region)
        }
    }

    val filteredCars = applyFilter(cars, appliedFilter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        FilterRowSection(
            filter = appliedFilter,
            onFilterChange = { newFilter -> appliedFilter = newFilter }
        )

        if (filteredCars.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("검색 결과가 없습니다.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val carPairs = filteredCars.chunked(2)
                items(carPairs.size) { idx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CarCardVertical(
                            car = carPairs[idx][0],
                            onClick = { onCarClick(carPairs[idx][0]) },
                            onFavoriteClick = {
                                carPairs[idx][0].isFavorite = !carPairs[idx][0].isFavorite
                                onFavoriteClick(carPairs[idx][0])
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (carPairs[idx].size > 1) {
                            CarCardVertical(
                                car = carPairs[idx][1],
                                onClick = { onCarClick(carPairs[idx][1]) },
                                onFavoriteClick = {
                                    carPairs[idx][1].isFavorite = !carPairs[idx][1].isFavorite
                                    onFavoriteClick(carPairs[idx][1])
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultPage(
    navController: NavController,
    searchManufacturerViewModel: SearchManufacturerViewModel,
    searchFilterViewModel: SearchFilterViewModel,
    listingViewModel: ListingViewModel,
    onBack: () -> Unit
) {
    val listings: List<ListingDto> by listingViewModel.listings.collectAsState()
    val state by searchFilterViewModel.filterState.collectAsState()

    val filteredListings by remember(
        listings,
        state,
        searchManufacturerViewModel.selectedBrand,
        searchManufacturerViewModel.selectedModel,
        searchManufacturerViewModel.selectedSubModels
    ) {
        derivedStateOf {
            getFilteredListings(
                allListings = listings,
                filter = state,
                brand = searchManufacturerViewModel.selectedBrand,
                model = searchManufacturerViewModel.selectedModel,
                subModels = searchManufacturerViewModel.selectedSubModels
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("검색 결과 (${filteredListings.size}대)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (filteredListings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("검색 결과가 없습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredListings.size) { index ->
                    val listing = filteredListings[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("carDetail/${listing.listingId}") }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${listing.brand} ${listing.model} ${listing.trim}", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("${listing.year}년 · ${listing.mileage}km · ${listing.fuel}", color = Color.Gray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                    if (index < filteredListings.lastIndex) {
                        Divider(color = Color(0xFFE0E0E0))
                    }
                }
            }
        }
    }
}

// 필터 Row
@Composable
fun FilterRowSection(
    filter: CarFilter,
    onFilterChange: (CarFilter) -> Unit
) {
    var minPrice by remember { mutableStateOf(filter.minPrice ?: 1000) }
    var maxPrice by remember { mutableStateOf(filter.maxPrice ?: 4000) }
    var minYear by remember { mutableStateOf(filter.minYear ?: 2015) }
    var maxYear by remember { mutableStateOf(filter.maxYear ?: 2023) }
    var minMileage by remember { mutableStateOf(filter.minMileage ?: 0) }
    var maxMileage by remember { mutableStateOf(filter.maxMileage ?: 50000) }
    var selectedFuelType by remember { mutableStateOf(filter.fuelType ?: "전체") }
    var selectedRegion by remember { mutableStateOf(filter.region ?: "전체") }

    var showPriceDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }
    var showMileageDialog by remember { mutableStateOf(false) }
    var showFuelDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }

    val fuelTypes = listOf("전체", "가솔린", "디젤", "하이브리드", "전기", "LPG")
    val regions = listOf("전체", "서울", "경기", "인천", "부산", "대전", "울산", "대구")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterLabelButton("가격") { showPriceDialog = true }
        FilterLabelButton("연식") { showYearDialog = true }
        FilterLabelButton("주행") { showMileageDialog = true }
        FilterLabelButton("연료") { showFuelDialog = true }
        FilterLabelButton("지역") { showRegionDialog = true }
    }

    // 가격 모달
    if (showPriceDialog) {
        FilterRangeModal(
            title = "가격",
            unit = "만원",
            valueRange = 500f..5000f,
            steps = (5000 - 500) / 100 - 1,
            currentMin = minPrice.toFloat(),
            currentMax = maxPrice.toFloat(),
            onDismiss = { showPriceDialog = false },
            onApply = { min, max ->
                minPrice = min
                maxPrice = max
                showPriceDialog = false
                onFilterChange(
                    filter.copy(
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        minYear = minYear,
                        maxYear = maxYear,
                        minMileage = minMileage,
                        maxMileage = maxMileage,
                        fuelType = if (selectedFuelType == "전체") null else selectedFuelType,
                        region = if (selectedRegion == "전체") null else selectedRegion
                    )
                )
            }
        )
    }

    // 연식 모달
    if (showYearDialog) {
        FilterRangeModal(
            title = "연식",
            unit = "년",
            valueRange = 2000f..2025f,
            steps = 2025 - 2000 - 1,
            currentMin = minYear.toFloat(),
            currentMax = maxYear.toFloat(),
            onDismiss = { showYearDialog = false },
            onApply = { min, max ->
                minYear = min
                maxYear = max
                showYearDialog = false
                onFilterChange(
                    filter.copy(
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        minYear = minYear,
                        maxYear = maxYear,
                        minMileage = minMileage,
                        maxMileage = maxMileage,
                        fuelType = if (selectedFuelType == "전체") null else selectedFuelType,
                        region = if (selectedRegion == "전체") null else selectedRegion
                    )
                )
            }
        )
    }

    // 주행거리 모달
    if (showMileageDialog) {
        FilterRangeModal(
            title = "주행거리",
            unit = "km",
            valueRange = 0f..100000f,
            steps = 100,
            currentMin = minMileage.toFloat(),
            currentMax = maxMileage.toFloat(),
            onDismiss = { showMileageDialog = false },
            onApply = { min, max ->
                minMileage = min
                maxMileage = max
                showMileageDialog = false
                onFilterChange(
                    filter.copy(
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        minYear = minYear,
                        maxYear = maxYear,
                        minMileage = minMileage,
                        maxMileage = maxMileage,
                        fuelType = if (selectedFuelType == "전체") null else selectedFuelType,
                        region = if (selectedRegion == "전체") null else selectedRegion
                    )
                )
            }
        )
    }

    // 연료 선택 모달
    if (showFuelDialog) {
        FilterSelectModal(
            title = "연료",
            options = fuelTypes,
            selectedOption = selectedFuelType,
            onDismiss = { showFuelDialog = false },
            onApply = { selectedFuelType = it; showFuelDialog = false; onFilterChange(filter.copy(fuelType = if (it=="전체") null else it)) }
        )
    }

    // 지역 선택 모달
    if (showRegionDialog) {
        FilterSelectModal(
            title = "지역",
            options = regions,
            selectedOption = selectedRegion,
            onDismiss = { showRegionDialog = false },
            onApply = { selectedRegion = it; showRegionDialog = false; onFilterChange(filter.copy(region = if (it=="전체") null else it)) }
        )
    }
}

// Compose의 Partial Bottom Sheet를 사용하는 버전
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false // Partial(중간) 상태 허용
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
        dragHandle = null // 필요시 드래그 핸들 추가 가능
    ) {
        // 시트 내부 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            content = content
        )
    }
}


// 범위 선택 모달
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRangeModal(
    title: String,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    currentMin: Float,
    currentMax: Float,
    onDismiss: () -> Unit,
    onApply: (Int, Int) -> Unit
) {
    var minVal by remember { mutableFloatStateOf(currentMin) }
    var maxVal by remember { mutableFloatStateOf(currentMax) }
    val noRipple = remember { MutableInteractionSource() }

    var minInput by remember { mutableStateOf(currentMin.toInt().toString()) }
    var maxInput by remember { mutableStateOf(currentMax.toInt().toString()) }

    LaunchedEffect(minVal) { minInput = minVal.toInt().toString() }
    LaunchedEffect(maxVal) { maxInput = maxVal.toInt().toString() }

    BottomSheetDialog(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Row {
                TextButton(onClick = {
                    minVal = valueRange.start
                    maxVal = valueRange.endInclusive
                }) {
                    Text("초기화")
                    Icon(Icons.Default.Refresh, contentDescription = "초기화")
                }

                Spacer(Modifier.height(8.dp))

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "닫기")
                }
            }

        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberInputField(
                value = minInput,
                onValueChange = {
                    minInput = it
                    minVal = it.toFloatOrNull()?.coerceIn(valueRange.start, maxVal) ?: minVal
                },
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text("~", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))

            NumberInputField(
                value = maxInput,
                onValueChange = {
                    maxInput = it
                    maxVal = it.toFloatOrNull()?.coerceIn(minVal, valueRange.endInclusive) ?: maxVal
                },
            )
        }

        Spacer(Modifier.height(16.dp))

        RangeSlider(
            value = minVal..maxVal,
            onValueChange = { range ->
                val newMin = range.start.coerceIn(valueRange.start, valueRange.endInclusive)
                val newMax = range.endInclusive.coerceIn(valueRange.start, valueRange.endInclusive)
                minVal = newMin
                maxVal = newMax
            },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                activeTrackColor = Color(0xFF3058EF),
                inactiveTrackColor = Color(0xFFE0E0E0),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            startThumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .indication(interactionSource = noRipple, indication = null)
                )
            },
            endThumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .indication(interactionSource = noRipple, indication = null)
                )
            }
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onApply(minVal.toInt(), maxVal.toInt()) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3058EF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp), // 버튼 높이 조절
            shape = RoundedCornerShape(6.dp), // 둥근 정도 (기본은 20dp 근처)
            ) { Text("확인", color = Color.White) }
    }
}

// ----------------- BasicTextField 기반 숫자 입력 -----------------
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
            modifier = Modifier
                .width(160.dp)
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isFocused) Color.Gray else Color.LightGray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .onFocusChanged { isFocused = it.isFocused }
        )
    }
}


// 단일 선택 모달
@Composable
fun FilterSelectModal(
    title: String,
    options: List<String>,
    selectedOption: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var selection by remember { mutableStateOf(selectedOption) }

    BottomSheetDialog(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "닫기")
            }
        }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { selection = option }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selection == option,
                    onClick = { selection = option },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3058EF))
                )
                Spacer(Modifier.width(12.dp))
                Text(option, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { selection = options.firstOrNull() ?: selection }) { Text("초기화") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onApply(selection) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3058EF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp), // 버튼 높이 조절
            shape = RoundedCornerShape(6.dp), // 둥근 정도 (기본은 20dp 근처)
            contentPadding = PaddingValues(vertical = 12.dp) // 텍스트와 버튼 사이 패딩
        ) {
            Text("확인", color = Color.White, fontSize = 16.sp)
        }
    }
}

// 필터 버튼
@Composable
fun FilterLabelButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Row {
            Text(label, fontSize = 14.sp)
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "필터",
            )
        }
    }
}

// 차 카드
@Composable
fun CarCardVertical(
    car: Car,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.LightGray.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = car.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)), // 위쪽만 둥글게
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (car.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (car.isFavorite) Color.Red else Color.Gray
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(car.trim, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("${car.year}년 · ${car.mileage}km · ${car.fuelType} · ${car.region}" , fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(car.price)}만원",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3058EF)
            )
        }
    }
}