import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

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
    manufacturer: String,
    model: String,
    filter: CarFilter = CarFilter(),
    onCarClick: (Car) -> Unit = {},
    onFavoriteClick: (Car) -> Unit = {}
) {
    val carList by remember { mutableStateOf(sampleCarList.filter { it.trim.contains(model) }) }
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

    val filteredCars = applyFilter(carList, appliedFilter)

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

// 하단 시트 기본 컴포저블
@Composable
fun BottomSheetDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                content = content
            )
        }
    }
}

// 범위 선택 모달
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

    BottomSheetDialog(onDismiss = onDismiss) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        RangeSlider(
            value = minVal..maxVal,
            onValueChange = { range -> minVal = range.start; maxVal = range.endInclusive },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF3058EF),
                activeTrackColor = Color(0xFF3058EF)
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${minVal.toInt()}$unit", fontWeight = FontWeight.Medium)
            Text("${maxVal.toInt()}$unit", fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("취소") }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { onApply(minVal.toInt(), maxVal.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3058EF))
            ) { Text("적용", color = Color.White) }
        }
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
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("취소") }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { onApply(selection) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3058EF))
            ) { Text("적용", color = Color.White) }
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

// 샘플 데이터
val sampleCarList = listOf(
    Car(
        id = "1",
        imageUrl = "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfNzgg%2FMDAxNzQ2NjYzNDMzODEx.sYs7v_njUPhV8Zkf38dANvaiGhPMD-jM91WdhmXr0SMg.xMShH1vd1djcmFvE2TWB-dZT88nhGYVG7chpVYo06dAg.JPEG%2F2.jpg&type=l340_165",
        trim = "아반떼 AD 1.6 GDi",
        year = 2018,
        mileage = 45000,
        fuelType = "가솔린",
        region = "서울",
        price = 3600
    ),
    Car(
        id = "1",
        imageUrl = "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfNzgg%2FMDAxNzQ2NjYzNDMzODEx.sYs7v_njUPhV8Zkf38dANvaiGhPMD-jM91WdhmXr0SMg.xMShH1vd1djcmFvE2TWB-dZT88nhGYVG7chpVYo06dAg.JPEG%2F2.jpg&type=l340_165",
        trim = "아반떼 AD 1.6 GDi",
        year = 2018,
        mileage = 45000,
        fuelType = "가솔린",
        region = "서울",
        price = 1000
    ),
    Car(
        id = "1",
        imageUrl = "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfNzgg%2FMDAxNzQ2NjYzNDMzODEx.sYs7v_njUPhV8Zkf38dANvaiGhPMD-jM91WdhmXr0SMg.xMShH1vd1djcmFvE2TWB-dZT88nhGYVG7chpVYo06dAg.JPEG%2F2.jpg&type=l340_165",
        trim = "아반떼 AD 1.6 GDi",
        year = 2018,
        mileage = 45000,
        fuelType = "가솔린",
        region = "서울",
        price = 8660
    ),
    Car(
        id = "2",
        imageUrl = "https://cdn.pixabay.com/photo/2015/01/19/13/51/car-604019_1280.jpg",
        trim = "쏘나타 DN8 2.0",
        year = 2020,
        mileage = 30000,
        fuelType = "LPG",
        region = "경기",
        price = 1800
    ),
    Car(
        id = "3",
        imageUrl = "https://cdn.pixabay.com/photo/2016/11/29/09/32/auto-1868726_1280.jpg",
        trim = "K5 DL3 1.6 터보",
        year = 2021,
        mileage = 15000,
        fuelType = "가솔린",
        region = "부산",
        price = 2200
    ),
    Car(
        id = "4",
        imageUrl = "https://cdn.pixabay.com/photo/2013/07/12/15/55/car-150334_1280.png",
        trim = "그랜저 IG 2.4",
        year = 2019,
        mileage = 38000,
        fuelType = "가솔린",
        region = "대구",
        price = 2100
    ),
    Car(
        id = "5",
        imageUrl = "https://cdn.pixabay.com/photo/2014/07/31/23/10/car-407165_1280.jpg",
        trim = "스포티지 QL 디젤",
        year = 2017,
        mileage = 60000,
        fuelType = "디젤",
        region = "인천",
        price = 1700
    ),
    Car(
        id = "6",
        imageUrl = "https://cdn.pixabay.com/photo/2012/05/29/00/43/car-49278_1280.jpg",
        trim = "아반떼 CN7 1.6",
        year = 2022,
        mileage = 8000,
        fuelType = "가솔린",
        region = "서울",
        price = 2300
    ),
    Car(
        id = "7",
        imageUrl = "https://cdn.pixabay.com/photo/2015/01/19/13/51/car-604019_1280.jpg",
        trim = "쏘렌토 MQ4 2.2 디젤",
        year = 2021,
        mileage = 20000,
        fuelType = "디젤",
        region = "경기",
        price = 3200
    ),
    Car(
        id = "8",
        imageUrl = "https://cdn.pixabay.com/photo/2016/11/29/09/32/auto-1868726_1280.jpg",
        trim = "K3 BD 1.6",
        year = 2019,
        mileage = 35000,
        fuelType = "가솔린",
        region = "부산",
        price = 1400
    ),
    Car(
        id = "9",
        imageUrl = "https://cdn.pixabay.com/photo/2013/07/12/15/55/car-150334_1280.png",
        trim = "그랜저 IG 3.0",
        year = 2018,
        mileage = 42000,
        fuelType = "가솔린",
        region = "대전",
        price = 2500
    ),
    Car(
        id = "10",
        imageUrl = "https://cdn.pixabay.com/photo/2014/07/31/23/10/car-407165_1280.jpg",
        trim = "스포티지 NQ5 하이브리드",
        year = 2023,
        mileage = 5000,
        fuelType = "하이브리드",
        region = "울산",
        price = 3400
    )
)

// 미리보기
@Composable
@Preview(showBackground = true)
fun SearchResultScreenPreview() {
    SearchResultScreen(
        manufacturer = "현대",
        model = "아반떼",
        filter = CarFilter(),
        onCarClick = {},
        onFavoriteClick = {}
    )
}
