package com.autoever.mocar.ui.search

import ROUTE_SEARCH
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.autoever.mocar.R
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.viewmodel.SearchSharedViewModel

@Composable
fun SubModelSelect(
    navController: NavController,
    brandName: String,
    modelName: String,
    allListings: List<ListingDto>,
    searchSharedViewModel: SearchSharedViewModel,
    onBack: () -> Unit = {},
    onConfirm: (List<ListingDto>) -> Unit = {}
) {
    val subModels = allListings
        .filter { it.brand == brandName && it.model == modelName }
        .groupBy { it.title }
        .map { (title, list) ->
            Triple(title, list.firstOrNull()?.year ?: 0, list)
        }

    val selectedItems = remember { mutableStateListOf<ListingDto>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // 상단 경로 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(38.dp)) {
                androidx.compose.material3.Icon(
                    painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(text = modelName, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 경로 텍스트
        Text("제조사 > 모델", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(subModels.size) { index ->
                val (title, year, list) = subModels[index]
                val isSelected = selectedItems.containsAll(list)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedItems.removeAll(list)
                            else selectedItems.addAll(list)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 16.sp)
                        Text("$year", fontSize = 13.sp, color = Color.Gray)
                    }

                    Text("${list.size}대", color = Color.Gray)

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3058EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "선택됨",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                searchSharedViewModel.selectedBrand = brandName
                searchSharedViewModel.selectedModel = modelName
                searchSharedViewModel.selectedSubModels.clear()
                searchSharedViewModel.selectedSubModels.addAll(selectedItems.map { it.title }.distinct())

                println("✅ Brand: ${searchSharedViewModel.selectedBrand}")
                println("✅ Model: ${searchSharedViewModel.selectedModel}")
                println("✅ SubModels: ${searchSharedViewModel.selectedSubModels}")


                navController.navigate(ROUTE_SEARCH) {
                    popUpTo("sub_model_select/$brandName/$modelName") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("선택 완료 (${selectedItems.size})")
        }
    }
}
