import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.ui.common.component.atoms.MocarTopBar
import com.autoever.mocar.ui.common.util.formatKrwPretty
import com.autoever.mocar.viewmodel.ResultFilterParams
import com.autoever.mocar.viewmodel.SearchResultViewModel

@Composable
fun SearchResultPage(
    navController: NavController,
    searchResultViewModel: SearchResultViewModel,
) {
    val results by searchResultViewModel.results.collectAsState()
    val favorites by searchResultViewModel.favorites.collectAsState()

    LaunchedEffect(results) {
        println("SearchResultViewModel에 저장된 결과: ${results.size}대")
        results.forEach { println(it) }
    }

    var filterParams by remember {
        mutableStateOf(
            ResultFilterParams(
                subModels = emptyList(),
                minPrice = 0f,
                maxPrice = 1000000000f, // 단위: 원
                minYear = 1990f,
                maxYear = 2025f,
                minMileage = 0f,
                maxMileage = 30000000f,
                types = emptyList(),
                fuels = emptyList(),
                regions = emptyList(),
            )
        )
    }

    fun applyFilter(results: List<ListingDto>, filter: ResultFilterParams?): List<ListingDto> {
        if (filter == null) return results

        return results.filter { car ->
            (filter.minPrice <= car.price && car.price <= filter.maxPrice) &&
                    (filter.minYear <= car.year && car.year <= filter.maxYear) &&
                    (filter.minMileage <= car.mileage && car.mileage <= filter.maxMileage) &&
                    (filter.types.isEmpty() || filter.types.contains(car.carType)) &&
                    (filter.fuels.isEmpty() || filter.fuels.contains(car.fuel)) &&
                    (filter.regions.isEmpty() || filter.regions.contains(car.region))
        }
    }

    val filteredCars = applyFilter(results, filterParams)
    Scaffold(
        topBar = {
            MocarTopBar(
                title = { Text("검색 결과 (${filteredCars.size}대)", style = MaterialTheme.typography.titleMedium) },
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            FilterRowSection(
                filter = filterParams,
                onFilterChange = { newFilter -> filterParams = newFilter }
            )
            if (filteredCars.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("검색 결과가 없습니다.", color = Color.Gray)
                }
            } else {
                val favoriteIds = remember(favorites) {
                    favorites.map { normalizeId(it.listingId) }.toSet()
                }
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
                            val car1 = carPairs[idx][0]
                            val car1Id = normalizeId(car1.listingId)
                            CarCardVertical(
                                listing = car1,
                                isFavorite = favoriteIds.contains(car1Id),
                                onClick = { navController.navigate(carDetailRoute(car1.listingId)) },
                                onFavoriteClick = {
                                    if (favoriteIds.contains(car1Id)) {
                                        searchResultViewModel.removeFavorite(car1Id)
                                    } else {
                                        searchResultViewModel.addFavorite(car1)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            if (carPairs[idx].size > 1) {
                                val car2 = carPairs[idx][1]
                                val car2Id = normalizeId(car2.listingId)
                                CarCardVertical(
                                    listing = car2,
                                    isFavorite = favoriteIds.contains(car2Id),
                                    onClick = { navController.navigate(carDetailRoute(car2.listingId)) },
                                    onFavoriteClick = {
                                        if (favoriteIds.contains(car2Id)) {
                                            searchResultViewModel.removeFavorite(car2Id)
                                        } else {
                                            searchResultViewModel.addFavorite(car2)
                                        }
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
}

// 필터 Row
@Composable
fun FilterRowSection(
    filter: ResultFilterParams,
    onFilterChange: (ResultFilterParams) -> Unit
) {
    var minPrice by remember { mutableFloatStateOf(filter.minPrice) }
    var maxPrice by remember { mutableFloatStateOf(filter.maxPrice) }
    var minYear by remember { mutableFloatStateOf(filter.minYear) }
    var maxYear by remember { mutableFloatStateOf(filter.maxYear) }
    var minMileage by remember { mutableFloatStateOf(filter.minMileage) }
    var maxMileage by remember { mutableFloatStateOf(filter.maxMileage) }

    var selectedFuelType by remember { mutableStateOf("전체") }
    var selectedRegion by remember { mutableStateOf("전체") }

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
        // 100만원 단위 이동: (전체Max-전체Min)/(100) - 1
        val priceSteps = ((filter.maxPrice / 10_000f) / 100f).toInt() - 1

        FilterRangeModal(
            title = "가격",
            unit = "만원",
            valueRange = 0f..80000000f / 10_000f,
            steps = priceSteps,
            currentMin = minPrice / 10_000f,
            currentMax = maxPrice / 10_000f,
            onDismiss = { showPriceDialog = false },
            onApply = { minMan, maxMan ->
                minPrice = minMan * 10_000f
                maxPrice = if (maxMan * 10_000f >= 80_000_000f) Float.MAX_VALUE else maxMan * 10_000f
                showPriceDialog = false
                onFilterChange(filter.copy(
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    minYear = minYear,
                    maxYear = maxYear,
                    minMileage = minMileage,
                    maxMileage = maxMileage,
                    fuels = if (selectedFuelType=="전체") emptyList() else listOf(selectedFuelType),
                    regions = if (selectedRegion=="전체") emptyList() else listOf(selectedRegion)
                ))
            }
        )
    }

    // 연식 모달
    if (showYearDialog) {
        FilterRangeModal(
            title = "연식",
            unit = "년",
            valueRange = 1990f..2025f,
            steps = (2025 - 1990) - 1,
            currentMin = minYear,
            currentMax = maxYear,
            onDismiss = { showYearDialog = false },
            onApply = { minYearValue, maxYearValue ->
                minYear = minYearValue.toFloat()
                maxYear = maxYearValue.toFloat()
                showYearDialog = false
                onFilterChange(
                    filter.copy(
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        minYear = minYear,
                        maxYear = maxYear,
                        minMileage = minMileage,
                        maxMileage = maxMileage,
                        fuels = if (selectedFuelType == "전체") emptyList() else listOf(selectedFuelType),
                        regions = if (selectedRegion == "전체") emptyList() else listOf(selectedRegion)
                    )
                )
            }
        )
    }

    // 주행거리 모달
    if (showMileageDialog) {
        val steps = (300_000f / 10_000f).toInt() - 1
        FilterRangeModal(
            title = "주행거리",
            unit = "km",
            valueRange = 0f..300_000f,
            steps = steps,
            currentMin = minMileage,
            currentMax = if (maxMileage >= 300_000f) 300_000f else maxMileage,
            onDismiss = { showMileageDialog = false },
            onApply = { min, max ->
                minMileage = min.toFloat()
                maxMileage = if (max >= 300_000f) Float.MAX_VALUE else max.toFloat()
                showMileageDialog = false
                onFilterChange(
                    filter.copy(
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        minYear = minYear,
                        maxYear = maxYear,
                        minMileage = minMileage,
                        maxMileage = maxMileage,
                        fuels = if (selectedFuelType == "전체") emptyList() else listOf(selectedFuelType),
                        regions = if (selectedRegion == "전체") emptyList() else listOf(selectedRegion)
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
            onApply = {
                selectedFuelType = it
                showFuelDialog = false
                onFilterChange(
                    filter.copy(
                        fuels = if (it == "전체") emptyList() else listOf(it)
                    )
                )
            }
        )
    }

    // 지역 선택 모달
    if (showRegionDialog) {
        FilterSelectModal(
            title = "지역",
            options = regions,
            selectedOption = selectedRegion,
            onDismiss = { showRegionDialog = false },
            onApply = {
                selectedRegion = it
                showRegionDialog = false
                onFilterChange(
                    filter.copy(
                        regions = if (it == "전체") emptyList() else listOf(it)
                    )
                )
            }
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
    listing: ListingDto,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val car = listing.toCar()

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)                         // 카드 전체 높이 고정
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.8f)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxSize()) {

            // ✅ 이미지 영역을 고정 비율로 (모든 카드 동일 높이)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)           // 필요하면 16f/9f로 변경
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            ) {
                AsyncImage(
                    model = car.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),   // ❗ width/height 고정 제거
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            //텍스트 영역: 줄수 고정 + 남는 높이 소화
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)                      // 남는 공간 정리
                    .padding(8.dp)
            ) {
                Text(
                    car.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,                    // 제목 2줄 고정
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${car.yearDesc} · ${car.mileageKm}km · ${car.fuel} · ${car.region}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,                    //스펙 1줄 고정
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))          // 가격을 하단으로 밀기
                Spacer(Modifier.height(4.dp))
                Text(
                    formatKrwPretty(car.priceKRW),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2A5BFF)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun normalizeId(raw: String?): String =
    raw?.removePrefix("listing_")?.trim().orEmpty()
