package com.autoever.mocar.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.autoever.mocar.R
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.domain.model.Seller
import com.autoever.mocar.ui.common.component.atoms.MocarTopBar
import com.autoever.mocar.ui.common.util.formatKrwPretty
import com.autoever.mocar.viewmodel.PriceUi
import org.checkerframework.checker.units.qual.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarDetailScreen(
    car: Car,
    seller: Seller?,
    price: PriceUi?,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {

    var isFav by remember(car.id) { mutableStateOf(car.isFavorite) }

    Scaffold(
        topBar = {
            MocarTopBar(
                title = car.plateNo,
                onBack = onBack,
                onMore = { /* TODO: 메뉴 */ }
            )
        },
        bottomBar = {
            BottomActionBar(
                priceRange = formatKrwPretty(car.priceKRW),
                onBuy = { /* TODO: 구매 플로우 */ }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F9))
                .padding(inner),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                val pagerState = rememberPagerState(pageCount = { car.images.size.coerceAtLeast(1) })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color.White)
                ) {
                    if (car.images.isNotEmpty()) {
                        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                            AsyncImage(
                                model = car.images[page],
                                contentDescription = car.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF3F4F6))
                        )
                    }

                    HeartButton(
                        isFavorite = isFav,
                        onClick = {
                            isFav = !isFav
                            onToggleFavorite()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )

                    DotsIndicator(
                        total = car.images.size.coerceAtLeast(1),
                        selected = pagerState.currentPage + 1,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }

            // 제목/요약/가격
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        car.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${car.yearDesc} · ${String.format("%,d", car.mileageKm)}km · ${car.fuel} · ${car.region}", // ✅ 실제 데이터
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatKrwPretty(car.priceKRW),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A5BFF)
                        )
                    )
                }
            }

            // 판매자 카드
            item {
                seller?.let {
                    SellerCard(
                        name = it.name,
                        rating = it.rating,
                        reviewCount = it.ratingCount,
                        photoUrl = it.photoUrl
                    )
                }
            }

            // 기본 정보
            item {
                SectionCard(title = "기본 정보") {
                    InfoRow("차량번호", car.plateNo)
                    InfoRow("연식", car.yearDesc)
                    InfoRow("주행거리", "${String.format("%,d", car.mileageKm)}km")
                    InfoRow("차종", car.carType)
                    InfoRow("배기량", "${car.displacement}cc")
                    InfoRow("연료", car.fuel)
                }
            }

            // 이 차의 상태
            item {
                car.description?.let { desc ->
                    val bullets = remember(desc) {
                        desc
                            .replace("\r", "")
                            .split('.', '\n')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    }
                    if (bullets.isNotEmpty()) {
                        SectionCard(title = "이 차의 상태") {
                            bullets.forEach { line ->
                                //마지막에 남은 온점 제거
                                val clean = if (line.endsWith(".")) line.dropLast(1) else line
                                BulletText(clean)
                            }
                        }
                    }
                }
            }

            // 시세
            item {
                if (price != null && price.min > 0 && price.max > 0) {
                    SectionCard(title = "시세") {
                        Text("시세안전구간", color = Color(0xFF6B7280), fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))

                        PriceBandReadonly(
                            min = price.min,
                            max = price.max,
                            current = car.priceKRW
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- Heart Button ---------------- */

@Composable
private fun HeartButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp) // 터치 영역
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "찜",
            tint = if (isFavorite) Color.Red else Color.Black,
            modifier = Modifier.size(28.dp)
        )
    }
}

/* ---------------- Dots ---------------- */

@Composable
private fun DotsIndicator(
    total: Int,
    selected: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x66000000)) // 살짝 어두운 배경(옵션)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { idx ->
            Box(
                modifier = Modifier
                    .size(if (idx + 1 == selected) 7.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (idx + 1 == selected) Color.White else Color(0xFFDDDDDD)
                    )
            )
        }
    }
}

/* ---------------- Seller Card ---------------- */

