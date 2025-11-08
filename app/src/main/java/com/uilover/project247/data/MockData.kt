package com.uilover.project247.data

object MockData {
    // --- CẤP 1: DANH SÁCH CÁC TOPIC ---
    val allTopics = listOf(
        Topic(
            id = "topic1_animals",
            title = "Animals",
            subtitle = "1. Động vật",
            imageResId = android.R.drawable.ic_menu_gallery // Placeholder
        ),
        Topic(
            id = "topic2_food",
            title = "Food",
            subtitle = "2. Đồ ăn",
            imageResId = android.R.drawable.ic_menu_gallery // Placeholder
        ),
        Topic(
            id = "topic3_school",
            title = "Schools",
            subtitle = "3. Trường học",
            imageResId = android.R.drawable.ic_menu_gallery // Placeholder
        )
    )

    // --- CẤP 2 & 3: TỪ VỰNG VÀ QUIZ (Đã lồng nhau) ---

    // -- BỘ TỪ CHO "ANIMALS" (topic1) --
    private val animalWords = listOf(
        VocabularyWord(
            id = "a1",
            word = "Dog",
            meaning = "Con chó",
            pronunciation = "/dɔːɡ/",
            exampleSentence = "The dog is barking.",
            quizzes = listOf(
                MultipleChoiceQuiz(
                    question = "Nghĩa của từ 'Dog' là gì?",
                    options = listOf("Con mèo", "Con chó", "Con gà", "Con lợn"),
                    correctAnswer = "Con chó"
                ),
                MultipleChoiceQuiz(
                    question = "Từ 'Con chó' trong tiếng Anh là gì?",
                    options = listOf("Cat", "Pig", "Dog", "Bird"),
                    correctAnswer = "Dog"
                )
            )
        ),
        VocabularyWord(
            id = "a2",
            word = "Cat",
            meaning = "Con mèo",
            pronunciation = "/kæt/",
            exampleSentence = "The cat is sleeping.",
            quizzes = listOf(
                MultipleChoiceQuiz(
                    question = "Nghĩa của từ 'Cat' là gì?",
                    options = listOf("Con mèo", "Con chuột", "Con chim", "Con cá"),
                    correctAnswer = "Con mèo"
                )
            )
        ),
        VocabularyWord(
            id = "a3",
            word = "Bird",
            meaning = "Con chim",
            pronunciation = "/bɜːrd/",
            exampleSentence = "A bird is flying.",
            quizzes = listOf(
                MultipleChoiceQuiz(
                    question = "___ is flying.",
                    options = listOf("Dog", "Cat", "Bird", "Fish"),
                    correctAnswer = "Bird"
                )
            )
        )
    )

    // -- BỘ TỪ CHO "FOOD" (topic2) --
    private val foodWords = listOf(
        VocabularyWord(
            id = "f1",
            word = "Apple",
            meaning = "Quả táo",
            pronunciation = "/ˈæpl/",
            exampleSentence = "I eat an apple every day.",
            quizzes = listOf(
                MultipleChoiceQuiz(
                    question = "Nghĩa của từ 'Apple' là gì?",
                    options = listOf("Quả chuối", "Quả cam", "Quả nho", "Quả táo"),
                    correctAnswer = "Quả táo"
                )
            )
        ),
        VocabularyWord(
            id = "f2",
            word = "Bread",
            meaning = "Bánh mì",
            pronunciation = "/bred/",
            exampleSentence = "I buy bread for breakfast.",
            quizzes = listOf(
                MultipleChoiceQuiz(
                    question = "Từ 'Bánh mì' trong tiếng Anh là gì?",
                    options = listOf("Rice", "Noodle", "Bread", "Cake"),
                    correctAnswer = "Bread"
                )
            )
        )
    )

    // -- BỘ TỪ CHO "SCHOOLS" (topic3) --
    // (Dùng lại từ "character" mà chúng ta đã test)
    private val schoolWords = listOf(
        VocabularyWord(
            id = "s1",
            word = "character",
            meaning = "Tính cách, cá tính (n)",
            pronunciation = "/ˈkerəktər/",
            exampleSentence = "His father has a strong impact on his character.",
            quizzes = listOf(
                MultipleChoiceQuiz(
                    question = "Nghĩa của từ 'character' là gì?",
                    options = listOf("Tính cách, cá tính (n)", "Giáo viên (n)", "Bài kiểm tra (n)"),
                    correctAnswer = "Tính cách, cá tính (n)"
                )
            )
        )
    )


    // --- CẤP 0: MAP (BẢN ĐỒ) ĐỂ LIÊN KẾT MỌI THỨ ---
    // Đây là thứ mà ViewModel của bạn sẽ sử dụng.
    // Nó liên kết ID của Topic với Danh sách từ vựng tương ứng.
    val wordsByTopicId: Map<String, List<VocabularyWord>> = mapOf(
        "topic1_animals" to animalWords,
        "topic2_food" to foodWords,
        "topic3_school" to schoolWords
    )
}