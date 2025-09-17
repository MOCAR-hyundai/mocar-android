package com.autoever.mocar.ui.sell

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.autoever.mocar.R
import kotlin.math.min


/* ---------- Step 정의 ---------- */
private enum class SellStep {
    Plate, Owner, CarInfo, Odometer, Price, Extra, Photos, Review, Done
}

private val totalSteps = SellStep.entries.size - 1 // Done 제외한 진행도 계산용

/* ---------- 폼 데이터 ---------- */
private class SellForm {
    var plate     by mutableStateOf("")
    var owner     by mutableStateOf("")
    var modelName by mutableStateOf("현대 싼타페 CM 2WD(2.0 VGT) CLX 고급형")
    var yearDesc  by mutableStateOf("2015년식")
    var mileageKm by mutableStateOf("")
    var hopePrice by mutableStateOf("")
    var extra     by mutableStateOf("")
    val photos = mutableStateListOf<Uri>()
}

/* ---------- 메인 스크린 ---------- */
@Composable
fun SellCarScreen() {
    val focus = LocalFocusManager.current
    var step by remember { mutableStateOf(SellStep.Plate) }
    val form = remember { SellForm() }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom),
        topBar = {
            SellTopBar(
                title = when (step) {
                    SellStep.Plate -> "차량 번호 입력"
                    SellStep.Owner -> "소유자명 입력"
                    SellStep.CarInfo -> "차량 정보 확인"
                    SellStep.Odometer -> "주행 거리 입력"
                    SellStep.Price -> "희망 가격 입력"
                    SellStep.Extra   -> "추가 정보 입력"
                    SellStep.Photos -> "차량 사진 등록"
                    SellStep.Review -> "입력 내용 검토"
                    SellStep.Done -> "완료"
                },
                progress = when (step) {
                    SellStep.Done -> 1f
                    else -> (SellStep.entries.indexOf(step) + 1).toFloat() / totalSteps
                }.coerceIn(0f, 1f)
            )
        },
        bottomBar = {
            SellBottomBar(
                showPrev = step != SellStep.Plate && step != SellStep.Done,
                prevText = "이전",
                nextText = when (step) {
                    SellStep.Review -> "등록"
                    SellStep.Done -> "처음으로"
                    else -> "다음"
                },
                onPrev = {
                    if (step != SellStep.Plate) {
                        step = SellStep.entries[SellStep.entries.indexOf(step) - 1]
                    }
                },
                onNext = {
                    focus.clearFocus()
                    step = when (step) {
                        SellStep.Review -> SellStep.Done
                        SellStep.Done -> SellStep.Plate
                        else -> SellStep.entries[SellStep.entries.indexOf(step) + 1]
                    }
                },
                nextEnabled = when (step) {
                    SellStep.Plate -> form.plate.isNotBlank()
                    SellStep.Owner -> form.owner.isNotBlank()
                    SellStep.Odometer -> form.mileageKm.isNotBlank()
                    SellStep.Price -> form.hopePrice.isNotBlank()
                    else -> true
                }
            )
        }
    ) { inner ->
        // 화면 배경 보장
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .imePadding()
        ) {
            // AnimatedContent는 그대로 쓰되 transitionSpec 필요없으면 빼도 OK
            AnimatedContent(
                targetState = step,
                label = "step"
            ) { s ->
                when (s) {
                    SellStep.Plate    -> PlateStep(form)
                    SellStep.Owner    -> OwnerStep(form)
                    SellStep.CarInfo  -> CarInfoStep(form)
                    SellStep.Odometer -> OdometerStep(form)
                    SellStep.Price    -> PriceStep(form)
                    SellStep.Extra    -> ExtraStep(form)
                    SellStep.Photos   -> PhotosStep(form)
                    SellStep.Review   -> ReviewStep(form)
                    SellStep.Done     -> DoneStep()
                }
            }
        }
    }
}

/* ---------- 공통 UI: TopBar / BottomBar ---------- */

@Composable
private fun SellTopBar(title: String, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // 진행 바
        CleanProgressBar(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SellBottomBar(
    showPrev: Boolean,
    prevText: String,
    nextText: String,
    nextEnabled: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(shadowElevation = 10.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showPrev) {
                Button(
                    onClick = onPrev,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = Color(0xFF374151)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp
                    )
                ) {
                    Text(prevText, fontSize = 18.sp)
                }
            }
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(if (showPrev) 1f else 1f)
                    .height(52.dp),
                enabled = nextEnabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A5BFF),
                    disabledContainerColor = Color(0xFFBBD2FF)
                )
            ) { Text(nextText, fontSize = 18.sp) }
        }
    }
}

