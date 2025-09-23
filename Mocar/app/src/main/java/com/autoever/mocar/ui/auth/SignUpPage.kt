package com.autoever.mocar.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.autoever.mocar.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.autoever.mocar.model.UserData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.DatePickerDialog
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import com.autoever.mocar.ui.common.component.molecules.ToastMessage
import com.google.firebase.auth.FirebaseAuthException
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCheck by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val passwordCheckVisible = remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordCheckError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var birthdayError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.KOREA) }
    // 달력 클릭 시 DatePickerDialog 표시
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                birthday = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val isFormValid by remember(email, password, passwordCheck, name, birthday,
        emailError, passwordError, passwordCheckError, nameError, birthdayError) {
        derivedStateOf {
            emailError.isEmpty()
                    && passwordError.isEmpty()
                    && passwordCheckError.isEmpty()
                    && nameError.isEmpty()
                    && birthdayError.isEmpty()
                    && email.isNotBlank()
                    && password.isNotBlank()
                    && passwordCheck.isNotBlank()
                    && name.isNotBlank()
                    && birthday.replace("-", "").length == 8
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        modifier = Modifier.fillMaxWidth()
            .padding(top = 20.dp),
        topBar = {
            Column(Modifier.padding(16.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_mocar),
                    contentDescription = "로고",
                    modifier = Modifier
                        .width(140.dp)
                        .height(60.dp)
                )
                Text(
                    text = "회원가입",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Start
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFFF8F8F8))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "이메일",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            // 이메일 입력
            OutlinedTextField(
                value = email,
                onValueChange = { email = it
                    emailError = if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) "" else "유효한 이메일 형식이 아닙니다."
                                },
                placeholder = { Text("abc@example.com") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                ),
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(onClick = { email = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "지우기",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
            )
            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "비밀번호",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            // 비밀번호 입력
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.length >= 8) "" else "비밀번호는 최소 8자 이상이어야 합니다."
                                },
                placeholder = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if(passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible.value = !passwordVisible.value
                        }
                    ) {
                        Icon(
                            imageVector =
                                if(passwordVisible.value) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                            contentDescription = "비밀번호 보이기"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            if (passwordError.isNotEmpty()) {
                Text(
                    text = passwordError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "비밀번호 확인",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            OutlinedTextField(
                value = passwordCheck,
                onValueChange = {
                    passwordCheck = it
                    passwordCheckError = if (password == it) "" else "비밀번호가 일치하지 않습니다."
                                },
                placeholder = { Text("비밀번호 확인") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if(passwordCheckVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordCheckVisible.value = !passwordCheckVisible.value
                        }
                    ) {
                        Icon(
                            imageVector =
                                if(passwordCheckVisible.value) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                            contentDescription = "비밀번호 보이기"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            if (passwordCheckError.isNotEmpty()) {
                Text(
                    text = passwordCheckError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "생년월일",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            OutlinedTextField(
                value = birthday,
                onValueChange = {
                    val digits = it.filter { char -> char.isDigit() } // 숫자만 추출

                    if (digits.length <= 8) {
                        birthday = digits

                        // 8자리 입력된 경우에만 하이픈 포맷 적용
                        if (digits.length == 8) {
                            birthday = "${digits.substring(0, 4)}-${
                                digits.substring(
                                    4,
                                    6
                                )
                            }-${digits.substring(6)}"
                        }

                        birthdayError = if (digits.length == 8) "" else "생년월일은 8자리여야 합니다."
                    }
                },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "달력 열기"
                        )
                    }
                },
                trailingIcon = {
                    if (birthday.isNotEmpty()) {
                        IconButton(onClick = { birthday = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "지우기",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (birthdayError.isNotEmpty()) {
                Text(
                    text = birthdayError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "이름",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Start
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it
                    nameError = if (name.isBlank()) "이름을 입력해주세요." else ""
                                },
                placeholder = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                ),
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "지우기",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
            )
            if (nameError.isNotEmpty()) {
                Text(
                    text = nameError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 4.dp)
                )
            }
            if (toastMessage != null) {
                ToastMessage(
                    message = toastMessage!!,
                    onDismiss = { toastMessage = null },
                    modifier = Modifier.padding(top = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(80.dp))
            }

            Button(
                onClick = {
                    // 1. 이메일 형식 검증
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@Button

                    // 2. 비밀번호 일치 확인
                    if (password != passwordCheck) return@Button

                    // 3. 비밀번호 최소 길이 확인
                    if (password.length < 8) return@Button

                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = task.result?.user?.uid ?: return@addOnCompleteListener
                                val userData = UserData(
                                    name = name,
                                    email = email,
                                    dob = birthday,
                                    phone = "",
                                    photoUrl = "",
                                    rating = 0,
                                    ratingCount = 0,
                                    createdAt = Timestamp.now(),
                                    updatedAt = Timestamp.now()
                                )

                                db.collection("users").document(uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        // 회원가입 성공 후 로그인 페이지로 이동
                                        navController.navigate("auth")
                                    }
                                    .addOnFailureListener {
                                        // Firestore 저장 실패 처리
                                        toastMessage = "회원정보 저장에 실패했습니다."
                                    }
                            } else {
                                // Firebase Auth 실패 처리
                                val exception = task.exception
                                if (exception is FirebaseAuthException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                                    toastMessage = "이미 가입된 이메일입니다."
                                } else {
                                    toastMessage = "회원가입에 실패했습니다."
                                }
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,   // 버튼 배경색
                    contentColor = Color.White     // 텍스트 색상
                ),
                enabled = isFormValid,
            ) {
                Text(
                    text = "회원가입",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .padding(top = 30.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "이미 계정이 있으신가요? ",
                    color = Color.Gray
                )

                Text(
                    text = "로그인",
                    modifier = Modifier
                        .clickable {
                            navController.navigate("auth")
                        },
                    color = Color(0xFF3058EF)
                )
            }
        }
    }
}

