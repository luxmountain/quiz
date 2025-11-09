package com.uilover.project247.data

/**
 * Cấp độ 3: Một câu hỏi trắc nghiệm
 * @param question Câu hỏi (ví dụ: "Nghĩa của 'Dog' là gì?")
 * @param options Danh sách các lựa chọn (ví dụ: ["Con mèo", "Con chó", ...])
 * @param correctAnswer Đáp án đúng (phải khớp chính xác với 1 trong các options)
 */
data class MultipleChoiceQuiz(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

/**
 * Cấp độ 2: Một từ vựng
 * Đã được cập nhật để chứa danh sách quiz của riêng nó
 */
data class VocabularyWord(
    val id: String,
    val word: String,
    val meaning: String,
    val pronunciation: String?,
    val exampleSentence: String?,
    // MỖI TỪ sẽ có 1 list các quiz (trắc nghiệm, điền từ, v.v.)
    // Hiện tại chúng ta chỉ thêm trắc nghiệm
    val quizzes: List<MultipleChoiceQuiz>
)

/**
 * Cấp độ 1: Chủ đề
 */
data class Topic(
    val id: String, // Dùng String cho ID để linh hoạt
    val title: String,
    val subtitle: String,
    val imageResId: Int
)