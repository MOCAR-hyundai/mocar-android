package com.autoever.mocar.ui.common.component.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.autoever.mocar.R

@Composable
fun MocarTopBar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null
) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back 버튼 (옵션)
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                }
            } else {
                Spacer(Modifier.size(38.dp))
            }

            // 가운데: 타이틀 슬롯 (가운데 정렬)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                title()
            }

            // 오른쪽: 더보기 또는 자리 맞춤
            if (onMore != null) {
                IconButton(
                    onClick = onMore,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more),
                        contentDescription = "더보기",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                }
            } else {
                Spacer(Modifier.size(38.dp))
            }
        }
    }
}

/** 텍스트만 쓰고 싶을 때 편의용 래퍼 */
@Composable
fun MocarTopBarText(
    titleText: String,
    onBack: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null
) {
    MocarTopBar(
        title = { Text(titleText, style = MaterialTheme.typography.titleMedium) },
        onBack = onBack,
        onMore = onMore
    )
}
