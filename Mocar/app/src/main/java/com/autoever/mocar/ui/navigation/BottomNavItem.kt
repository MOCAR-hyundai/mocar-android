package com.autoever.mocar.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object BuyCar  : BottomNavItem("buy_car",  Icons.Filled.Home,           "내차사기")
    object SellCar : BottomNavItem("sell_car", Icons.Filled.DirectionsCar,  "내차팔기")
    object Search  : BottomNavItem("search",   Icons.Filled.Search,         "검색")
    object Chat    : BottomNavItem("chat",     Icons.Filled.Chat,           "채팅")
    object MyPage  : BottomNavItem("my_page",  Icons.Filled.Person,         "마이")
}
