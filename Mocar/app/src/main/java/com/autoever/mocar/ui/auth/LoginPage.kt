package com.autoever.mocar.ui.auth

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.autoever.mocar.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.autoever.mocar.ui.common.component.molecules.ToastMessage
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    var keepLoggedIn by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var toastMessage by remember { mutableStateOf<String?>(null) }

    // 로그인 확인
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedKeepLoggedIn = sharedPref.getBoolean("keepLoggedIn", false)

        if (user != null && savedKeepLoggedIn) {
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true } // 뒤로가기 막기
            }
        }
    }


    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        modifier = Modifier.fillMaxWidth()
            .padding(top = 20.dp),
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_mocar),
                        contentDescription = "모카 로고",
                        modifier = Modifier
                            .width(140.dp)
                            .height(60.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F8F8)
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF8F8F8))
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "로그인",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(30.dp))

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
                onValueChange = {
                    email = it
                    emailError = if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) "" else "유효한 이메일 형식이 아닙니다."
                                },
                placeholder = { Text("abc@example.com") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3058EF),
                    unfocusedBorderColor = Color.Gray
                )
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
                placeholder = { Text("8자 이상의 비밀번호") },
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 0.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = keepLoggedIn,
                        onCheckedChange = { keepLoggedIn = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF3058EF)
                        ),
                        modifier = Modifier.offset(x = (-6).dp),
                    )
                    Text(text = "로그인 상태 유지",
                        modifier = Modifier.offset(x = (-12).dp),)
                }

                Text(
                    text = "비밀번호 재설정",
                    modifier = Modifier
                        .clickable {
                            navController.navigate("resetPassword")
                        }
                        .padding(end = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        toastMessage = "이메일과 비밀번호를 모두 입력해주세요."
                        return@Button
                    }

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                sharedPref.edit().putBoolean("keepLoggedIn", keepLoggedIn).apply()

                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                toastMessage = "이메일 또는 비밀번호를 확인하세요."
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
                Text(text = "로그인",
                    fontSize = 16.sp
                )
            }

            if (toastMessage != null) {
                ToastMessage(
                    message = toastMessage!!,
                    onDismiss = { toastMessage = null },
                    modifier = Modifier.padding(top = 40.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .padding(bottom = 35.dp)
            ) {
                Text(
                    text = "계정이 없으신가요? ",
                    color = Color.Gray
                    )

                Text(
                    text = "회원가입",
                    modifier = Modifier
                        .clickable {
                            navController.navigate("signup")
                        },
                    color = Color(0xFF3058EF)
                )
            }

        }
    }
}
