package com.autoever.mocar.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(navController : NavController) {
    var searchText by remember { mutableStateOf("") }
    var selectedMenu by remember { mutableStateOf("제조사") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            BottomButtons()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(innerPadding)
        ) {
            // 상단 검색창
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                SearchBar()
            }

            // 최근 검색 기록
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text("최근검색기록")
            }

            // 본문 영역 (좌측 메뉴 + 우측 내용)
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFD7D7D7),
                        shape = RectangleShape
                    )
            ) {
                LeftMenu(selected = selectedMenu) { clicked ->
                    selectedMenu = clicked
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedMenu) {
                        "제조사" -> Manufacturer(navController = navController, "")
                        "가격" -> Price()
                        "연식" -> Year()
                        "주행거리" -> Mileage()
                        "차종" -> CarType()
                        "연료" -> Fuel()
                        "지역" -> Region()
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String = "",
    onValueChange: (String) -> Unit = {}
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
            placeholder = { Text("차량번호를 검색해보세요") },
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
    }
}

@Composable
fun BottomButtons() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { /* 초기화 */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
                .height(60.dp)
        ) {
            Text(text = "초기화",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = { /* 차량 보기 */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(2f)
                .height(60.dp)
        ) {
            Text(
                text = "2,536대 보기",
                fontSize = 16.sp,
            )
        }
    }
}
