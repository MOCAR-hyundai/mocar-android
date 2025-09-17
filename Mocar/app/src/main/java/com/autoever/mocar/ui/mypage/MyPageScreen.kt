package com.autoever.mocar.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 1. 프로필 정보 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (profileImageUrl != null) {
                // Coil 등 이미지 라이브러리 사용 필요
                // 예시: AsyncImage(model = profileImageUrl, contentDescription = "프로필 사진", ...)
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
                        text = userName.firstOrNull()?.toString() ?: "",
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
                    text = userName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Edit, // androidx.compose.material.icons.Icons.Default.Edit 사용
                    contentDescription = "프로필 수정",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "프로필 수정",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
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
            Column {
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
                            color = MaterialTheme.colorScheme.primary
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
            Column {
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