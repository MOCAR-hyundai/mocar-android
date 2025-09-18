package com.autoever.mocar.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MyPageScreen(
    userName: String,
    userEmail: String,
    profileImageUrl: String? = null,
    onEditProfileClick: () -> Unit,
    onWishListClick: () -> Unit,
    onPurchaseListClick: () -> Unit,
    onRegisterListClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        if (user != null) {
            // 1. 프로필 정보 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Firestore에서 유저 정보 가져오기 위한 상태
                var userNameState by remember { mutableStateOf<String?>(null) }
                var userEmailState by remember { mutableStateOf<String?>(null) }
                var userPhotoUrlState by remember { mutableStateOf<String?>(null) }
                var isUserLoaded by remember { mutableStateOf(false) }

                // Firestore에서 유저 정보 fetch
                LaunchedEffect(user?.uid) {
                    if (user != null && !isUserLoaded) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    userNameState = document.getString("name")
                                    userEmailState = document.getString("email")
                                    userPhotoUrlState = document.getString("photoUrl")
                                } else {
                                    userNameState = null
                                    userEmailState = null
                                    userPhotoUrlState = null
                                }
                                isUserLoaded = true
                            }
                            .addOnFailureListener {
                                userNameState = null
                                userEmailState = null
                                userPhotoUrlState = null
                                isUserLoaded = true
                            }
                    }
                }

                // 프로필 이미지
                if (!userPhotoUrlState.isNullOrBlank()) {
                    // Coil 등 이미지 라이브러리 사용 필요
                    // 예시: AsyncImage(model = userPhotoUrlState, contentDescription = "프로필 사진", ...)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userNameState?.firstOrNull()?.toString() ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userNameState ?: "이름이 존재하지 않습니다.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = userEmailState ?: "이메일이 존재하지 않습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEditProfileClick() }
                        .padding(8.dp)
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "프로필 수정",
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "프로필 수정",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        } else {
            // 로그인 안 됨: 전체 영역 "로그인해주세요"로 대체
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "로그인해주세요",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }


        // 2. My 영역
        Text(
            text = "My",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // 나의 찜 매물
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWishListClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 아이콘(하트) 원 안에
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "찜 매물",
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "나의 찜 매물",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "이동",
                    )
                }
                // 나의 구입 매물
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPurchaseListClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 아이콘(쇼핑카트) 원 안에
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = "구입 매물",
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "나의 구입 매물",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "이동",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 나의 등록 매물
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRegisterListClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 아이콘(업로드) 원 안에
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold

                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "나의 등록 매물",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "이동",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Account 영역
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSettingsClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "설정",
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "설정",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "이동",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogoutClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "로그아웃",
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "로그아웃",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "이동",
                    )

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MyPageScreen(
        userName = "홍길동",
        userEmail = "hong@domain.com",
        profileImageUrl = null,
        onEditProfileClick = {},
        onWishListClick = {},
        onPurchaseListClick = {},
        onRegisterListClick = {},
        onSettingsClick = {},
        onLogoutClick = {}
    )
}