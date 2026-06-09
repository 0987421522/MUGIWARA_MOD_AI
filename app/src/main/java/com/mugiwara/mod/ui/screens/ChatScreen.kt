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
import com.mugiwara.mod.network.*
import com.mugiwara.mod.ui.theme.*
import kotlinx.coroutines.launch

// ===== SYSTEM PROMPT للمساعد =====
private const val SYSTEM_PROMPT = """أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متكامل واحترافي.

قدراتك الكاملة:
🔹 البرمجة بجميع اللغات: Kotlin, Java, Python, JavaScript, C++, C#, PHP, Swift, Rust, Go, وغيرها
🔹 تطوير تطبيقات Android وiOS
🔹 تطوير الويب (Frontend وBackend)
🔹 Linux وTermux وأوامر Shell
🔹 الشبكات والبروتوكولات
🔹 قواعد البيانات (SQL, NoSQL)
🔹 الأمن السيبراني الدفاعي والأخلاقي (تعليمي فقط)
🔹 الذكاء الاصطناعي وتعلم الآلة
🔹 تصحيح الأكواد وشرحها
🔹 بناء المشاريع من الصفر

قواعد الرد:
- رد دائماً بلغة المستخدم (عربي أو إنجليزي)
- قدّم الأكواد كاملة وجاهزة للتنفيذ
- اشرح كل خطوة بوضوح
- كن دقيقاً ومفيداً دائماً"""

// ===== Data Classes =====
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

    // تاريخ المحادثة للـ context
    val conversationHistory = remember { mutableListOf<GroqMessage>() }

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

            // أضف رسالة المستخدم للتاريخ
            conversationHistory.add(GroqMessage(role = "user", content = currentInput))

            scope.launch {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
                try {
                    // بناء قائمة الرسائل مع System Prompt
                    val allMessages = mutableListOf(
                        GroqMessage(role = "system", content = SYSTEM_PROMPT)
                    ).apply {
                        // أضف آخر 10 رسائل فقط لتجنب تجاوز الـ context
                        addAll(conversationHistory.takeLast(10))
                    }

                    val response = GroqDirectClient.instance.chat(
                        GroqRequest(messages = allMessages)
                    )

                    val reply = if (response.isSuccessful && response.body() != null) {
                        val replyText = response.body()!!.choices.firstOrNull()?.message?.content
                            ?: "⚠️ لم يتم الحصول على رد."
                        // أضف رد المساعد للتاريخ
                        conversationHistory.add(GroqMessage(role = "assistant", content = replyText))
                        replyText
                    } else {
                        val errorCode = response.code()
                        when (errorCode) {
                            401 -> "⚠️ مفتاح API غير صحيح.\nاذهب للإعدادات وأدخل مفتاح Groq الصحيح.\nاحصل عليه مجاناً من: console.groq.com"
                            429 -> "⚠️ تم تجاوز حد الطلبات. انتظر دقيقة وأعد المحاولة."
                            500 -> "⚠️ خطأ في الخادم. أعد المحاولة."
                            else -> "⚠️ خطأ $errorCode: ${response.message()}"
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
                            "⚠️ لا يوجد اتصال بالإنترنت.\nتأكد من الاتصال بالإنترنت وأعد المحاولة."
                        e.message?.contains("timeout") == true ->
                            "⚠️ انتهت مهلة الاتصال.\nأعد المحاولة."
                        else ->
                            "⚠️ خطأ: ${e.localizedMessage}"
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
        // ===== TopBar =====
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
                // زر مسح المحادثة
                IconButton(onClick = {
                    messages = listOf()
                    conversationHistory.clear()
                }) {
                    Icon(Icons.Default.Delete, "مسح المحادثة", tint = GrayText)
                }
                IconButton(onClick = onNavigateToAbout) {
                    Icon(Icons.Default.Info, "عن المطور", tint = Red500)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "الإعدادات", tint = Green500)
                }
            }
        )

        // ===== قائمة الرسائل =====
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState
        ) {
            // شاشة الترحيب عند عدم وجود رسائل
            if (messages.isEmpty()) {
                item {
                    WelcomeChatContent()
                }
            }

            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // مؤشر التحميل
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
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

        // ===== معاينة الصورة المحددة =====
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
                        modifier = Modifier
                            .size(60.dp)
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

        // ===== حقل الإدخال =====
        Surface(
            color = BlackSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
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

// ===== محتوى الترحيب داخل ChatScreen =====
@Composable
fun WelcomeChatContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏴‍☠️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "MUGIWARA MOD AI",
            color = Red500,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text("مساعدك الذكي المتكامل", color = Green500, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))

        // بطاقات القدرات
        val capabilities = listOf(
            "💻 اكتب لي كود Python/Kotlin/Java" to Red500,
            "🐧 أوامر Linux وTermux" to Green500,
            "🔒 أمن سيبراني وشبكات" to Red500,
            "📱 تطوير تطبيقات Android" to Green500,
            "🌐 تطوير ويب Frontend/Backend" to Red500,
            "🗄️ قواعد البيانات SQL/NoSQL" to Green500,
            "🐛 تصحيح الأكواد وشرحها" to Red500,
            "🤖 الذكاء الاصطناعي وML" to Green500
        )

        capabilities.forEach { (text, color) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp),
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

// ===== فقاعة الرسائل =====
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
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            // SelectionContainer يسمح للمستخدم بنسخ النص
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
