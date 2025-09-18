package com.autoever.mocar.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel

import com.autoever.mocar.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    navController : NavController,
    onBack: () -> Unit,
    viewModel: SearchFilterViewModel = viewModel()
) {
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
                IconButton(onClick = onBack,
                    modifier = Modifier.size(38.dp)) {
                    Icon(painterResource(id = R.drawable.ic_back), contentDescription = "뒤로",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                SearchBar()
            }

            // 최근 검색 기록
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text("최근검색기록 ")
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "제조사 다시 선택",
                    tint = Color.Gray
                )
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
                val viewModel: SearchFilterViewModel = viewModel()

                LeftMenu(
                    selected = selectedMenu,
                    onSelect = { selectedMenu = it },
                    viewModel = viewModel
                )

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
            placeholder = { Text("제조사, 모델을 검색해보세요") },
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
fun LeftMenu(selected: String,
             onSelect: (String) -> Unit,
             viewModel: SearchFilterViewModel = viewModel()
) {

    val state by viewModel.filterState.collectAsState()
    val default = SearchFilterState() // 기본값

    val menuItems = listOf("제조사", "가격", "연식", "주행거리", "차종", "연료", "지역")

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxSize()
            .padding(start = 0.dp)
            .border(
                width = 0.5.dp,
                color = Color(0xFFD7D7D7),
                shape = RectangleShape
            ),
        verticalArrangement = Arrangement.Top,
    ) {
        menuItems.forEach { item ->
            val isChanged = when (item) {
                "가격"     -> state.priceRange != default.priceRange
                "연식"     -> state.yearRange != default.yearRange
                "주행거리" -> state.mileageRange != default.mileageRange
                "차종"     -> state.selectedTypes.isNotEmpty()
                "연료"     -> state.selectedFuels.isNotEmpty()
                "지역"     -> state.selectedRegions.isNotEmpty()
                else       -> false
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(if (selected == item) Color.White else Color(0xFFEDEDED))
                    .clickable { onSelect(item) },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 12.dp),
                        color = if (selected == item) Color(0xFF3058EF) else Color.Black,
                        fontWeight = if (selected == item) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    )

                    if (isChanged) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                                .background(Color.Red, shape = RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

// 버튼
@Composable
fun BottomButtons() {
    val viewModel: SearchFilterViewModel = viewModel()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { viewModel.resetAllFilters() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
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
            modifier = Modifier
                .weight(2f)
                .height(60.dp)
        ) {
            Text(
                text = "선택",
                fontSize = 16.sp,
            )
        }
    }
}

