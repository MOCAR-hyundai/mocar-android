package com.autoever.mocar.ui.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.ui.common.component.atoms.MocarTopBar
import com.autoever.mocar.ui.common.util.formatKrwPretty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

@Composable
fun SellListScreen(
    navController: NavHostController,
    onCarClick: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val collectionName = "listings"

    var listingItems by remember { mutableStateOf<List<Order>>(emptyList()) }
    var soldCars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Firebase에서 판매 목록 가져오기
    LaunchedEffect(user?.uid) {
        if (user == null) {
            errorMessage = "로그인이 필요합니다"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null

            val orderSnapshot = db.collection(collectionName)
                .whereEqualTo("sellerId", user.uid)
                .get()
                .await()

            val orders = orderSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }
            listingItems = orders

            soldCars = if (orders.isNotEmpty()) {
                val listingIds = orders.map { it.listingId }
                withContext(Dispatchers.IO) {
                    listingIds.map { id ->
                        async {
                            try {
                                val doc = db.collection("listings").document(id).get().await()
                                doc.toObject(ListingDto::class.java)?.toCar()
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
            } else emptyList()


        } catch (e: Exception) {
            errorMessage = "판매 목록 불러오기 실패: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            MocarTopBar(
                title = { Text("나의 등록 매물") },
                onBack = { navController.popBackStack() },
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
                soldCars.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("판매중인 매물이 없습니다", fontSize = 18.sp, color = Color.Gray)
                    Text("보유중인 차량을 등록해보세요", fontSize = 14.sp, color = Color.Gray)
                }
                else -> {
                    // 필터 상태 변수
                    var filterStatus by remember { mutableStateOf("전체") }

                    Column {
                        // 1. 필터
                        SellListFilter(
                            selectedStatus = filterStatus,
                            onStatusChange = { filterStatus = it }
                        )

                        // 2. 필터 적용 후 리스트
                        val filteredCars = when(filterStatus) {
                            "전체" -> soldCars
                            "판매중" -> soldCars.filter { it.status == "on_sale" }
                            "예약중" -> soldCars.filter { it.status == "reserved" }
                            "판매 완료" -> soldCars.filter { it.status == "sold" }
                            else -> soldCars
                        }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val carPairs = filteredCars.chunked(2)
                        items(carPairs.size) { idx ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                carPairs[idx].forEach { car ->
                                    CarSellWithStatus(
                                        car = car,
                                        onClick = { onCarClick(car.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (carPairs[idx].size < 2) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
    }

// 필터
@Composable
fun SellListFilter(
    selectedStatus: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val options = listOf("전체", "판매중", "예약중", "판매 완료")

    Box(
        modifier = Modifier
            .padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable { expanded = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedStatus,
                fontSize = 16.sp,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onStatusChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


// 매물 카드
@Composable
fun CarSellWithStatus(
    car: Car,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        ) {
            Column {
                Box(Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                ) {
                    AsyncImage(
                        model = car.imageUrl,
                        contentDescription = car.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
                ) {
                    Text(
                        car.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${car.yearDesc} · ${car.mileageKm}km · ${car.fuel}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        formatKrwPretty(car.priceKRW),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A5BFF)
                        )
                    )
                }
            }
        }

        // 카드 전체 오버레이 + 중앙 텍스트
        if (car.status == "reserved" || car.status == "sold") {
            Box(
                modifier = Modifier
                    .matchParentSize() // 카드 전체 영역 덮기
                    .background(
                        Color(0x99000000),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (car.status == "reserved") "예약중" else "판매 완료",
                    color = Color.White,
                    fontSize = 20.sp,
                )
            }
        }
    }
}
