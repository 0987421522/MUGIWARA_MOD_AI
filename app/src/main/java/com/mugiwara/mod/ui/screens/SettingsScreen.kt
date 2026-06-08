package com.mugiwara.mod.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var selectedLanguage by remember { mutableStateOf("العربية") }
    var isDarkMode by remember { mutableStateOf(true) }
    var apiServer by remember { mutableStateOf("http://localhost:8000") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        TopAppBar(
            title = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            // Language Section
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🌐 اللغة / Language",
                        color = Red500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LanguageOption("العربية", selectedLanguage == "العربية") {
                        selectedLanguage = "العربية"
                    }
                    LanguageOption("English", selectedLanguage == "English") {
                        selectedLanguage = "English"
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Server Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔧 إعدادات الخادم",
                        color = Green500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = apiServer,
                        onValueChange = { apiServer = it },
                        label = { Text("Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = Red500,
                            unfocusedBorderColor = BlackCard
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // About App
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ℹ️ معلومات التطبيق",
                        color = Red500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoRow("الإصدار", "1.0.0")
                    InfoRow("المطور", "MUGIWARA 🏴‍☠️")
                    InfoRow("الوضع", "Monkey D. Luffy Mode")
                }
            }
        }
    }
}

@Composable
fun LanguageOption(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Red500,
                unselectedColor = GrayText
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            name,
            color = if (isSelected) WhiteText else GrayText,
            fontSize = 15.sp
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, color = GrayText, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = WhiteText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
