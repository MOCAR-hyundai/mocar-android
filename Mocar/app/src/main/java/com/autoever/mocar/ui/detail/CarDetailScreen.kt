package com.autoever.mocar.ui.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoever.mocar.R
import com.autoever.mocar.model.Car
import com.autoever.mocar.ui.home.formatKrwPretty

@Composable
fun CarDetailScreen(
    car: Car?,
    onBack: () -> Unit,
    onToggleFavorite: (Car) -> Unit
) {
    // car가 null이면 간단히 종료
    if (car == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("차량 정보를 찾을 수 없습니다.")
        }
        return
    }

    Scaffold(
        topBar = {
            DetailTopBar(
                plateNo = car.plateNo,
                onBack = onBack,
                onMore = { /* TODO */ }
            )
        },
        bottomBar = {
            BottomActionBar(
                priceRange = formatKrwPretty(car.priceKRW),
                onChat = { /* TODO: 채팅 이동 */ },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color.White)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = car.imageRes),
                            contentDescription = car.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                        DotsIndicator(total = 4, selected = 1)
                    }
                    var isFav by remember { mutableStateOf(car.isFavorite) }
                    HeartButton(
                        isFavorite = isFav,
                        onClick = {
                            isFav = !isFav
                            onToggleFavorite(car.copy(isFavorite = isFav))
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
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
                SellerCard(
                    name = "Hela Quintin",
                    rating = 5.0,
                    reviewCount = 100
                )
            }

            // 기본 정보
            item {
                SectionCard(title = "기본 정보") {
                    InfoRow("차량번호", car.plateNo)
                    InfoRow("연식", car.yearDesc)
                    InfoRow("주행거리", "${String.format("%,d", car.mileageKm)}km")
                    InfoRow("변속기", car.transmission)
                    InfoRow("차종", car.carType)
                    InfoRow("배기량", "${car.displacement}cc")
                    InfoRow("연료", car.fuel)
                }
            }

            // 이 차의 상태
            item {
                SectionCard(title = "이 차의 상태") {
                    BulletText("실내외 사용감이 다소 있습니다.")
                    BulletText("조수석 쪽 휀더 경미한 흠집이 있습니다.")
                    BulletText("조수석 도어 외부 도어캐치 부근 경미한 흠집/자국이 있습니다.")
                }
            }

            // 시세
            item {
                SectionCard(title = "시세") {
                    Text("시세안전구간", color = Color(0xFF6B7280), fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("4,254~5,180만원", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    PriceSlider() // 시각적 슬라이더 (읽기용)
                }
            }
        }
    }
}

/* ---------------- TopBar ---------------- */

@Composable
private fun DetailTopBar(
    plateNo: String,
    onBack: () -> Unit,
    onMore: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(38.dp)) {
                Icon(painterResource(id = R.drawable.ic_back), contentDescription = "뒤로",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
            }
            Text(plateNo, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onMore,
                modifier = Modifier.size(38.dp)) {
                Icon(painterResource(id = R.drawable.ic_more), contentDescription = "더보기",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
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
            tint = Color.Black, // 항상 검정색
            modifier = Modifier.size(28.dp)
        )
    }
}

/* ---------------- Dots ---------------- */

@Composable
private fun DotsIndicator(total: Int, selected: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
        repeat(total) { idx ->
            Box(
                modifier = Modifier
                    .size(if (idx + 1 == selected) 7.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (idx + 1 == selected) Color(0xFF2A5BFF) else Color(0xFFE5E7EB))
            )
        }
    }
}

/* ---------------- Seller Card ---------------- */

@Composable
private fun SellerCard(name: String, rating: Double, reviewCount: Int) {
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
                    Image(
                        painter = painterResource(id = R.drawable.sample_avatar), // 없으면 기본 원
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Crop
                    )
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
    onChat: () -> Unit,
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
                    .weight(1f)
                    .clickable { onChat() },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_chat), contentDescription = null) // 없으면 Icons.Default.Chat
                    Spacer(Modifier.width(6.dp))
                    Text("채팅")
                }
            }

            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .weight(2f)
                    .clickable { onBuy() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2A5BFF)
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("구매하기", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
