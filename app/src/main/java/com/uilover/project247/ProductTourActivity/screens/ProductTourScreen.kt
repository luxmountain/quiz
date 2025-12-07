package com.uilover.project247.ProductTourActivity.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.data.models.TourPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductTourScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val pages = remember {
        listOf(
            TourPage(
                title = "Chào mừng đến với MochiVocab! 👋",
                description = "Ứng dụng học từ vựng tiếng Anh hiệu quả với phương pháp flashcard và luyện tập tương tác",
                emoji = "🎉",
                backgroundColor = Color(0xFF6200EA)
            ),
            TourPage(
                title = "Học từ vựng theo chủ đề 📚",
                description = "Hàng trăm từ vựng được sắp xếp theo cấp độ và chủ đề. Mở khóa từng chủ đề sau khi hoàn thành trước đó",
                emoji = "📖",
                backgroundColor = Color(0xFF1976D2)
            ),
            TourPage(
                title = "Flashcard thông minh 🎴",
                description = "Học từ vựng qua hình ảnh, nghe phát âm, xem ví dụ và ghi nhớ nghĩa tiếng Việt",
                emoji = "🃏",
                backgroundColor = Color(0xFF7B1FA2)
            ),
            TourPage(
                title = "Luyện tập đa dạng ✍️",
                description = "Nhiều dạng bài tập: trắc nghiệm, điền từ, nghe và chọn đáp án để củng cố kiến thức",
                emoji = "✅",
                backgroundColor = Color(0xFFE91E63)
            ),
            TourPage(
                title = "Tra từ điển nhanh 🔍",
                description = "Tra cứu nghĩa, phát âm, ví dụ và từ đồng nghĩa/trái nghĩa của bất kỳ từ nào",
                emoji = "📝",
                backgroundColor = Color(0xFF00796B)
            ),
            TourPage(
                title = "Hội thoại thực tế 💬",
                description = "Luyện tập với các hội thoại mẫu trong tình huống thực tế hàng ngày",
                emoji = "🗣️",
                backgroundColor = Color(0xFFF57C00)
            ),
            TourPage(
                title = "AI Study Assistant 🤖",
                description = "Trợ lý AI thông minh giúp bạn học từ vựng hiệu quả hơn với các gợi ý và hỗ trợ cá nhân hóa",
                emoji = "🎓",
                backgroundColor = Color(0xFF5E35B1)
            ),
            TourPage(
                title = "Sẵn sàng bắt đầu! 🚀",
                description = "Hãy bắt đầu hành trình chinh phục tiếng Anh của bạn ngay hôm nay!",
                emoji = "💪",
                backgroundColor = Color(0xFF43A047)
            )
        )
    }
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pages[pagerState.currentPage].backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Bỏ qua",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                TourPageContent(pages[page])
            }
            
            // Indicators
            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val width by animateDpAsState(
                        targetValue = if (index == pagerState.currentPage) 24.dp else 8.dp,
                        label = "indicator_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) 
                                    Color.White 
                                else 
                                    Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Quay lại")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Next/Finish button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = pages[pagerState.currentPage].backgroundColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) 
                            "Bắt đầu" 
                        else 
                            "Tiếp theo",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TourPageContent(page: TourPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji/Icon
        Text(
            text = page.emoji,
            fontSize = 120.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Description
        Text(
            text = page.description,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}
