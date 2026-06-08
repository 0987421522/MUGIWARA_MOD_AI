package com.mugiwara.mod.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mugiwara.mod.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        TopAppBar(
            title = { Text("عن المطور", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "رجوع", tint = WhiteText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BlackSurface,
                titleContentColor = WhiteText
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Red700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = WhiteText,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "🏴‍☠️ MUGIWARA",
                color = Red500,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "MONKEY D. LUFFY MODE",
                color = Green500,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About card
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "👨‍💻 عن المطور",
                        color = Red500,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "MUGIWARA هو خبير في الأمن السيبراني واختبار الاختراق " +
                        "الأخلاقي، مع خبرة واسعة في:\n\n" +
                        "• اختبار الاختراق (Penetration Testing)\n" +
                        "• تقييم الثغرات الأمنية (Vulnerability Assessment)\n" +
                        "• تطوير التطبيقات الآمنة\n" +
                        "• تحليل البرامج الضارة (Malware Analysis)\n" +
                        "• أمن الشبكات والشبكات اللاسلكية\n" +
                        "• برمجة Kotlin، Python، والعديد من اللغات\n" +
                        "• التداول والأسواق المالية\n\n" +
                        "تم تطوير هذا التطبيق لغرض تعليم الأمن السيبراني " +
                        "وتقديم محتوى مفيد للمهتمين بالمجال.",
                        color = GrayText,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skills card
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "🛡️ التخصصات",
                        color = Green500,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SkillRow("الأمن السيبراني", Red500)
                    SkillRow("اختبار الاختراق", Red500)
                    SkillRow("البرمجة والتطوير", Green500)
                    SkillRow("التداول المالي", Green500)
                    SkillRow("تحليل الثغرات", Red500)
                    SkillRow("تطوير Android", Green500)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "جميع الحقوق محفوظة © 2024 MUGIWARA MOD\n🏴‍☠️ Monkey D. Luffy Mode",
                color = GrayText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SkillRow(skill: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            skill,
            color = WhiteText,
            fontSize = 15.sp
        )
    }
}
