package com.autoever.mocar.ui.search

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoever.mocar.R

data class carDatas(
    val name: String,
    val count: Int,
    val imageRes: Int
)

@Composable
fun Manufacturer(navController: NavController, searchQuery: String) {

    val korCars = listOf(
        carDatas("현대", 14325, R.drawable.brand_hyundai),
        carDatas("기아", 23426, R.drawable.brand_kia),
        carDatas("제네시스", 1234, R.drawable.brand_genesis),
        carDatas("쉐보레", 3453, R.drawable.brand_chevrolet),
        carDatas("르노코리아", 45, R.drawable.brand_renault)
    )

    val forCars = listOf(
        carDatas("BMW", 1598, R.drawable.brand_bmw),
        carDatas("벤츠", 3457, R.drawable.brand_benz),
        carDatas("아우디", 12345, R.drawable.brand_audi),
        carDatas("테슬라", 12351, R.drawable.brand_tesla),
        carDatas("페라리", 23456, R.drawable.brand_ferrari)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        item {
            CategoryLabel("국산차")
        }
        items(korCars) { (name, count, imageRes) ->
            ManufacturerCard(name = name, count = count, imageRes = imageRes, onClick = {
                navController.navigate("modelSelect")

            })
        }

        item {
            CategoryLabel("수입차")
        }
        items(forCars) { (name, count, imageRes) ->
            ManufacturerCard(name = name, count = count, imageRes = imageRes, onClick = {
                navController.navigate("modelSelect")

            })
        }
    }
}

@Composable
fun CategoryLabel(text: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .wrapContentSize()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(50) // 양 끝 둥글게
            )
            .border(
                width = 0.5.dp,
                color = Color(0xFFD7D7D7),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp), // 내부 여백 (글자보다 조금 크게)
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun ManufacturerCard(
    name: String,
    count: Int,
    imageRes: Int,
    onClick: () -> Unit
    ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .border(
                width = 0.2.dp,
                color = Color(0xFFD7D7D7),
                shape = RectangleShape
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 로고 이미지 + 이름
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "$name 로고",
                    modifier = Modifier
                        .height(30.dp)
                        .width(70.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 개수 + 화살표
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%,d".format(count),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "다음",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}

