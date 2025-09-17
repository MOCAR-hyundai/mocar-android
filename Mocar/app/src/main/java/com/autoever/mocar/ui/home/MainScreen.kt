package com.autoever.mocar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autoever.mocar.ui.chat.ChatScreen
import com.autoever.mocar.ui.mypage.MyPageScreen
import com.autoever.mocar.ui.navigation.BottomNavItem
import com.autoever.mocar.ui.search.SearchPage
import com.autoever.mocar.ui.sell.SellCarScreen

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.BuyCar,
        BottomNavItem.SellCar,
        BottomNavItem.Search,
        BottomNavItem.Chat,
        BottomNavItem.MyPage
    )

    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route ?: BottomNavItem.BuyCar.route

            MocarBottomBarPill(
                items = items,
                selectedRoute = currentRoute,
                onSelect = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.BuyCar.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(BottomNavItem.BuyCar.route)  { HomeScreen(navController=rootNavController) }
            composable(BottomNavItem.SellCar.route) { SellCarScreen() }
            composable(BottomNavItem.Search.route)  { SearchPage() }
            composable(BottomNavItem.Chat.route)    { ChatScreen() }
            composable(BottomNavItem.MyPage.route)  { MyPageScreen(
                userName = "홍길동",
                userEmail = "hong@domain.com",
                profileImageUrl = null,
                onEditProfileClick = {},
                onWishListClick = {},
                onPurchaseListClick = {},
                onRegisterListClick = {},
                onSettingsClick = {},
                onLogoutClick = {}
            ) }
        }
    }
}

@Composable
private fun MocarBottomBarPill(
    items: List<BottomNavItem>,
    selectedRoute: String,
    onSelect: (String) -> Unit
) {
    val barShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val blue = Color(0xFF2563EB)
    val iconGray = Color(0xFF6B7280)
    val textGray = Color(0xFF111827)

    Surface(
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = barShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { item ->
                val selected = item.route == selectedRoute

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) blue else Color.Transparent)
                        .clickable { onSelect(item.route) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp),
                            tint = if (selected) Color.White else iconGray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            color = if (selected) Color.White else textGray
                        )
                    }
                }
            }
        }
    }
}