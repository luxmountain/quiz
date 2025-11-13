package com.uilover.project247.ConversationActivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.common.io.Files.append
import com.uilover.project247.ConversationActivity.viewmodels.ConversationDetailViewModel
import com.uilover.project247.ConversationActivity.viewmodels.ConversationPhase
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.models.DialogueLine
import com.uilover.project247.data.models.QuizOption
import com.uilover.project247.utils.TextToSpeechManager
import kotlinx.coroutines.delay
import androidx.compose.ui.text.withStyle
import com.uilover.project247.ConversationActivity.components.DialogueBubble
import com.uilover.project247.ConversationActivity.components.QuizButton
import com.uilover.project247.LearningActivity.components.ProgressBar
import com.uilover.project247.R

@Composable
fun createStyledDialogueText(text: String, target: String): AnnotatedString {
    // Tìm vị trí của từ mục tiêu (không phân biệt hoa thường)
    val startIndex = text.indexOf(target, ignoreCase = true)

    // Nếu không tìm thấy, trả về text bình thường
    if (startIndex == -1) {
        return buildAnnotatedString { append(text) }
    }

    val endIndex = startIndex + target.length

    // Nếu tìm thấy, xây dựng string
    return buildAnnotatedString {
        // Phần 1: Chữ trước từ mục tiêu
        append(text.substring(0, startIndex))

        // Phần 2: Từ mục tiêu (với style)
        withStyle(style = SpanStyle(
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline // Gạch chân
        )) {
            append(text.substring(startIndex, endIndex))
        }

        // Phần 3: Chữ sau từ mục tiêu
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
    LaunchedEffect(uiState.visibleDialogueLines.size, uiState.currentPhase) {
        delay(100)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // ✅ Dùng ProgressBar component riêng của bạn
                    ProgressBar(
                        progress =0.3f,
                        iconResId = R.drawable.ic_kitty
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading || conversation == null) {
            // TODO: Loading UI nếu cần
        } else {
            val firstSpeakerName = conversation.dialogue
                .sortedBy { it.order }
                .firstOrNull()
                ?.speaker ?: ""

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                // ✅ 1. Context ở đầu trang + nút loa
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { ttsManager.speak(conversation.contextDescription) },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource( R.drawable.ic_loudspeaker),
                            contentDescription = "Đọc ngữ cảnh",
                            tint = Color.Unspecified // Giữ nguyên màu icon
                        )
                    }
                    Text(
                        text = conversation.contextDescription,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f)
                    )

                }

                // ✅ 2. Ảnh minh họa bo tròn
                AsyncImage(
                    model = conversation.imageUrl,
                    contentDescription = "Context Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 3. Các câu hội thoại
                uiState.visibleDialogueLines.forEach { dialogue ->
                    val isUser = dialogue.speaker != firstSpeakerName

                    DialogueBubble(
                        dialogue = dialogue,
                        isUser = isUser,
                        targetWord = conversation.targetWord,
                        onSpeakClick = {
                            ttsManager.speak(dialogue.text)
                        }
                    )
                }

                // ✅ 4. Phần câu hỏi Quiz
                if (uiState.currentPhase >= ConversationPhase.QUESTION) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = conversation.questionVi,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    conversation.options.forEach { option ->
                        QuizButton(
                            option = option,
                            checkResult = uiState.checkResult,
                            selectedOptionId = uiState.selectedOptionId,
                            onClick = { viewModel.checkAnswer(option) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
