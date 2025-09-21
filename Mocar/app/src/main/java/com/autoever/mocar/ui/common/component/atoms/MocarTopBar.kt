package com.autoever.mocar.ui.common.component.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
    title: String,
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
                // 없으면 Spacer 넣어도 됨
            }

            Text(title, style = MaterialTheme.typography.titleMedium)

            // More 버튼 (옵션)
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
                // 없으면 Spacer 넣어도 됨
            }
        }
    }
}
