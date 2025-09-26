package com.autoever.mocar.ui.common.component.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun BrandChip(
    brand: BrandUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Color(0xFF2A5BFF) else Color(0xFFE5E7EB),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (brand.logoUrl != null) {
                AsyncImage(
                    model = brand.logoUrl,
                    contentDescription = brand.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB))
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // 괄호 앞에서 줄바꿈 되도록 처리
        val displayName = brand.name
            .replace(" (", "\n(")  // "쉐보레 (GM대우)" 같은 경우
            .replace("(", "\n(")  // 그냥 "("만 있는 경우도 커버

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),               // 텍스트 영역 높이 고정 (2줄 기준)
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName,
                fontSize = 12.sp,
                lineHeight = 16.sp,           // 두 줄일 때도 일정한 라인 높이
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}