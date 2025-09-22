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
import androidx.compose.ui.platform.LocalContext
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
                onValueChange = { email = it },
                placeholder = { Text("abc@example.com") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                )
            )

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
                onValueChange = { password = it },
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
                onValueChange = { passwordCheck = it },
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
                    if (it.matches(Regex("^\\d{0,4}-?\\d{0,2}-?\\d{0,2}$"))) {
                        birthday = it
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

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
                onValueChange = { name = it },
                placeholder = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(60.dp))

            // 버튼 (동작 없음)
            Button(
                onClick = {
                    // 1. 이메일 형식 검증
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "유효한 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 2. 비밀번호 일치 확인
                    if (password != passwordCheck) {
                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 3. 비밀번호 최소 길이 확인
                    if (password.length < 8) {
                        Toast.makeText(context, "비밀번호는 최소 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

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
                                        Toast.makeText(context, "회원정보 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Firebase Auth 실패 처리
                                Toast.makeText(context, "회원가입에 실패했습니다: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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

