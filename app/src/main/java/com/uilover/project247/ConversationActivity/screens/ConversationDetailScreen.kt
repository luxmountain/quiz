package com.uilover.project247.ConversationActivity.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.uilover.project247.ConversationActivity.viewmodels.ConversationDetailViewModel
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.utils.TextToSpeechManager
import kotlinx.coroutines.delay
import androidx.compose.ui.text.withStyle
import com.uilover.project247.ConversationActivity.components.AnswerFeedbackPopup
import com.uilover.project247.ConversationActivity.components.AvatarIcon
import com.uilover.project247.ConversationActivity.components.ChatInputBottomBar
import com.uilover.project247.ConversationActivity.components.DialogueBubble
import com.uilover.project247.ConversationActivity.components.QuizResultOverlay
import com.uilover.project247.ConversationActivity.components.QuizSection
import com.uilover.project247.ConversationActivity.viewmodels.ConversationStep
import com.uilover.project247.LearningActivity.components.ProgressBar
import com.uilover.project247.R

@Composable
fun createStyledDialogueText(text: String, target: String,showBlank: Boolean = false): AnnotatedString {
    // Tìm vị trí của từ mục tiêu (không phân biệt hoa thường)
    val startIndex = text.indexOf(target, ignoreCase = true)

    // Nếu không tìm thấy, trả về text bình thường
    if (startIndex == -1) {
        return buildAnnotatedString { append(text) }
    }

    val endIndex = startIndex + target.length

    // Nếu tìm thấy, xây dựng string
    return buildAnnotatedString {
        append(text.substring(0, startIndex))
        if (showBlank) {
            // 1. HIỂN THỊ CHỖ TRỐNG (THEO YÊU CẦU MỚI)
            val blank = "_______"
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )) {
                append(blank)
            }
        } else {
            // 2. HIỂN THỊ GẠCH CHÂN (LOGIC CŨ)
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )) {
                append(text.substring(startIndex, endIndex))
            }
        }

        append(text.substring(endIndex))
    }
}

