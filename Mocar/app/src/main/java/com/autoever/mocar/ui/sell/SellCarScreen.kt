package com.autoever.mocar.ui.sell

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.autoever.mocar.R
import com.autoever.mocar.ui.common.util.sanitize
import com.autoever.mocar.viewmodel.ListingStatus
import com.autoever.mocar.viewmodel.SellCarViewModel
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
    var mainImageUrl by mutableStateOf("")
    val photos = mutableStateListOf<Uri>()
}

/* ---------- 메인 스크린 ---------- */
@Composable
fun SellCarScreen() {
    val vm: SellCarViewModel = viewModel()
    val lookup by vm.lookup.collectAsState()
    val submit by vm.submit.collectAsState()

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    val focus = LocalFocusManager.current
    var step by remember { mutableStateOf(SellStep.Plate) }
    val form = remember { SellForm() }

    // 등록 완료되면 Done 으로 이동
    LaunchedEffect(submit.done) {
        if (submit.done) step = SellStep.Done
    }

    // 에러가 생기면 스낵바
    LaunchedEffect(lookup.error) { lookup.error?.let { snackbarHostState.showSnackbar(it) } }
    LaunchedEffect(submit.error) { submit.error?.let { snackbarHostState.showSnackbar(it) } }

    // 판매 차단 여부 계산
    val saleBlocked: Boolean = remember(lookup.raw?.status) {
        val s = lookup.raw?.status
        s == ListingStatus.ON_SALE || s == ListingStatus.RESERVED
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom),
        topBar = {
            val currentIndex = SellStep.entries.indexOf(step)
            val progressValue = if (step == SellStep.Done) 1f
            else (currentIndex.toFloat() / totalSteps).coerceIn(0f, 1f)
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
                progress = progressValue
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
                    when (step) {
                        SellStep.Owner -> {               // 소유자명 입력 후 조회
                            vm.findListing(form.plate, form.owner)
                            step = SellStep.CarInfo
                        }
                        SellStep.Review -> {
                            if (!submit.loading) {
                                vm.submitSale(
                                    mileageKm = form.mileageKm.filter { it.isDigit() }.toLongOrNull(),
                                    hopePrice = form.hopePrice.filter { it.isDigit() }.toLongOrNull(),
                                    description = form.extra,
                                    images = form.photos.map { it.toString() }
                                )
                            }
                        }
                        SellStep.Done -> step = SellStep.Plate
                        else -> step = SellStep.entries[SellStep.entries.indexOf(step) + 1]
                    }
                },

// 버튼 활성화 조건 (기존과 동일, 필요시 Owner 단계에서 로딩 동안 false로도 확장 가능)
                nextEnabled = when (step) {
                    SellStep.Plate -> form.plate.isNotBlank()
                    SellStep.Owner -> form.owner.isNotBlank() && !lookup.loading
                    SellStep.CarInfo -> {
                        val blocked = lookup.raw?.status in setOf(ListingStatus.ON_SALE, ListingStatus.RESERVED)
                        !blocked
                    }
                    SellStep.Review -> !submit.loading
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
                    SellStep.CarInfo -> {
                        // 조회 결과를 form에 반영
                        LaunchedEffect(lookup.car?.id) {
                            lookup.car?.let { car ->
                                form.modelName = car.title.ifBlank {
                                    listOf(car.brandName, car.title).filter { it.isNotBlank() }.joinToString(" ")
                                }
                                form.yearDesc = car.yearDesc
                                form.mainImageUrl = car.imageUrl.orEmpty()   //대표 이미지 1장만
                            }
                        }

                        // 판매/예약이면 배너를 먼저 노출
                        val saleBlocked = remember(lookup.raw?.status) {
                            lookup.raw?.status in setOf(ListingStatus.ON_SALE, ListingStatus.RESERVED)
                        }

                        Column(Modifier.fillMaxSize()) {
                            if (saleBlocked) {
                                SaleBlockedBanner(
                                    status = lookup.raw?.status
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            // 원래의 내용
                            CarInfoStep(form)

                            // 조회 상태 메시지(선택)
                            when {
                                lookup.loading -> Text("검색 중…", modifier = Modifier.padding(20.dp))
                                lookup.error != null && lookup.car == null ->
                                    Text("오류: ${lookup.error}", color = Color.Red, modifier = Modifier.padding(20.dp))
                            }
                        }
                    }
                    SellStep.Odometer -> OdometerStep(form)
                    SellStep.Price    -> PriceStep(form)
                    SellStep.Extra    -> ExtraStep(form)
                    SellStep.Photos   -> PhotosStep(form)
                    SellStep.Review   -> ReviewStep(form, saleBlocked)
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
    Surface(shadowElevation = 0.dp,
        tonalElevation  = 0.dp,
        color = Color.White) {
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
        Text("차량 정보",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(16.dp))

        val hasImage = form.mainImageUrl.isNotBlank()
        if (hasImage) {
            AsyncImage(
                model = form.mainImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF4F4F6)),
                contentScale = ContentScale.Crop
            )
        } else {
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
        }
        Spacer(Modifier.height(12.dp))
        Text(form.modelName, color = Color(0xFF6B7280))

        Spacer(Modifier.height(16.dp))
        InfoCard(
            rows = listOf(
                "차량 번호" to (form.plate.ifBlank { "—" }),
                "모델명"   to form.modelName,
                "연식"     to form.yearDesc
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
private fun ReviewStep(form: SellForm, saleBlocked: Boolean) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(20.dp)
    ) {
        // 기본정보 이미지(대표 1장) 표시
        val hasMain = form.mainImageUrl.isNotBlank()
        if (hasMain) {
            AsyncImage(
                model = form.mainImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.sample_car_2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            form.modelName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${form.yearDesc} · ${form.mileageKm.ifBlank { "—" }} km",
            color = Color(0xFF6B7280),
            style = MaterialTheme.typography.bodyMedium
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

        // ✅ 사용자가 추가로 올린 사진들 (섬네일 가로 스크롤)
        if (form.photos.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(form.photos) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 120.dp, height = 90.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 상태 차단 문구(옵션)
        if (saleBlocked) {
            Spacer(Modifier.height(12.dp))
            Text(
                "현재 상태 때문에 등록할 수 없습니다.",
                color = Color(0xFFD92D20),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 하단 버튼이 가려지지 않도록 여백
        Spacer(Modifier.height(24.dp))
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
    val interaction = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                BorderStroke(
                    2.dp,
                    if (isFocused) Color(0xFF2A5BFF) else Color(0xFF111827)
                ),
                RoundedCornerShape(14.dp)
            ),
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor  = Color.Transparent,
            errorBorderColor     = Color.Transparent,
            cursorColor          = Color(0xFF111827)
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
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        value,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                }
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

@Composable
private fun SaleBlockedBanner(status: String?) {
    val (title, message) = when (status) {
        ListingStatus.RESERVED -> "등록 불가" to "예약 중인 매물입니다."
        ListingStatus.ON_SALE  -> "등록 불가" to "이미 판매중인 매물입니다."
        else -> return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF7ED),                   // 연한 오렌지 배경
        border = BorderStroke(1.dp, Color(0xFFFBD6A8)) // 라이트 오렌지 보더
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 알림 아이콘 (X 제거)
            Icon(
                imageVector = Icons.Outlined.Info, // 없으면 Icons.Outlined.Info 사용
                contentDescription = null,
                tint = Color(0xFFB45309) // 진한 오렌지
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9A3412) // 텍스트 오렌지
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9A3412))
                )
            }
        }
    }
}

