package com.mugiwara.mod.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mugiwara.mod.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController

// داخل Box قبل المحتوى:
AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context ->
        VideoView(context).apply {
            setVideoURI(Uri.parse("android.resource://${context.packageName}/raw/background_video"))
            setMediaController(null)
            setOnPreparedListener { mp ->
                mp.isLooping = true
                start()
            }
        }
    }
)
// داخل Column:
Image(
    painter = painterResource(id = R.drawable.logo),
    contentDescription = "MUGIWARA MOD Logo",
    modifier = Modifier
        .size(200.dp)
        .clip(CircleShape)
)
@Composable
fun WelcomeScreen(onNavigateToChat: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(500)
        startAnimation = true
        delay(2000)
        showButton = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn() + scaleIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // App Icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Red700),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "MUGIWARA MOD",
                            tint = WhiteText,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "🏴‍☠️ MUGIWARA MOD",
                        color = Red500,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "خبير الأمن السيبراني والبرمجة",
                        color = Green500,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Cyber Security Expert | Programmer | Ethical Hacker | Trader",
                        color = GrayText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Developer info box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlackCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👨‍💻 Developed By: MUGIWARA",
                                color = Red500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "MONKEY D. LUFFY MODE 🏴‍☠️",
                                color = Green500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "متخصص في أمن المعلومات، اختبار الاختراق، " +
                                        "وتطوير التطبيقات الآمنة. هذا التطبيق يهدف " +
                                        "لتعليم الأمن السيبراني والبرمجة بطريقة عملية وآمنة.",
                                color = GrayText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    AnimatedVisibility(
                        visible = showButton,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Button(
                            onClick = onNavigateToChat,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red700,
                                contentColor = WhiteText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "ابدأ المحادثة 🚀",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

