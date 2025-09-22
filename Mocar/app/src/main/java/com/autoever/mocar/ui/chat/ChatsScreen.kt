package com.autoever.mocar.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoever.mocar.ui.common.component.atoms.MocarTopBar
import com.autoever.mocar.viewmodel.ChatsViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ChatsScreen(
    navController: NavController,
    vm: ChatsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ChatsViewModel.factory()
    )
) {
    val rooms by vm.rooms.collectAsState()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,   // ← 상태바를 흰색으로
            darkIcons = true       // 아이콘은 검은색
        )
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            MocarTopBar(
                title = { Text("Chats", style = MaterialTheme.typography.titleMedium) },
                onBack = { navController.popBackStack() },
                onMore = { /* TODO: 메뉴 */ }
            )
        }
    ) { inner ->
        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .background(Color(0xFFF8F8F8)),
                contentAlignment = Alignment.Center
            ) {
                Text("아직 채팅이 없어요.\n관심있는 매물에서 채팅을 시작해 보세요!", lineHeight = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(rooms, key = { it.id }) { room ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("chat_room/${room.id}") }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            room.listingTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(room.lastMessage.ifBlank { "대화를 시작해 보세요" }, maxLines = 1)
                    }
                    Divider()
                }
            }
        }
    }
}
