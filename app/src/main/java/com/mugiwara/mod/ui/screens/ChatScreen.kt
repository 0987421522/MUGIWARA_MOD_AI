package com.mugiwara.mod.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mugiwara.mod.network.ChatRequest
import com.mugiwara.mod.network.RetrofitClient
import com.mugiwara.mod.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Int,
    val text: String,
    val isUser: Boolean,
    val imageUri: Uri? = null,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageId by remember { mutableStateOf(1) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImage = uri }

    val sendMessage = {
        if (inputText.isNotBlank() && !isLoading) {
            val userMsg = ChatMessage(
                id = messageId++,
                text = inputText,
                isUser = true,
                imageUri = selectedImage
            )
            messages = messages + userMsg
            val currentInput = inputText
            inputText = ""
            selectedImage = null
            isLoading = true

            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
                try {
                    val response = RetrofitClient.instance.sendMessage(
                        ChatRequest(message = currentInput, language = "ar")
                    )
                    val reply = if (response.isSuccessful && response.body() != null) {
                        response.body()!!.reply
                    } else {
                        "⚠️ تعذر الاتصال بالخادم. تأكد من تشغيل backend."
                    }
                    messages = messages + ChatMessage(
                        id = messageId++,
                        text = reply,
                        isUser = false
                    )
                } catch (e: Exception) {
                    messages = messages + ChatMessage(
                        id = messageId++,
                        text = "⚠️ خطأ في الاتصال: ${e.localizedMessage}",
                        isUser = false
                    )
                } finally {
                    isLoading = false
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "🏴‍☠️ MUGIWARA MOD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "خبير الأمن السيبراني",
                        fontSize = 12.sp,
                        color = Green500
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BlackSurface,
                titleContentColor = WhiteText
            ),
            actions = {
                IconButton(onClick = onNavigateToAbout) {
                    Icon(Icons.Default.Info, "عن المطور", tint = Red500)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "الإعدادات", tint = Green500)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Red500,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "👨‍💻 MUGIWARA MOD",
                                color = Red500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "خبير الأمن السيبراني",
                                color = Green500,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ethical Hacker | Programmer | Trader",
                                color = GrayText,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BlackCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("💬 اسألني عن:", color = WhiteText, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("🔒 الأمن السيبراني", color = Red500, fontSize = 14.sp)
                                    Text("💻 البرمجة", color = Green500, fontSize = 14.sp)
                                    Text("📈 التداول", color = Red500, fontSize = 14.sp)
                                    Text("🛡️ اختبار الاختراق", color = Green500, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            items(messages) { message ->
                ChatBubble(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Green500,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MUGIWARA يفكر...", color = GrayText, fontSize = 14.sp)
                    }
                }
            }
        }

        if (selectedImage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .background(BlackCard, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تم تحديد الصورة ✅", color = Green500, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedImage = null }) {
                        Icon(Icons.Default.Close, "إزالة", tint = Red500)
                    }
                }
            }
        }

        Surface(
            color = BlackSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { imagePicker.launch("image/*") }) {
                    Icon(Icons.Default.Image, "صورة", tint = Green500)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("اكتب سؤالك هنا...", color = GrayText) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        cursorColor = Red500,
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = BlackCard
                    ),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading
                )

                IconButton(
                    onClick = sendMessage,
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Send,
                        "إرسال",
                        tint = if (inputText.isNotBlank() && !isLoading) Red500 else GrayText
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) Red700 else BlackCard
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (message.imageUri != null) {
            AsyncImage(
                model = message.imageUri,
                contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Surface(
            color = bgColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = WhiteText,
                fontSize = 15.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
