package com.autoever.mocar.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.autoever.mocar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordPage (navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

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
                "비밀번호 재설정",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(70.dp))

            Column(
                modifier = Modifier.fillMaxWidth()) {
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

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp), // ← 모서리 둥글게
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3058EF),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                Toast.makeText(
                                    context,
                                    "비밀번호 재설정 이메일을 발송했습니다.\n메일을 확인해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // 원한다면 로그인 화면으로 이동
                                navController.popBackStack()
                            }
                    } else {
                        Toast.makeText(context, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("인증 요청",
                    fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "로그인 화면으로 돌아가기",
                modifier = Modifier
                    .clickable {
                        navController.navigate("login")
                    },
                color = Color(0xFF3058EF)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}