package com.autoever.mocar.ui.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.ui.common.component.molecules.CarGrid
import com.autoever.mocar.ui.common.component.molecules.CarUi
import com.autoever.mocar.ui.common.util.formatKrwPretty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

// order 데이터 모델
data class Order(
    val orderId: String = "",
    val listingId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val status: String = "",
    val contractPrice: Long = 0L,
    val reservedAt: String? = null,
    val soldAt: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyListScreen(
    navController: NavHostController,
    onCarClick: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val collectionName = "orders"

    var orderItems by remember { mutableStateOf<List<Order>>(emptyList()) }
    var boughtCars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Firebase에서 찜 목록 가져오기
    var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(user?.uid) {
        if (user == null) return@LaunchedEffect
        db.collection("favorites")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snap, _ ->
                favoriteIds = snap?.documents
                    ?.mapNotNull { it.getString("listingId") }
                    ?.toSet() ?: emptySet()
            }
    }
    LaunchedEffect(user?.uid) {
        if (user == null) {
            errorMessage = "로그인이 필요합니다"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null

            val boughtSnapshot = db.collection(collectionName)
                .whereEqualTo("buyerId", user.uid)
                .get()
                .await()

            val orders = boughtSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }
            orderItems = orders

            boughtCars = if (orders.isNotEmpty()) {
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
            errorMessage = "구매 목록 불러오기 실패: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("구입 내역") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
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

                boughtCars.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("구입한 매물이 없습니다", fontSize = 18.sp, color = Color.Gray)
                    Text("판매중인 차량을 구경해보세요", fontSize = 14.sp, color = Color.Gray)
                }

                else -> {
                    // Car → CarUi 매핑 후 2열 Grid로 표시
                    val carUis = boughtCars.map { car ->
                        car.toUi(isFavorite = favoriteIds.contains(car.id))
                    }
                    val ordersMap = orderItems.associateBy { it.listingId }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val carPairs = boughtCars.chunked(2)
                        items(carPairs.size) { idx ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                carPairs[idx].forEach { car ->
                                    val order = ordersMap[car.id]
                                    CarCardWithStatus(
                                        car = car,
                                        order = order,
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

@Composable
fun CarCardWithStatus(
    car: Car,
    order: Order?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick).width(260.dp).wrapContentHeight(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(
                    model = car.imageUrl,
                    contentDescription = car.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // 즐겨찾기 아이콘은 필요시 추가
            }
            Divider(color = Color(0xFFEDEDED), thickness = 1.dp)
            Column(Modifier.background(Color.White).padding(12.dp)) {
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
                order?.let {
                    when (it.status.lowercase()) {
                        "reserved" -> Text(
                            text = "예약일: ${it.reservedAt?.toKoreanDateFormat() ?: "-"}",
                            color = Color(0xFFFFA500),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        "sold" -> Text(
                            text = "판매일: ${it.soldAt?.toKoreanDateFormat() ?: "-"}",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
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
}

private fun String.toKoreanDateFormat(): String {
    // yyyy-MM-dd 형식 예상
    if (this.length < 10) return this
    val year = substring(0, 4)
    val month = substring(5, 7)
    val day = substring(8, 10)
    return "${year}년 ${month}월 ${day}일"
}

/* ---------------- 확장함수: Car → CarUi ---------------- */
private fun Car.toUi(isFavorite: Boolean) = CarUi(
    id = id,
    title = title,
    imageUrl = imageUrl,   // URL 사용
    imageRes = null,
    mileageKm = mileageKm,
    region = region,
    priceKRW = priceKRW,
    isFavorite = isFavorite
)
