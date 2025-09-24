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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import com.autoever.mocar.R
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.ui.common.component.atoms.BrandChip
import com.autoever.mocar.ui.common.component.atoms.BrandUi
import com.autoever.mocar.ui.common.component.molecules.CarGrid
import com.autoever.mocar.ui.common.component.molecules.CarUi
import com.autoever.mocar.ui.common.component.molecules.FavoriteCarousel
import com.autoever.mocar.viewmodel.HomeViewModel

@Composable
fun HomeRoute(
    navController: NavController,
    scrollSignal: Int,
    vm: HomeViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.uiState.collectAsState()
    val favorites by vm.favoriteIds.collectAsState(emptySet())

    when {
        state.loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        state.error != null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("오류: ${state.error}", color = Color.Red)
        }
        else -> HomeScreen(
            navController = navController,
            cars = state.cars,
            brands = state.brands,
            favorites = favorites,
            scrollSignal = scrollSignal
        )
    }
}

// ---------------- 홈스크린 ----------------
@Composable
fun HomeScreen(
    navController: NavController,
    cars: List<Car>,
    brands: List<BrandUi>,
    favorites: Set<String>,
    scrollSignal: Int
) {
    val gutter = 22.dp
    var selectedBrandId by remember { mutableStateOf<String?>(null) }

    val selectedBrandName = remember(selectedBrandId, brands) {
        brands.firstOrNull { it.id == selectedBrandId }?.name
    }

    val listState = rememberLazyListState()

    // 🔹 시그널 값이 바뀔 때마다 맨 위로 스크롤
    LaunchedEffect(scrollSignal) {
        if (scrollSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    val filtered = remember(selectedBrandName, cars) {
        if (selectedBrandName.isNullOrBlank()) {
            cars
        } else {
            cars.filter { it.brandName.equals(selectedBrandName, ignoreCase = true) }
        }
    }

    LazyColumn(
        state = listState,
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
                navController = navController,
                cars = cars
                    .filter { favorites.contains(it.id) }
                    .map { it.toUi() },
            )
        }

      //브랜드 선택
        item {
            Text("Brands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(brands, key = { it.id }) { brand ->
                    Box(
                        modifier = Modifier.width(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BrandChip(
                            brand = brand,
                            selected = selectedBrandId == brand.id,
                            onClick = {
                                selectedBrandId =
                                    if (selectedBrandId == brand.id) null else brand.id
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 필터 결과 헤더
        item {
            val title = if (selectedBrandId == null) {
                "전체 차량"
            } else {
                brands.firstOrNull { it.id == selectedBrandId }?.name ?: "필터 결과"
            }
            SectionHeader(
                title = title,
                subtitle = "Available",
                actionText = if (selectedBrandId != null) "Clear" else null,
                onActionClick = { selectedBrandId = null }
            )
        }

        // 차량 카드 2열 그리드
        item {
            CarGrid(
                navController = navController,
                cars = filtered.map { car ->
                    car.toUi()
                },
            )
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