@Composable
private fun SellerCard(
    name: String,
    rating: Double,
    reviewCount: Int,
    photoUrl: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFEDEDED)) {
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.sample_avatar),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$rating", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("(${reviewCount}+Reviews)", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}


/* ---------------- Section Card ---------------- */

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(title, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 1.dp
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

/* ---------------- Info Row (2단 레이아웃처럼) ---------------- */

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF6B7280))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

/* ---------------- Bullet Text ---------------- */

@Composable
private fun BulletText(text: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text("•", modifier = Modifier.width(16.dp))
        Text(text)
    }
}

/* ---------------- Price Slider(읽기용) ---------------- */

@Composable
private fun PriceSlider() {
    Column {
        val ticks = listOf("4,010", "4,293", "4,576", "4,859", "5,142", "5,425")
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.Slider(
            value = 0.5f,
            onValueChange = {},
            enabled = false
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ticks.forEach { Text(it, fontSize = 10.sp, color = Color(0xFF9CA3AF)) }
        }
    }
}

/* ---------------- Bottom Action Bar ---------------- */

@Composable
private fun BottomActionBar(
    priceRange: String,
    onBuy: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth() // 전체 폭 사용
                    .clickable { onBuy() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2A5BFF)
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("구매문의", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun PriceBandReadonly(
    min: Long,
    max: Long,
    current: Long,
    modifier: Modifier = Modifier
) {
    // guard
    val safeMin = min.coerceAtMost(max)
    val safeMax = max.coerceAtLeast(min)
    val safeCur = current.coerceIn(safeMin, safeMax)

    val density = androidx.compose.ui.platform.LocalDensity.current
    var trackWidthPx by remember { mutableStateOf(0f) }

    // 현재가 x좌표(px)
    val thumbXPx = remember(safeMin, safeMax, safeCur, trackWidthPx) {
        if (safeMax == safeMin || trackWidthPx <= 0f) 0f
        else (safeCur - safeMin).toFloat() / (safeMax - safeMin).toFloat() * trackWidthPx
    }

    Column(modifier) {
        // ① 범위 텍스트
        Text(
            "${formatKrwPretty(safeMin)} ~ ${formatKrwPretty(safeMax)}",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        // ② 트랙 + 하이라이트 + 썸(점)
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onGloballyPositioned { trackWidthPx = it.size.width.toFloat() }
        ) {
            // 트랙
            Canvas(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val h = size.height
                val r = h / 2f
                // 바탕 트랙
                drawRoundRect(
                    color = Color(0xFFE5E7EB),
                    size = androidx.compose.ui.geometry.Size(size.width, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                )
                // 하이라이트(전체 밴드를 표시하므로 전체 폭을 파란색으로)
                drawRoundRect(
                    color = Color(0xFF2A5BFF),
                    size = androidx.compose.ui.geometry.Size(size.width, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                )
            }

            // 현재가 점(썸)
            Box(
                Modifier
                    .size(16.dp)
                    .align(Alignment.CenterStart)
                    .absoluteOffset(
                        x = with(density) { thumbXPx.toDp() - 8.dp }  // 점의 중심이 오게 - 반지름
                    )
                    .clip(CircleShape)
                    .background(Color.White) // 테두리 대비
            )
            Box(
                Modifier
                    .size(12.dp)
                    .align(Alignment.CenterStart)
                    .absoluteOffset(
                        x = with(density) { thumbXPx.toDp() - 6.dp }
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF2A5BFF))
            )
        }

        // ③ 아래 레이블 (min / 현재 / max)
        Box(Modifier.fillMaxWidth()) {
            Text(
                formatKrwPretty(safeMin),
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                formatKrwPretty(safeCur),
                color = Color(0xFF111827),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .absoluteOffset(x = with(density) { thumbXPx.toDp() - 20.dp }) // 대충 가운데 오도록 살짝 보정
            )
            Text(
                formatKrwPretty(safeMax),
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

