package com.autoever.mocar.ui.mypage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyPageScreen(
    navController: NavHostController,
    onRegisterListClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    val context = LocalContext.current

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
                LaunchedEffect(user.uid) {
                    if (!isUserLoaded) {
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
//                Column(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(12.dp))
//                        .clickable { onEditProfileClick() }
//                        .padding(8.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Edit,
//                        contentDescription = "회원정보 수정",
//                        modifier = Modifier
//                            .size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = "회원정보 수정",
//                        style = MaterialTheme.typography.labelMedium,
//                    )
//                }
            }
        } else {
            // 로그인 안 됨: 전체 영역 "로그인해주세요"로 대체, 클릭 시 로그인 화면으로 이동
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("login")
                    },
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 나의 찜 매물
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("like_list") }
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
                    .clickable {
                        navController.navigate("buy_list")
                    }
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
                    .clickable { navController.navigate("sell_list") }
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

        // 3. Account 영역
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "비밀번호 수정",
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "비밀번호 수정",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "이동",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // TODO: 공통컴포넌트로 모달 창 만들기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        auth.signOut()
                        navController.navigate("login")
                    }
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

            // TODO: 공통컴포넌트로 모달 창 만들기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (user != null) {
                            // Firestore 유저 문서 먼저 삭제
                            db.collection("users").document(user.uid)
                                .delete()
                                .addOnSuccessListener {
                                    // Firebase Auth 계정 삭제
                                    user.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login") {
                                                popUpTo("mypage") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "재로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login") {
                                                popUpTo("mypage") { inclusive = true }
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "유저 데이터 삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonRemove,
                        contentDescription = "회원 탈퇴",
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "회원 탈퇴",
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
}