/* ---------- Step 1: 차량 번호 ---------- */
@Composable
private fun PlateStep(form: SellForm) {
    StepScaffold(
        headline = "내 차,\n시세를 알아볼까요?",
        input = {
            LabeledField(
                placeholder = "12가 1234",
                value = form.plate,
                onValueChange = { form.plate = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
    )
}

/* ---------- Step 2: 소유자명 ---------- */
@Composable
private fun OwnerStep(form: SellForm) {
    StepScaffold(
        headline = "소유자명을 입력해주세요.",
        input = {
            LabeledField(
                placeholder = "홍길동",
                value = form.owner,
                onValueChange = { form.owner = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
    )
}

/* ---------- Step 3: 차량정보 확인 (카드형 표) ---------- */
@Composable
private fun CarInfoStep(form: SellForm) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("차량 정보", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.sample_car_2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF4F4F6)),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(12.dp))
        Text(form.modelName, color = Color(0xFF6B7280))

        Spacer(Modifier.height(16.dp))
        InfoCard(
            rows = listOf(
                "차량 번호" to (form.plate.ifBlank { "—" }),
                "모델명" to form.modelName,
                "연식" to form.yearDesc
            )
        )
    }
}

/* ---------- Step 4: 주행거리 ---------- */
@Composable
private fun OdometerStep(form: SellForm) {
    StepScaffold(
        headline = "주행거리를 입력해주세요.",
        input = {
            LabeledField(
                placeholder = "예: 85,000km",
                value = form.mileageKm,
                onValueChange = { form.mileageKm = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )
}

/* ---------- Step 5: 희망가격 ---------- */
@Composable
private fun PriceStep(form: SellForm) {
    StepScaffold(
        headline = "판매가격을 입력해주세요.",
        input = {
            LabeledField(
                placeholder = "예: 12,000,000(원)",
                value = form.hopePrice,
                onValueChange = { form.hopePrice = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )
}

/* ---------- Step: 추가 정보 ---------- */
@Composable
private fun ExtraStep(form: SellForm) {
    StepScaffold(
        headline = "추가정보를 입력해주세요.",
        input = {
            OutlinedTextField(
                value = form.extra,
                onValueChange = { form.extra = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp)
                    .padding(top = 4.dp),
                placeholder = { Text("추가 정보 입력") },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF111827),
                    focusedBorderColor   = Color(0xFF2A5BFF)
                )
            )
        }
    )
}

/* ---------- Step 6: 사진 등록 ---------- */
@Composable
private fun PhotosStep(form: SellForm) {
    val pickMulti = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            form.photos.addAll(uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "차량사진을 등록해주세요.",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(16.dp))

        // 대형 미리보기
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            if (form.photos.isNotEmpty()) {
                AsyncImage(
                    model = form.photos.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("사진을 추가해주세요", color = Color(0xFF9CA3AF))
            }
        }

        Spacer(Modifier.height(16.dp))

        // 썸네일 목록 + 추가 버튼
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedButton(
                    onClick = {
                        pickMulti.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2A5BFF)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF7F7F9),
                        contentColor = Color(0xFF2A5BFF)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Add,
                            contentDescription = null)
                        Spacer(Modifier.height(4.dp))
                        Text("추가", fontSize = 14.sp)
                    }
                }
            }

            items(form.photos) { uri ->
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFF1F5))
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // 삭제 버튼
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(20.dp)
                            .background(Color(0x66000000), CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { form.photos.remove(uri) }
                    )
                }
            }
        }
    }
}

/* ---------- Step 7: 검토 ---------- */
@Composable
private fun ReviewStep(form: SellForm) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 상단 대표 이미지
        if (form.photos.isNotEmpty()) {
            AsyncImage(
                model = form.photos.first(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
        } else {
            Image(
                painter = painterResource(id = R.drawable.sample_car_2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(
            form.modelName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${form.yearDesc} · ${form.mileageKm.ifBlank { "—" }} km",
            color = Color(0xFF6B7280)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = form.hopePrice.ifBlank { "—" },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A5BFF)
            )
        )

        Spacer(Modifier.height(16.dp))
        InfoCard(
            rows = listOf(
                "차량 번호" to (form.plate.ifBlank { "—" }),
                "소유자명" to (form.owner.ifBlank { "—" }),
                "주행거리" to (form.mileageKm.ifBlank { "—" }),
                "희망가격" to (form.hopePrice.ifBlank { "—" }),
                "추가정보" to (form.extra.ifBlank { "—" })
            )
        )
    }
}

/* ---------- Step 8: 완료 ---------- */
@Composable
private fun DoneStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = Color(0xFF22C55E)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "차량 등록이 완료되었습니다!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
    }
}

/* ---------- 재사용 컴포넌트 ---------- */

@Composable
private fun StepScaffold(
    headline: String,
    input: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(36.dp))
        Text(
            headline,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(23.dp))
        input()
    }
}

@Composable
private fun LabeledField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFF111827),
            focusedBorderColor = Color(0xFF2A5BFF)
        )
    )
}

@Composable
private fun InfoCard(rows: List<Pair<String, String>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(Modifier.padding(16.dp)) {
            rows.forEachIndexed { idx, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color(0xFF6B7280))
                    Text(value, fontWeight = FontWeight.SemiBold)
                }
                if (idx != rows.lastIndex) Divider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun CleanProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFE5E7EB),
    activeColor: Color = Color(0xFF2A5BFF),
) {
    val p = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(p)
                .background(activeColor)
        )
    }
}