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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mugiwara.mod.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {

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

            // ===== حالة السيرفر =====
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, null, tint = Green500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "حالة السيرفر",
                            color = WhiteText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Green500, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("متصل بسيرفر MUGIWARA ✅", color = Green500, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "المفتاح محفوظ بأمان على السيرفر 🔒",
                        color = GrayText,
                        fontSize = 13.sp
                    )
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
                    Text("النموذج: Claude Sonnet", color = Green500, fontSize = 14.sp)
                    Text("المزود: Anthropic ✅", color = GrayText, fontSize = 13.sp)
                    Text("السيرفر: Railway Cloud", color = GrayText, fontSize = 13.sp)
                }
            }

            // ===== معلومات التطبيق =====
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Green500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عن التطبيق", color = WhiteText, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("👨‍💻 Developed By: MUGIWARA", color = Red500, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🏴‍☠️ MONKEY D. LUFFY MODE", color = Green500, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("الإصدار: 2.0", color = GrayText, fontSize = 13.sp)
                }
            }
        }
    }
}