// Màn hình chi tiết
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    viewModel: ConversationDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversation = uiState.conversation
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    var translationVisibleMap by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    val progressManager = remember { com.uilover.project247.data.repository.UserProgressManager(context) }

    // State cho ô chat (của Quiz 2)
    var userAnswer by rememberSaveable { mutableStateOf("") }

    // Reset userAnswer khi bắt đầu bước mới
    LaunchedEffect(uiState.currentStep) {
        userAnswer = ""
    }

    // Quản lý vòng đời của TTS
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                ttsManager.shutdown()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            ttsManager.shutdown()
        }
    }

    // Tự động đọc
    LaunchedEffect(uiState.currentSpokenText) {
        val textToSpeak = uiState.currentSpokenText
        if (!textToSpeak.isNullOrBlank()) {
            ttsManager.speakWithCallback(textToSpeak) {
                viewModel.onSpeechFinished()
            }
        }
    }

    // Tự động cuộn
    LaunchedEffect(uiState.visibleDialogueLines.size, uiState.currentStep) {
        delay(100)
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    if (uiState.currentStep == ConversationStep.FINISHED) {
        LaunchedEffect(Unit) {
            // Lưu kết quả hoàn thành conversation
            conversation?.let { conv ->
                val result = com.uilover.project247.data.repository.StudyResult(
                    topicId = conv.id,
                    topicName = conv.title,
                    studyType = "conversation",
                    totalItems = conv.dialogue.size,
                    correctCount = conv.dialogue.size, // Hoàn thành tất cả
                    timeSpent = 0, // TODO: Track thời gian nếu cần
                    accuracy = 100f,
                    completedDate = System.currentTimeMillis()
                )
                progressManager.saveStudyResult(result)
            }
            
            delay(1000) // Đợi 1s sau khi hoàn thành
            onNavigateBack()
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // ✅ Dùng ProgressBar component riêng của bạn
                    ProgressBar(
                        progress =uiState.progress,
                        iconResId = R.drawable.ic_kitty
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
            )
        },
        bottomBar = {
            val currentDialogue = uiState.currentDialogue
            if (currentDialogue != null) {
                when (uiState.currentStep) {

                    // 1. ĐANG LÀM QUIZ 1 (Chọn nghĩa)
                    ConversationStep.QUIZ_CHOICE -> {
                        if (uiState.checkResult == CheckResult.NEUTRAL) {
                            // Hiển thị các nút chọn
                            QuizSection(
                                question = "Chọn nghĩa đúng của từ gạch chân",
                                options = currentDialogue.options,
                                onAnswerSelected = { viewModel.checkChoiceAnswer(it) }
                            )
                        } else {
                            // Hiển thị popup Đúng/Sai
                            QuizResultOverlay(
                                checkResult = uiState.checkResult,
                                onContinue = { viewModel.onQuizContinue() }
                            )
                        }
                    }

                    // 2. ĐANG LÀM QUIZ 2 (Viết từ)
                    ConversationStep.QUIZ_WRITE -> {
                        if (uiState.checkResult == CheckResult.NEUTRAL) {
                            // HIỂN THỊ Ô CHAT MỚI
                            ChatInputBottomBar(
                                text = userAnswer,
                                onTextChange = {
                                    userAnswer = it
                                    viewModel.clearCheckResult()
                                },
                                onSend = {
                                    if(userAnswer.isNotBlank())
                                        viewModel.checkWriteAnswer(userAnswer)
                                },
                                isEnabled = true
                            )
                        } else {
                            // HIỂN THỊ POPUP KẾT QUẢ (GIỐNG ẢNH TRƯỚC)
                            AnswerFeedbackPopup(
                                wordInfo = uiState.currentWordInfo,
                                checkResult = uiState.checkResult,
                                onContinue = { viewModel.onQuizContinue() }
                            )
                        }
                    }

                    // Các bước khác (LOADING, CONTEXT, DIALOGUE) -> không có bottom bar
                    else -> {
                        Box(modifier = Modifier.height(0.dp)) // Empty
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading || conversation == null) {
            // TODO: Loading UI nếu cần
        } else {
            val firstSpeakerName = conversation.dialogue.firstOrNull()?.speaker ?: ""

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(0.dp))
                // (Context, Image giữ nguyên)
                if (uiState.currentStep >= ConversationStep.CONTEXT) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { ttsManager.speak(conversation.contextDescription) }, modifier = Modifier.size(24.dp)) {
                            Icon(painterResource(id = R.drawable.ic_loudspeaker), "Đọc", tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(conversation.contextDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                AsyncImage(
                    model = conversation.imageUrl,
                    contentDescription = "Context",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .clip(RoundedCornerShape(16.dp))
                )

                // Các câu thoại
                uiState.visibleDialogueLines.forEach { dialogue ->
                    val isUser = dialogue.speaker != firstSpeakerName

                    // --- 4. LOGIC HIỂN THỊ (GẠCH CHÂN / CHỖ TRỐNG) ---
                    val isCurrentLine = dialogue.order == uiState.currentDialogueIndex
                    val showBlank = isCurrentLine && uiState.currentStep == ConversationStep.QUIZ_WRITE

                    DialogueBubble(
                        dialogue = dialogue,
                        isUser = isUser,
                        targetWord = dialogue.vocabularyWord,
                        onSpeakClick = { ttsManager.speak(dialogue.text) },
                        isTranslationVisible = translationVisibleMap[dialogue.order] ?: false,
                        onTranslateClick = {
                            translationVisibleMap = translationVisibleMap.toMutableMap().apply {
                                this[dialogue.order] = !(this[dialogue.order] ?: false)
                            }
                        },
                        // Truyền tham số mới vào DialogueBubble
                        showBlank = showBlank
                    )
                }

                if (uiState.currentStep == ConversationStep.QUIZ_CHOICE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding chuẩn
                        verticalAlignment = Alignment.Bottom, // Căn avatar xuống dưới
                        horizontalArrangement = Arrangement.Start // Căn trái
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.question),
                            contentDescription = "Quiz prompt",
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // --- 5. SỬA LẠI PROMPT "ĐIỀN TỪ" ---
                if (uiState.currentStep == ConversationStep.QUIZ_WRITE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding chuẩn
                        verticalAlignment = Alignment.Bottom, // Căn avatar xuống dưới
                        horizontalArrangement = Arrangement.Start // Căn trái
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.question),
                            contentDescription = "Quiz prompt",
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF3F3F3), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Điền vào chỗ trống",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
