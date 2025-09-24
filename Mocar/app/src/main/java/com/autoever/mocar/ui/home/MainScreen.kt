package com.autoever.mocar.ui.home

import ROUTE_SEARCH
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autoever.mocar.ui.chat.ChatsScreen
import com.autoever.mocar.ui.mypage.MyPageScreen
import com.autoever.mocar.ui.navigation.BottomNavItem
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

    var homeScrollSignal by remember { mutableStateOf(0) }

    // 현재 선택된 탭
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavItem.BuyCar.route) }
    // 우리가 관리하는 탭 백스택 (이전 탭들)
    val tabBackStack = remember { mutableStateListOf<String>() }

    fun navigateToTab(route: String, fromBack: Boolean = false) {
        if (!fromBack && route != selectedTab) {
            tabBackStack.add(selectedTab)           // 현재 탭을 스택에 쌓고 이동
        }
        selectedTab = route
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goPrevTabOrHome() {
        val target = tabBackStack.lastOrNull() ?: BottomNavItem.BuyCar.route
        if (tabBackStack.isNotEmpty()) {
            tabBackStack.removeAt(tabBackStack.lastIndex)
        }
        navigateToTab(target, fromBack = true)
    }
    // 안드로이드 하드웨어 뒤로가기: 탭 루트에 있으면 이전 탭/홈으로
    BackHandler(enabled = true) {
        goPrevTabOrHome()
    }

    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        bottomBar = {
            MocarBottomBarPill(
                items = items,
                selectedRoute = selectedTab,
                onSelect = { route ->
                    if (route == selectedTab) {
                        //같은 탭을 다시 클릭한 경우
                        if (route == BottomNavItem.BuyCar.route) {
                            homeScrollSignal++      // home으로 올라가라고 신호
                        }
                        return@MocarBottomBarPill
                    }
                    if (route == BottomNavItem.Search.route) {
                        rootNavController.navigate(ROUTE_SEARCH) // SearchPage로 이동
                        return@MocarBottomBarPill
                    }
                    navigateToTab(route)
                }
            )
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.BuyCar.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(BottomNavItem.BuyCar.route)  {
                HomeRoute(
                    navController = rootNavController,
                    scrollSignal = homeScrollSignal
                )
            }
            composable(BottomNavItem.SellCar.route) { SellCarScreen() }
            composable(BottomNavItem.Search.route)  {
//                rootNavController.navigate(ROUTE_SEARCH)
            }
            composable(BottomNavItem.Chat.route)    { ChatsScreen(navController = rootNavController,
                                                                    onBack = { goPrevTabOrHome() }) }
            composable(BottomNavItem.MyPage.route)  { MyPageScreen(
                navController = rootNavController,
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
        modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
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
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(20.dp),
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