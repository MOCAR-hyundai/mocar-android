package com.autoever.mocar.ui.common.component.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.autoever.mocar.ui.common.util.formatKrwPretty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class CarUi(
    val id: String,
    val title: String,
    val mileageKm: Int,
    val region: String,
    val priceKRW: Long,
    val imageUrl: String? = null,
    val imageRes: Int? = null
)

@Composable
fun CarCard(
    navController: NavController,
    car: CarUi,
    modifier: Modifier = Modifier
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var isFavorite by remember { mutableStateOf(false) }

    if (user != null) {
        LaunchedEffect(car.id, user.uid) {
            db.collection("favorites")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("listingId", car.id)
                .get()
                .addOnSuccessListener { snapshot ->
                    isFavorite = !snapshot.isEmpty
                }
        }
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        onClick = { navController.navigate("carDetail/${car.id}") }
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                when {
                    car.imageUrl != null -> {
                        AsyncImage(
                            model = car.imageUrl,
                            contentDescription = car.title,
                            modifier = Modifier.width(375.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    car.imageRes != null -> {
                        Image(
                            painter = painterResource(car.imageRes),
                            contentDescription = car.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF3F4F6))
                        )
                    }
                }
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "찜",
                    tint = if (isFavorite) Color.Red else Color(0xFF111827),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .clickable {
                            val favoritesRef = db.collection("favorites")
                            if (isFavorite && user != null) {
                                favoritesRef
                                    .whereEqualTo("userId", user.uid)
                                    .whereEqualTo("listingId", car.id)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        for (doc in snapshot.documents) {
                                            favoritesRef.document(doc.id).delete()
                                        }
                                        isFavorite = false
                                    }
                                } else {
                                // 찜 추가
                                val newFavorite = hashMapOf(
                                    "userId" to (user?.uid ?: ""),
                                    "listingId" to car.id,
                                    "createdAt" to com.google.firebase.Timestamp.now()
                                )
                                favoritesRef.add(newFavorite)
                                    .addOnSuccessListener { isFavorite = true }
                            }
                        }
                )
            }
            Divider(color = Color(0xFFEDEDED), thickness = 1.dp)
            Column(Modifier.background(Color.White).padding(12.dp)) {
                Text(
                    car.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AccessTime, null,
                        tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${car.mileageKm}km", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Outlined.Place, null,
                        tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(car.region, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
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