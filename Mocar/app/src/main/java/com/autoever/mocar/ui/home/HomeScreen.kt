package com.autoever.mocar.ui.home

import ROUTE_SEARCH
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import carDetailRoute
import com.autoever.mocar.R
import com.autoever.mocar.model.Brand
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.ui.common.component.atoms.BrandChip
import com.autoever.mocar.ui.common.component.atoms.BrandUi
import com.autoever.mocar.ui.common.component.molecules.CarCard
import com.autoever.mocar.ui.common.component.molecules.CarUi
import com.autoever.mocar.ui.common.component.molecules.FavoriteCarousel
//import com.autoever.mocar.ui.home.HomeSampleData.brands as sampleBrands

@Composable
fun HomeRoute(
    navController: NavController,
    vm: com.autoever.mocar.viewmodel.HomeViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.uiState.collectAsState()

    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("로딩 중…") }
        state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("오류: ${state.error}") }
        else -> HomeScreen(
            navController = navController,
            cars = state.cars,
            onToggleFavorite = { vm.toggleFavorite(it) }
        )
    }
}

// ---------------- 홈스크린 ----------------
@Composable
fun HomeScreen(
    navController: NavController,
    cars: List<Car>,
    onToggleFavorite: (String) -> Unit
) {
    val gutter = 22.dp
    var selectedBrandId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(selectedBrandId, cars) {
        if (selectedBrandId == null) cars else cars.filter { it.brandId == selectedBrandId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)),
        contentPadding = PaddingValues(
            start = gutter, end = gutter,
            top = 16.dp, bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 상단 로고/알림
        item { TopBar(notifications = 2) }

        // 검색바 + 필터 버튼
        item {
            SearchBar(
                onClickBar   = { navController.navigate(ROUTE_SEARCH) },
                onClickFilter = { navController.navigate(ROUTE_SEARCH) }
            )
        }

        // 찜한 목록 캐러셀
        item { SectionHeader("찜한 목록", "Available", "View All") }
        item {
            FavoriteCarousel(
                cars = cars.filter { it.isFavorite }.map { it.toUi() },
                onToggleFav = { c -> onToggleFavorite(c.id) },
                onCardClick = { car -> navController.navigate(carDetailRoute(car.id)) }
            )
        }

        // 브랜드 선택
//        item {
//            Text("Brands", style = MaterialTheme.typography.titleMedium)
//            Spacer(Modifier.height(12.dp))
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                items(sampleBrands, key = { it.id }) { brand ->
//                    BrandChip(
//                        brand = brand.toUi(),
//                        selected = selectedBrandId == brand.id,
//                        onClick = {
//                            selectedBrandId =
//                                if (selectedBrandId == brand.id) null else brand.id
//                        }
//                    )
//                }
//            }
//        }

        // 필터 결과 헤더
//        item {
//            val title = if (selectedBrandId == null) {
//                "전체 차량"
//            } else {
//                sampleBrands.firstOrNull { it.id == selectedBrandId }?.name ?: "필터 결과"
//            }
//            SectionHeader(
//                title = title,
//                subtitle = "Available",
//                actionText = if (selectedBrandId != null) "Clear" else null,
//                onActionClick = { selectedBrandId = null }
//            )
//        }

        // 차량 카드 2열 그리드
        items(filtered.chunked(2)) { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { car ->
                    Box(Modifier.weight(1f)) {
                        CarCard(
                            car = car.toUi(),
                            onFavoriteToggle = { onToggleFavorite(car.id) },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(carDetailRoute(car.id)) }
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f)) // 홀수 보정
            }
        }
    }
}

/* ---------------- 매퍼 (도메인 → UI) ---------------- */
private fun Car.toUi() = CarUi(
    id = id,
    title = title,
    imageUrl = imageUrl,   // URL 사용
    imageRes = null,
    mileageKm = mileageKm,
    region = region,
    priceKRW = priceKRW,
    isFavorite = isFavorite
)

private fun Brand.toUi() = BrandUi(
    id = id,
    name = name,
    logoRes = logoRes
)

/* ---------------- TopBar ---------------- */
@Composable
private fun TopBar(notifications: Int = 0) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_mocar),
            contentDescription = "Mocar logo",
            modifier = Modifier.height(30.dp)
        )

        BadgedBox(
            badge = {
                if (notifications > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                    ) {
                        Text(text = notifications.toString())
                    }
                }
            }
        ) {
            Surface(shape = CircleShape, color = Color.White, tonalElevation = 1.dp) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "알림", tint = Color.Black)
                }
            }
        }
    }
}

/* ---------------- SearchBar ---------------- */
@Composable
private fun SearchBar(
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onClickFilter: () -> Unit = {},
    onClickBar: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(56.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = true,
                modifier = Modifier.matchParentSize(),
                singleLine = true,
                placeholder = { Text("어떤 차를 찾으시나요?") },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(22.dp), tint = Color(0xFF6B7280))
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White,
                    unfocusedBorderColor    = Color(0xFFE5E7EB),
                    focusedBorderColor      = Color(0xFFE5E7EB)
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        onClickBar()
                    }
            )
        }
        Spacer(Modifier.width(12.dp))
        FilledIconButton(
            onClick = onClickFilter,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF2A5BFF), contentColor = Color.White
            )
        ) { Icon(Icons.Default.Tune, contentDescription = "필터") }
    }
}

/* ---------------- SectionHeader ---------------- */
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        if (actionText != null) {
            Text(
                actionText,
                color = Color.Gray,
                modifier = if (onActionClick != null)
                    Modifier.clickable { onActionClick() } else Modifier
            )
        }
    }
}

/* ---------------- 가격 포맷터 ---------------- */
fun formatKrwPretty(amount: Long): String {
    val eok = amount / 100_000_000
    val man = (amount % 100_000_000) / 10_000
    return when {
        eok > 0L && man > 0L -> "${eok}억 ${String.format("%,d만원", man)}"
        eok > 0L && man == 0L -> "${eok}억"
        else -> String.format("%,d만원", man)
    }
}