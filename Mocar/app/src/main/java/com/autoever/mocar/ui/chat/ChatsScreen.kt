package com.autoever.mocar.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.autoever.mocar.ui.common.component.atoms.MocarTopBar
import com.autoever.mocar.viewmodel.ChatsViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(
    navController: NavController,
    vm: ChatsViewModel = viewModel(
        factory = ChatsViewModel.factory()
    )
) {
    val rows by vm.rooms.collectAsState()

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
        if (rows.isEmpty()) {
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
                items(rows, key = { it.id }) { row ->
                    ChatListRow(
                        data = row,
                        onClick = { navController.navigate("chat_room/${row.id}") }
                    )
                    Divider(color = Color(0xFFEAEAEA))
                }
            }
        }
    }
}

@Composable
private fun ChatListRow(
    data: ChatRowUi,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아바타
        AsyncImage(
            model = data.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB))
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    data.partnerName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    data.listingTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                data.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                maxLines = 1
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            formatDate(data.lastAt),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9CA3AF)
        )
    }
}

private fun formatDate(millis: Long): String {
    if (millis <= 0) return ""
    val cal = Calendar.getInstance()
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return if (millis >= today.timeInMillis) {
        SimpleDateFormat("a h:mm", Locale.getDefault()).format(Date(millis))
    } else {
        SimpleDateFormat("M월 d일", Locale.getDefault()).format(Date(millis))
    }
}
