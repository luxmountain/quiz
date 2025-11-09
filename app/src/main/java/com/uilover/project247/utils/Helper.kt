package com.uilover.project247.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

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
