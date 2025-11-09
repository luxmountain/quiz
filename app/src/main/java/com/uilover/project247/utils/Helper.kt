package com.uilover.project247.utils

import android.text.Html
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.text.HtmlCompat

/**
 * Parse HTML string và tạo AnnotatedString với formatting
 * Hỗ trợ: <b>, <u>, <i> tags
 * @param htmlText Text có chứa HTML tags
 * @return AnnotatedString với formatting
 */
fun parseHtmlToAnnotatedString(htmlText: String): AnnotatedString {
    return buildAnnotatedString {
        try {
            // Parse HTML thành Spanned
            val spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val text = spanned.toString()
            
            // Tìm các phần được format (bold, underline)
            val boldPattern = Regex("<b>(.*?)</b>", RegexOption.IGNORE_CASE)
            val underlinePattern = Regex("<u>(.*?)</u>", RegexOption.IGNORE_CASE)
            val combinedPattern = Regex("<b><u>(.*?)</u></b>|<u><b>(.*?)</b></u>", RegexOption.IGNORE_CASE)
            
            var lastIndex = 0
            val cleanText = htmlText
                .replace(Regex("<b><u>|</u></b>|<u><b>|</b></u>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("</?[bu]>", RegexOption.IGNORE_CASE), "")
            
            // Tìm vị trí của text được bold và underline
            combinedPattern.findAll(htmlText).forEach { match ->
                val matchedText = match.groupValues.find { it.isNotEmpty() && it != match.value } ?: return@forEach
                val startIndex = cleanText.indexOf(matchedText, lastIndex)
                if (startIndex >= 0) {
                    // Add text trước match
                    if (startIndex > lastIndex) {
                        append(cleanText.substring(lastIndex, startIndex))
                    }
                    // Add text với bold + underline style
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                        append(matchedText)
                    }
                    lastIndex = startIndex + matchedText.length
                }
            }
            
            // Add phần còn lại
            if (lastIndex < cleanText.length) {
                append(cleanText.substring(lastIndex))
            }
            
            // Fallback: nếu không có tag nào, dùng text gốc
            if (length == 0) {
                append(cleanText)
            }
        } catch (e: Exception) {
            // Fallback: remove tất cả HTML tags
            append(htmlText.replace(Regex("<.*?>"), ""))
        }
    }
}

/**
 * Tạo câu văn với từ được in đậm
 * @param sentence Câu văn gốc
 * @param wordToBold Từ cần in đậm
 * @return AnnotatedString với từ được in đậm
 */
fun createBoldText(sentence: String, wordToBold: String): AnnotatedString {
    return buildAnnotatedString {
        try {
            val startIndex = sentence.indexOf(wordToBold, ignoreCase = true)
            if (startIndex == -1) {
                append(sentence)
                return@buildAnnotatedString
            }
            val endIndex = startIndex + wordToBold.length
            append(sentence.substring(0, startIndex))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(sentence.substring(startIndex, endIndex))
            }
            append(sentence.substring(endIndex))
        } catch (e: Exception) {
            append(sentence)
        }
    }
}

/**
 * Chuyển đổi loại từ sang dạng viết tắt
 * @param wordType Loại từ (noun, verb, adjective, etc.)
 * @return Dạng viết tắt (n, v, adj, etc.)
 */
fun getWordTypeAbbreviation(wordType: String): String {
    return when (wordType.lowercase()) {
        "noun" -> "n"
        "verb" -> "v"
        "adjective" -> "adj"
        "adverb" -> "adv"
        "preposition" -> "prep"
        "conjunction" -> "conj"
        else -> wordType.take(3) // Fallback: lấy 3 ký tự đầu
    }
}
