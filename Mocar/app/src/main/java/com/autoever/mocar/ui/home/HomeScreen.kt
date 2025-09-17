package com.autoever.mocar.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import carDetailRoute
import com.autoever.mocar.R
import com.autoever.mocar.model.Brand
import com.autoever.mocar.model.Car
import com.autoever.mocar.ui.home.HomeSampleData.cars

// ---------------- 홈스크린 ----------------
@Composable
fun HomeScreen(navController: NavController,
               cars: List<Car>,
               onToggleFavorite: (String) -> Unit) {
    var favorites by remember { mutableStateOf(HomeSampleData.cars) }
    var selectedBrandId by remember { mutableStateOf<String?>(null) }

    val filtered by remember(selectedBrandId, cars) {
        mutableStateOf(
            if (selectedBrandId == null) cars
            else cars.filter { it.brandId == selectedBrandId }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 상단바 / 검색
        item { TopBar(notifications = 2) }
        item { SearchBar() }

        // 찜한 목록 캐러셀
        item { SectionHeader("찜한 목록", "Available", "View All") }
        item {
            FavoriteCarousel(
                cars = cars.filter { it.isFavorite },
                onToggleFav = { c -> onToggleFavorite(c.id) },
                onCardClick = { car -> navController.navigate(carDetailRoute(car.id)) }
            )
        }

        // 브랜드 선택
        item {
            Text("Brands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(HomeSampleData.brands, key = { it.id }) { brand ->
                    BrandChip(
                        brand = brand,
                        selected = selectedBrandId == brand.id,
                        onClick = {
                            selectedBrandId =
                                if (selectedBrandId == brand.id) null else brand.id
                        }
                    )
                }
            }
        }

        // 필터 결과 헤더
        item {
            val title = if (selectedBrandId == null) {
                "전체 차량"
            } else {
                HomeSampleData.brands.firstOrNull { it.id == selectedBrandId }?.name ?: "필터 결과"
            }
            SectionHeader(title = title, subtitle = "Available", actionText = if (selectedBrandId != null) "Clear" else null)
        }

        // 차량 카드 2열 그리드
        items(filtered.chunked(2)) { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { car ->
                    Box(Modifier.weight(1f)) {
                        CarCard(
                            car = car,
                            onFavoriteToggle = { onToggleFavorite(car.id) },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navController.navigate(carDetailRoute(car.id))
                            }
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f)) // 홀수 개 보정
            }
        }
    }
}


// ---------------- TopBar ----------------
@Composable
fun TopBar(notifications: Int = 0) {
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgedBox(
                badge = {
                    if (notifications > 0) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier.offset(x = (-2).dp, y = (2).dp) // ← 위치 조정
                        ) {
                            Text(text = notifications.toString())
                        }
                    }
                }
            ) {
                Surface(     // 아이콘 둥근 배경
                    shape = CircleShape,
                    color = Color.White,
                    tonalElevation = 1.dp,
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림",
                            tint = Color.Black
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

// ---------------- SearchBar ----------------
@Composable
fun SearchBar(
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onClickFilter: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 입력창
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            singleLine = true,
            placeholder = { Text("어떤 차를 찾으시나요?") },
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
                // 컨테이너: 흰색
                unfocusedContainerColor = Color.White,
                focusedContainerColor   = Color.White,
                // 테두리: 연회색 고정 (밑줄 없음)
                unfocusedBorderColor    = Color(0xFFE5E7EB),
                focusedBorderColor      = Color(0xFFE5E7EB),
                // 텍스트/플레이스홀더 색
                unfocusedTextColor      = Color(0xFF111827),
                focusedTextColor        = Color(0xFF111827),
                unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                focusedPlaceholderColor   = Color(0xFF9CA3AF),
                cursorColor             = Color(0xFF2A5BFF)  // 브랜드 블루
            )
        )

        Spacer(Modifier.width(12.dp))

        // 필터 버튼 (파란 사각, 16dp 라운드, 흰 아이콘)
        FilledIconButton(
            onClick = onClickFilter,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF2A5BFF),   // 브랜드 블루
                contentColor   = Color.White
            )
        ) {
            Icon(Icons.Default.Tune, contentDescription = "필터")
        }
    }
}

// ---------------- SectionHeader ----------------
@Composable
private fun SectionHeader(title: String, subtitle: String? = null, actionText: String? = null) {
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
            Text(actionText, color = Color.Gray)
        }
    }
}

// ---------------- CarCard ----------------
@Composable
fun CarCard(
    car: Car,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    OutlinedCard(
        modifier = modifier
            .width(260.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        onClick = { onClick?.invoke() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Image(
                    painter = painterResource(car.imageRes),
                    contentDescription = car.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // 카드 안쪽 오른쪽 상단 하트 버튼
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .size(32.dp) // 동그라미 크기
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape) // 회색 테두리
                        .background(Color.White, CircleShape) // 흰색 배경
                        .clickable { onFavoriteToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (car.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "찜",
                        tint = Color(0xFF111827), // 아이콘 색
                        modifier = Modifier.size(20.dp) // 하트 아이콘 크기
                    )
                }
            }

            Divider(color = Color(0xFFEDEDED), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(
                    car.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${car.mileageKm}km", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Outlined.Place, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(car.region, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    formatKrwPretty(car.priceKRW),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2A5BFF))
                )
            }
        }
    }
}

fun formatKrwPretty(amount: Long): String {
    val eok = amount / 100_000_000
    val man = (amount % 100_000_000) / 10_000

    return when {
        eok > 0L && man > 0L -> "${eok}억 ${String.format("%,d만원", man)}"
        eok > 0L && man == 0L -> "${eok}억"
        else -> String.format("%,d만원", man)
    }
}


// ---------------- BrandChip ----------------
@Composable
private fun BrandChip(
    brand: Brand,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Color(0xFF2A5BFF) else Color(0xFFE5E7EB),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = brand.logoRes),
                contentDescription = brand.name,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(brand.name, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun FavoriteCarousel(
    cars: List<Car>,
    onToggleFav: (Car) -> Unit,
    onCardClick: (Car) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp) // 좌우 여백
    ) {
        items(cars, key = { it.id }) { car ->
            CarCard( // 당신이 쓰는 최종 카드 컴포저블 이름
                car = car,
                onFavoriteToggle = { onToggleFav(car) },
                modifier = Modifier.width(260.dp),
                onClick = { onCardClick(car) }
            )
        }
    }
}