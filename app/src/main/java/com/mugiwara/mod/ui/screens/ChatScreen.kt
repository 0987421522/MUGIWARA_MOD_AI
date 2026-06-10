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
import androidx.compose.foundation.text.selection.SelectionContainer
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

    val conversationHistory = remember { mutableListOf<Map<String, String>>() }

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

            conversationHistory.add(mapOf("role" to "user", "content" to currentInput))

            scope.launch {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
                try {
                    val response = RetrofitClient.instance.sendMessage(
                        ChatRequest(
                            message = currentInput,
                            language = "ar",
                            conversation_history = conversationHistory.takeLast(10).toList()
                        )
                    )

                    val reply = if (response.isSuccessful && response.body() != null) {
                        val replyText = response.body()!!.reply
                        conversationHistory.add(mapOf("role" to "assistant", "content" to replyText))
                        replyText
                    } else {
                        when (response.code()) {
                            401 -> "⚠️ خطأ في مفتاح التطبيق."
                            429 -> "⚠️ تم تجاوز حد الطلبات. انتظر دقيقة."
                            500 -> "⚠️ خطأ في السيرفر. أعد المحاولة."
                            503 -> "⚠️ السيرفر غير متاح حالياً."
                            else -> "⚠️ خطأ ${response.code()}: ${response.message()}"
                        }
                    }

                    messages = messages + ChatMessage(
                        id = messageId++,
                        text = reply,
                        isUser = false
                    )

                } catch (e: Exception) {
                    val errorMsg = when {
                        e.message?.contains("Unable to resolve host") == true ->
                            "⚠️ لا يوجد اتصال بالإنترنت."
                        e.message?.contains("timeout") == true ->
                            "⚠️ انتهت مهلة الاتصال. أعد المحاولة."
                        else -> "⚠️ خطأ: ${e.localizedMessage}"
                    }
                    messages = messages + ChatMessage(
                        id = messageId++,
                        text = errorMsg,
                        isUser = false
                    )
                } finally {
                    isLoading = false
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
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
                        text = "🏴‍☠️ MUGIWARA MOD AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isLoading) "يفكر..." else "جاهز للمساعدة ✅",
                        fontSize = 11.sp,
                        color = if (isLoading) Red500 else Green500
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BlackSurface,
                titleContentColor = WhiteText
            ),
            actions = {
                IconButton(onClick = {
                    messages = listOf()
                    conversationHistory.clear()
                }) {
                    Icon(Icons.Default.Delete, "مسح", tint = GrayText)
                }
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
                item { WelcomeChatContent() }
            }

            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(BlackCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏴‍☠️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BlackCard),
                            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Green500,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MUGIWARA يفكر...", color = GrayText, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        if (selectedImage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
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
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
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
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
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
                    enabled = !isLoading,
                    maxLines = 5
                )
                IconButton(
                    onClick = sendMessage,
                    enabled = inputText.isNotBlank() && !isLoading
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
fun WelcomeChatContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏴‍☠️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("MUGIWARA MOD AI", color = Red500, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("مساعدك الذكي — Claude Sonnet", color = Green500, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            "💻 كود Python/Kotlin/Java/C++" to Red500,
            "🐧 أوامر Linux وTermux" to Green500,
            "🔒 أمن سيبراني وشبكات" to Red500,
            "📱 تطوير تطبيقات Android" to Green500,
            "🌐 تطوير ويب Frontend/Backend" to Red500,
            "🗄️ قواعد البيانات SQL/NoSQL" to Green500,
            "🐛 تصحيح الأكواد وشرحها" to Red500,
            "🤖 الذكاء الاصطناعي وML" to Green500
        ).forEach { (text, color) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = text,
                    color = color,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) Red700 else BlackCard
    val shape = if (message.isUser)
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        if (!message.isUser) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏴‍☠️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("MUGIWARA MOD", color = Red500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (message.imageUri != null) {
            AsyncImage(
                model = message.imageUri,
                contentDescription = null,
                modifier = Modifier.width(200.dp).height(200.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Surface(color = bgColor, shape = shape, modifier = Modifier.widthIn(max = 300.dp)) {
            SelectionContainer {
                Text(
                    text = message.text,
                    color = WhiteText,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 22.sp
                )
            }
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.height(2.dp))
            Text("أنت", color = GrayText, fontSize = 11.sp)
        }
    }
}
