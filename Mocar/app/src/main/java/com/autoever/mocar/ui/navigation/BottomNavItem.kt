package com.autoever.mocar.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.autoever.mocar.R

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val iconRes: Int,
    val label: String
) {
    object BuyCar  : BottomNavItem("buy_car",  R.drawable.ic_tab_home, "내차사기")
    object SellCar : BottomNavItem("sell_car", R.drawable.ic_tab_sell, "내차팔기")
    object Search  : BottomNavItem("search",   R.drawable.ic_tab_search, "검색")
    object Chat    : BottomNavItem("chat",     R.drawable.ic_tab_chat, "채팅")
    object MyPage  : BottomNavItem("my_page",  R.drawable.ic_tab_my, "마이")
}
