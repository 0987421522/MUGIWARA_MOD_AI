package com.mugiwara.mod.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mugiwara.mod.network.GroqDirectClient
import com.mugiwara.mod.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {

    var apiKey by remember { mutableStateOf(GroqDirectClient.GROQ_API_KEY) }
    var showKey by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    "⚙️ الإعدادات",
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "رجوع", tint = Red500)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ===== بطاقة مفتاح Groq API =====
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, null, tint = Red500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "مفتاح Groq API",
                            color = WhiteText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "احصل على مفتاحك المجاني من:\nconsole.groq.com",
                        color = Green500,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            saveSuccess = false
                        },
                        label = { Text("أدخل مفتاح API", color = GrayText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            cursorColor = Red500,
                            focusedBorderColor = Red500,
                            unfocusedBorderColor = BlackCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showKey)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    tint = GrayText
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            GroqDirectClient.GROQ_API_KEY = apiKey.trim()
                            saveSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Red700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, null, tint = WhiteText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ المفتاح", color = WhiteText, fontWeight = FontWeight.Bold)
                    }

                    if (saveSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Green500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تم حفظ المفتاح بنجاح ✅", color = Green500, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ===== بطاقة تعليمات الاستخدام =====
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Green500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "كيفية الحصول على مفتاح Groq",
                            color = WhiteText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val steps = listOf(
                        "1️⃣" to "افتح console.groq.com",
                        "2️⃣" to "سجّل حساباً مجانياً",
                        "3️⃣" to "اضغط API Keys",
                        "4️⃣" to "اضغط Create API Key",
                        "5️⃣" to "انسخ المفتاح والصقه هنا",
                        "6️⃣" to "اضغط حفظ واستمتع! 🚀"
                    )

                    steps.forEach { (emoji, step) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(step, color = GrayText, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ===== معلومات النموذج =====
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, null, tint = Red500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نموذج الذكاء الاصطناعي", color = WhiteText, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("النموذج: Llama 3 70B", color = Green500, fontSize = 14.sp)
                    Text("المزود: Groq (سريع جداً ✅)", color = GrayText, fontSize = 13.sp)
                    Text("السرعة: أسرع نموذج متاح مجاناً", color = GrayText, fontSize = 13.sp)
                }
            }
        }
    }
}
