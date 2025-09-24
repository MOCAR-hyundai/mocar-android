package com.autoever.mocar.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.autoever.mocar.domain.model.Message
import com.autoever.mocar.ui.common.component.atoms.MocarTopBar
import com.autoever.mocar.viewmodel.ChatRoomViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val ChatBg = Color(0xFFF7F7F9)        // 전체 배경
private val BubbleMine = Color(0xFF2A5BFF)     // 내 말풍선 파랑
private val BubbleOther = Color(0xFFEDEEF2)   // 상대 말풍선 회색
private val TimeGray = Color(0xFF9CA3AF)

@Composable
fun ChatRoomScreen(
    chatId: String,
    vm: ChatRoomViewModel = viewModel(
        factory = ChatRoomViewModel.factory(chatId)
    ),
    onBack: () -> Unit = {}
) {
    val messages by vm.messages.collectAsState()
    val partner by vm.partner.collectAsState(initial = null) // VM에서 상대 이름/사진 제공
    var input by remember { mutableStateOf("") }

    // 채팅화면에서만 상태바 흰색 + 다크아이콘
    val sysUi = rememberSystemUiController()
    DisposableEffect(Unit) {
        sysUi.setStatusBarColor(Color.White, darkIcons = true)
        onDispose { /* 필요시 복원 */ }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            MocarTopBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFFE5E7EB)) {
                            if (!partner?.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = partner?.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(34.dp)
                                )
                            } else {
                                Box(Modifier.size(34.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            partner?.name ?: "상대방",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                onBack = onBack,
                onMore = { /* TODO: 더보기 메뉴 */ }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = input,
                onValueChange = { input = it },
                onSend = {
                    val t = input.trim()
                    if (t.isNotEmpty()) {
                        vm.send(t)
                        input = ""
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(ChatBg)
        ) {
            if (messages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("메시지를 입력해 대화를 시작해 보세요.", color = TimeGray)
                }
            } else {
                val messages by vm.messages.collectAsState()
                val listState = rememberLazyListState()
                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.lastIndex)
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(messages, key = { _, m -> m.id }) { index, msg ->
                        // 날짜 구분선 (전 메시지와 날짜가 다르면 출력)
                        val prev = messages.getOrNull(index - 1)
                        if (prev == null || !isSameDay(prev.createdAt, msg.createdAt)) {
                            DayDivider(msg.createdAt)
                            Spacer(Modifier.height(6.dp))
                        }
                        MessageRow(msg)
                    }
                }
            }
        }
    }
}

/* ---- 말풍선 ---- */
@Composable
private fun MessageRow(msg: Message) {
    val screenW = LocalConfiguration.current.screenWidthDp
    val maxBubbleWidth = (screenW * 0.72f).dp     // 화면의 72%까지만

    val bubbleShape = RoundedCornerShape(10.dp)   // 둥근 네모(조금 각지게)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),   // 옆 여백 최소화
        horizontalArrangement = if (msg.mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom                 // 시간 텍스트 하단 정렬
    ) {
        if (msg.mine) {
            // 내 메시지: 시간 ← 버블
            Text(
                text = timeLabel(msg.createdAt),
                fontSize = 11.sp,
                color = TimeGray,
                modifier = Modifier.padding(end = 6.dp)
            )
            Surface(
                shape = bubbleShape,
                color = BubbleMine,
                modifier = Modifier.widthIn(max = maxBubbleWidth) // ★ 가로폭 제한
            ) {
                Text(
                    text = msg.text.orEmpty(),
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        } else {
            // 상대 메시지: 버블 → 시간
            Surface(
                shape = bubbleShape,
                color = BubbleOther,
                modifier = Modifier.widthIn(max = maxBubbleWidth) // ★ 가로폭 제한
            ) {
                Text(
                    text = msg.text.orEmpty(),
                    color = Color.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Text(
                text = timeLabel(msg.createdAt),
                fontSize = 11.sp,
                color = TimeGray,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

/* ---- 입력 바 ---- */
@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(shadowElevation = 10.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                placeholder = { Text("메시지를 입력하세요") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                color = Color(0xFF2A5BFF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(46.dp)
                    .width(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSend() }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("전송", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val ca = java.util.Calendar.getInstance().apply { timeInMillis = a }
    val cb = java.util.Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR) &&
            ca.get(java.util.Calendar.DAY_OF_YEAR) == cb.get(java.util.Calendar.DAY_OF_YEAR)
}

@Composable
private fun DayDivider(millis: Long) {
    val fmt = remember { java.text.SimpleDateFormat("yyyy년 M월 d일", java.util.Locale.getDefault()) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            color = Color(0xFFF0F2F5),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = fmt.format(java.util.Date(millis)),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color(0xFF6B7280),
                fontSize = 12.sp
            )
        }
    }
}


/* ---- 시간 포맷 ---- */
@Composable
private fun timeLabel(millis: Long): String {
    val sdf = remember { java.text.SimpleDateFormat("a h:mm", java.util.Locale.getDefault()) }
    return sdf.format(java.util.Date(millis))
}
