package com.autoever.mocar.ui.home

import android.content.IntentSender.OnFinished
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoever.mocar.R
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    // 간단한 페이드 인
    var started by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (started) 1f else 0f, label = "splashAlpha")

    LaunchedEffect(Unit) {
        started = true
        delay(1600)
        onFinished()  // 자동 이동
    }

    Box(Modifier.fillMaxSize()) {
        // 배경 이미지 (로컬 drawable)
        Image(
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 어두운 그라데이션
        // 살짝 어두워지는 스크림 + 아래쪽 그라데이션 (두번째 스샷 느낌)
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.35f)
                    )
                )
        )

        // 내용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .alpha(alpha),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 위 공간을 더 주어 텍스트를 '아래로'
            Spacer(Modifier.height(70.dp))

            Column {
                Text(
                    text = "중고차 거래를\n더 간편하게",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 40.sp,
                    lineHeight = 56.sp,
                    style = MaterialTheme.typography.displaySmall.copy(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Top,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_mocar_onboarding),
                    contentDescription = "Mocar Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.60f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}