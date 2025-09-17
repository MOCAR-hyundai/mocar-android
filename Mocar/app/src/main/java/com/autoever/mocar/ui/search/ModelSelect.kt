package com.autoever.mocar.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModelSelect(
    onBack: () -> Unit = {},
    onConfirm: (List<String>) -> Unit = {}
) {
    val exampleModels = listOf(
        "그랜저" to 8354,
        "포터" to 6911,
        "아반떼" to 5543,
        "쏘나타" to 4625,
        "싼타페" to 4272,
        "i30" to 363,
        "i40" to 254,
        "ST1" to 3,
        "갤로퍼" to 44,
        "그라나다" to 0,
    )

    var selectedModel by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            Spacer(Modifier.width(8.dp))
            Text("현대", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 경로 텍스트
        Text("제조사 > 모델 > 세부모델", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // 전체 모델 리스트
        exampleModels.forEach { (model, count) ->
            ModelRow(
                name = model,
                count = count,
                selected = selectedModel == model,
                onSelect = {
                    selectedModel = model
                    onConfirm(listOf(model)) // ← 한 개라도 클릭 시 바로 종료
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 선택 버튼
        Button(
            onClick = {
                val allModelNames = exampleModels.map { it.first }
                onConfirm(allModelNames) // ← 선택 버튼 누르면 전체 선택으로 간주
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("선택")
        }
    }
}

@Composable
fun ModelRow(
    name: String,
    count: Int,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%,d".format(count),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
