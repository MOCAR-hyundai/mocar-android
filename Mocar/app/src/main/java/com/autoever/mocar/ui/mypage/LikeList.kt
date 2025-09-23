package com.autoever.mocar.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.ui.common.component.molecules.CarGrid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

// Like 데이터 모델
data class LikeItem(
    val fid: String = "",
    val userId: String = "",
    val listingId: String = "",
    val createdAt: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikeListScreen(
    navController: NavHostController,
    onCarClick: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val collectionName = "favorites"

    var likeItems by remember { mutableStateOf<List<LikeItem>>(emptyList()) }
    var likedCars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Firebase에서 찜 목록 가져오기
    LaunchedEffect(user?.uid) {
        if (user == null) {
            errorMessage = "로그인이 필요합니다"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null

            val likesSnapshot = db.collection(collectionName)
                .whereEqualTo("userId", user.uid)
                .get()
                .await()

            val likes = likesSnapshot.documents.mapNotNull { doc ->
                doc.toObject(LikeItem::class.java)?.copy(fid = doc.id)
            }
            likeItems = likes

            likedCars = if (likes.isNotEmpty()) {
                val listingIds = likes.map { it.listingId }
                withContext(Dispatchers.IO) {
                    listingIds.map { id ->
                        async {
                            try {
                                val doc = db.collection("listings").document(id).get().await()
                                doc.toObject(ListingDto::class.java)?.toCar(isFavorite = true)
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
            } else emptyList()

        } catch (e: Exception) {
            errorMessage = "찜 목록 불러오기 실패: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val removeFromLikes: (String) -> Unit = { carId ->
        val likeToRemove = likeItems.find { it.listingId == carId }
        likeToRemove?.let { like ->
            db.collection(collectionName).document(like.fid)
                .delete()
                .addOnSuccessListener {
                    likeItems = likeItems.filter { it.listingId != carId }
                    likedCars = likedCars.filter { it.id != carId }
                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("나의 찜 매물") },
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
                likedCars.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("찜한 매물이 없습니다", fontSize = 18.sp, color = Color.Gray)
                    Text("관심있는 차량을 찜해보세요", fontSize = 14.sp, color = Color.Gray)
                }
                else -> {
                    // Car → CarUi 매핑 후 2열 Grid로 표시
                    val carUis = likedCars.map { it.toCarUi() }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CarGrid(
                                cars = carUis,
                                onFavoriteToggle = removeFromLikes,
                                onCardClick = onCarClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- 확장함수: Car → CarUi ---------------- */
private fun Car.toCarUi() = com.autoever.mocar.ui.common.component.molecules.CarUi(
    id = id,
    title = title,
    imageUrl = imageUrl,
    imageRes = null,
    mileageKm = mileageKm,
    region = region,
    priceKRW = priceKRW,
    isFavorite = isFavorite
)


@Preview(showBackground = true)
@Composable
fun LikeListScreenPreview() {
    LikeListScreen(
        navController = androidx.navigation.compose.rememberNavController()
    )
}
