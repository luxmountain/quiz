    package com.uilover.project247.data.repository

    import android.content.Context
    import android.content.SharedPreferences
    import android.util.Log
    import com.google.gson.Gson
    import com.google.gson.reflect.TypeToken
    import com.uilover.project247.data.models.*
    import kotlinx.coroutines.channels.awaitClose
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.callbackFlow
    import kotlin.random.Random

    /**
    * Repository quản lý Review Feature với Spaced Repetition
    */
    class ReviewRepository(context: Context) {
        
        private val prefs: SharedPreferences = context.getSharedPreferences(
            "review_progress",
            Context.MODE_PRIVATE
        )
        private val gson = Gson()
        private val firebaseRepository = FirebaseRepository()
        
    companion object {
        private const val TAG = "ReviewRepository"
        private const val KEY_FLASHCARD_PROGRESS = "flashcard_progress"
        
        // ========== TEST MODE: 20 MINUTES INTERVAL ==========
        // User requested: 20 MINUTES for easier testing
        private const val TEST_MODE = false
        private const val TEST_INTERVAL = 60 * 6 * 1000L  // 20 MINUTES (1200 seconds)
        
        // ========== REVIEW BUFFER: 5 MINUTES ==========
        // Gom các từ "sắp đến hạn" (trong vòng 5 phút) để ôn luôn một thể
        // Tránh tình trạng ngồi đợi từng từ rớt xuống cách nhau vài giây/phút
        private const val REVIEW_BUFFER = 0L  // 5 minutes buffer
        
        // Spaced Repetition Intervals (Thang ghi nhớ)
        // Will be used when TEST_MODE = false
        private const val LEVEL_1_MINUTES = 0L        // Thang 1: 0 minutes - Ôn ngay (Từ mới học + Từ làm sai)
        private const val LEVEL_2_HOURS = 10L         // Thang 2: 10 giờ
        private const val LEVEL_3_DAYS = 3L           // Thang 3: 3 ngày
        private const val LEVEL_4_DAYS = 7L           // Thang 4: 7 ngày
        private const val LEVEL_5_DAYS = 10L          // Thang 5: 10 ngày
    }        // ==================== FLASHCARD PROGRESS MANAGEMENT ====================
        
        /**
        * Lưu progress của một flashcard
        */
        fun saveFlashcardProgress(progress: FlashcardProgress) {
            val allProgress = getAllFlashcardProgress().toMutableMap()
            allProgress[progress.flashcardId] = progress
            
            val json = gson.toJson(allProgress)
            prefs.edit().putString(KEY_FLASHCARD_PROGRESS, json).apply()
            
            Log.d(TAG, "Saved progress for flashcard: ${progress.flashcardId}, level: ${progress.level}")
        }
        
        /**
        * Lấy progress của một flashcard
        */
        fun getFlashcardProgress(flashcardId: String): FlashcardProgress? {
            return getAllFlashcardProgress()[flashcardId]
        }
        
        /**
        * Lấy tất cả flashcard progress
        */
        fun getAllFlashcardProgress(): Map<String, FlashcardProgress> {
            val json = prefs.getString(KEY_FLASHCARD_PROGRESS, null) ?: return emptyMap()
            val type = object : TypeToken<Map<String, FlashcardProgress>>() {}.type
            return gson.fromJson(json, type)
        }
        
        /**
        * Đánh dấu flashcard đã học xong (sau khi hoàn thành flashcard + write + listen)
        * THANG 1 = Ôn ngay sau khi học (0 giây)
        * Sau khi ôn tập đúng ở Thang 1 → Lên Thang 2 (10h)
        */
        fun markFlashcardLearned(flashcardId: String, word: String) {
            val existing = getFlashcardProgress(flashcardId)
            val now = System.currentTimeMillis()
            
            val progress = if (existing != null) {
                existing.copy(
                    learned = true,
                    level = 1,  // THANG 1 - Ôn ngay
                    lastReviewDate = now,
                    nextReviewDate = calculateNextReviewDate(1, now)  // 0 giây = Ôn ngay
                )
            } else {
                FlashcardProgress(
                    flashcardId = flashcardId,
                    word = word,
                    learned = true,
                    level = 1,  // THANG 1 - Ôn ngay
                    lastReviewDate = now,
                    nextReviewDate = calculateNextReviewDate(1, now),  // 0 giây = Ôn ngay
                    createdAt = now
                )
            }
            
            saveFlashcardProgress(progress)
            
            Log.d(TAG, "Marked flashcard '$word' as learned - THANG 1 (Ôn ngay)")
            Log.d(TAG, "  flashcardId: $flashcardId")
            Log.d(TAG, "  nextReviewDate: ${progress.nextReviewDate} (now: $now)")
            Log.d(TAG, "  isDueForReview: ${progress.isDueForReview()}")
        }
        
        /**
        * Đánh dấu "Tôi đã biết từ này"
        */
        fun markFlashcardKnownAlready(flashcardId: String, word: String) {
            val progress = FlashcardProgress(
                flashcardId = flashcardId,
                word = word,
                learned = true,
                knownAlready = true,
                level = 5,
                lastReviewDate = System.currentTimeMillis(),
                nextReviewDate = Long.MAX_VALUE,
                createdAt = System.currentTimeMillis()
            )
            
            saveFlashcardProgress(progress)
        }
        
        // ==================== REVIEW STATS ====================
        
        /**
        * Observe Review Stats in Real-time (Flow)
        * Automatically emits new stats when SharedPreferences changes
        */
        fun observeReviewStats(): Flow<ReviewStats> = callbackFlow {
            // Emit initial stats
            trySend(getReviewStats())
            
            // Listen for changes
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_FLASHCARD_PROGRESS) {
                    Log.d(TAG, "[REAL-TIME] Progress changed - emitting new stats")
                    trySend(getReviewStats())
                }
            }
            
            prefs.registerOnSharedPreferenceChangeListener(listener)
            
            // Cleanup
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
                Log.d(TAG, "[REAL-TIME] Stopped observing stats")
            }
        }
        
        /**
        * Tính toán thống kê cho Dashboard
        * STANDARD SPACED REPETITION: dueCount = ALL words where nextReviewDate <= Now
        */
        fun getReviewStats(): ReviewStats {
            val allProgress = getAllFlashcardProgress().values
            val currentTime = System.currentTimeMillis()
            
            // Lọc chỉ lấy từ trong "Sổ tay"
            val notebookWords = allProgress.filter { it.isInNotebook() }
            
            Log.d(TAG, "========================")
            Log.d(TAG, "GET REVIEW STATS (STANDARD SR)")
            Log.d(TAG, "Current time: $currentTime")
            Log.d(TAG, "Total progress entries: ${allProgress.size}")
            Log.d(TAG, "Notebook words (learned && !knownAlready): ${notebookWords.size}")
            
            // Đếm phân bố theo level
            val level1Count = notebookWords.count { it.level == 1 }
            val level2Count = notebookWords.count { it.level == 2 }
            val level3Count = notebookWords.count { it.level == 3 }
            val level4Count = notebookWords.count { it.level == 4 }
            val level5Count = notebookWords.count { it.level == 5 }
            
        Log.d(TAG, "Distribution: L1=$level1Count, L2=$level2Count, L3=$level3Count, L4=$level4Count, L5=$level5Count")
        
        // REVIEW BUFFER: Count words within 5-minute buffer
        val bufferTime = currentTime + REVIEW_BUFFER
        val dueWordsWithBuffer = notebookWords.filter { it.nextReviewDate <= bufferTime }
        val strictDueWords = notebookWords.filter { it.isDueForReview() }
        
        val dueCount = dueWordsWithBuffer.size
        val strictDueCount = strictDueWords.size
        val bufferCount = dueCount - strictDueCount
        
        Log.d(TAG, "⏰ REVIEW BUFFER (5 minutes):")
        Log.d(TAG, "  - Strict due (now): $strictDueCount")
        Log.d(TAG, "  - Buffer (within 5min): $bufferCount")
        Log.d(TAG, "  - Total shown: $dueCount")
        
        val dueLevelCounts = dueWordsWithBuffer.groupBy { it.level }.mapValues { it.value.size }
        Log.d(TAG, "Due by level (with buffer): $dueLevelCounts")
        
        dueWordsWithBuffer.take(5).forEach {
            val timeUntil = (it.nextReviewDate - currentTime) / 1000
            if (timeUntil > 0) {
                Log.d(TAG, "  - '${it.word}' Level ${it.level}, due in ${timeUntil}s (buffered)")
            } else {
                Log.d(TAG, "  - '${it.word}' Level ${it.level}, overdue by ${-timeUntil}s")
            }
        }
        
        // COUNTDOWN: Min nextReviewDate of FUTURE words (date > bufferTime)
        val futureWords = notebookWords.filter { it.nextReviewDate > bufferTime }
        val nextReviewTime = futureWords.minByOrNull { it.nextReviewDate }?.nextReviewDate;            if (nextReviewTime != null) {
                val timeUntil = (nextReviewTime - currentTime) / 1000
                val nextWord = futureWords.minByOrNull { it.nextReviewDate }
                Log.d(TAG, "⏰ COUNTDOWN: Next review in ${timeUntil}s")
                Log.d(TAG, "  - Next word: '${nextWord?.word}' (Level ${nextWord?.level})")
            } else {
                Log.d(TAG, "⏰ COUNTDOWN: No future words")
            }
            
            Log.d(TAG, "========================")
            
            return ReviewStats(
                totalWordsInNotebook = notebookWords.size,
                level1Count = level1Count,
                level2Count = level2Count,
                level3Count = level3Count,
                level4Count = level4Count,
                level5Count = level5Count,
                dueForReviewCount = dueCount,
                nextReviewTime = nextReviewTime
            )
        }
        
        /**
        * Lấy batch từ để ôn tập
        * 
        * STANDARD SPACED REPETITION:
        * - Filter: ALL words where nextReviewDate <= Now
        * - Sort Priority 1: Level 1 first (newly learned or just failed)
        * - Sort Priority 2: Oldest date first (ascending nextReviewDate)
        * - Take limit
        */
        suspend fun getReviewBatch(limit: Int = 10): List<Flashcard> {
            val allProgress = getAllFlashcardProgress()
            val currentTime = System.currentTimeMillis()
            
            Log.d(TAG, "========== GET REVIEW BATCH (STANDARD SR) ==========")
            Log.d(TAG, "Total progress entries: ${allProgress.size}")
            
        // Step A: Lọc TẤT CẢ từ trong Sổ tay
        val notebookWords = allProgress.values.filter { it.isInNotebook() }
        Log.d(TAG, "Notebook words (learned && !knownAlready): ${notebookWords.size}")
        
        // Debug: Log level distribution
        val levelCounts = notebookWords.groupBy { it.level }.mapValues { it.value.size }
        Log.d(TAG, "Level distribution: $levelCounts")
        
        if (notebookWords.isEmpty()) {
            Log.d(TAG, "📭 EMPTY: No words in notebook")
            Log.d(TAG, "=======================================")
            return emptyList()
        }
        
        // Step B: Filter - Words with REVIEW BUFFER (gom từ sắp đến hạn trong 5 phút)
        // Deadline <= Now + 5 phút (thay vì chỉ <= Now)
        val bufferTime = currentTime + REVIEW_BUFFER
        val dueWords = notebookWords.filter { it.nextReviewDate <= bufferTime }
        
        val strictDueCount = notebookWords.count { it.nextReviewDate <= currentTime }
        val bufferCount = dueWords.size - strictDueCount
        
        Log.d(TAG, "⏰ REVIEW BUFFER ENABLED (5 minutes):")
        Log.d(TAG, "  - Strict due (now): $strictDueCount words")
        Log.d(TAG, "  - Buffer (within 5min): $bufferCount words")
        Log.d(TAG, "  - Total batch: ${dueWords.size} words")
        
        if (dueWords.isEmpty()) {
            Log.d(TAG, "⏰ WAITING: No words due yet (even with 5min buffer)")
            
            // Debug: Show next upcoming words
            val upcoming = notebookWords
                .filter { it.nextReviewDate > bufferTime }
                .sortedBy { it.nextReviewDate }
                .take(5)
            
            upcoming.forEachIndexed { index, word ->
                val timeUntil = (word.nextReviewDate - currentTime) / 1000
                Log.d(TAG, "  Upcoming[$index]: '${word.word}' Level ${word.level}, in ${timeUntil}s")
            }
            
            Log.d(TAG, "=======================================")
            return emptyList()
        }            // Step C: Sort Priority
            // Priority 1: Level 1 MUST be first (it.level != 1 → false for L1, true for L2+)
            // Priority 2: Oldest date first (ascending nextReviewDate)
            val sortedDueWords = dueWords.sortedWith(compareBy(
                { it.level != 1 },        // Level 1 first (false < true)
                { it.nextReviewDate }     // Then oldest first
            ))
            
            // Debug: Log level distribution of due words
            val dueLevelCounts = dueWords.groupBy { it.level }.mapValues { it.value.size }
            Log.d(TAG, "Due by level: $dueLevelCounts")
            
            // Step D: Take limit
            val batchProgress = sortedDueWords.take(limit)
            
            Log.d(TAG, "--- SELECTED BATCH (${batchProgress.size} words) ---")
            batchProgress.forEachIndexed { index, word ->
                val overdue = (currentTime - word.nextReviewDate) / 1000
                Log.d(TAG, "  [$index] '${word.word}' - Level ${word.level}, overdue by ${overdue}s")
            }
            
            // VERIFY: Count levels in selected batch
            val selectedLevelCounts = batchProgress.groupBy { it.level }.mapValues { it.value.size }
            Log.d(TAG, "Batch composition: $selectedLevelCounts")
            
            // Fetch Flashcard data từ Firebase
            return fetchFlashcardsFromFirebase(batchProgress)
        }
        
        /**
        * Fetch flashcard data từ Firebase
        */
        private suspend fun fetchFlashcardsFromFirebase(progressList: List<FlashcardProgress>): List<Flashcard> {
            val batchFlashcards = mutableListOf<Flashcard>()
            val levels = firebaseRepository.getLevels()
            
            Log.d(TAG, "--- Fetching flashcard content ---")
            for (progress in progressList) {
                Log.d(TAG, "Fetching: '${progress.word}' (id=${progress.flashcardId}, level=${progress.level})")
                
                var found = false
                for (level in levels) {
                    val topics = firebaseRepository.getTopicsByLevel(level.id)
                    for (topic in topics) {
                        val topicDetail = firebaseRepository.getTopic(level.id, topic.id)
                        val flashcard = topicDetail?.flashcards?.find { it.id == progress.flashcardId }
                        
                        if (flashcard != null) {
                            batchFlashcards.add(flashcard)
                            Log.d(TAG, "  ✓ Found in ${level.name}/${topic.name}")
                            found = true
                            break
                        }
                    }
                    if (found) break
                }
                
                if (!found) {
                    Log.e(TAG, "  ✗ ERROR: Flashcard '${progress.word}' (id=${progress.flashcardId}) NOT FOUND in Firebase!")
                    Log.e(TAG, "  Skipping this word...")
                }
            }
            
            Log.d(TAG, "Final batch: ${batchFlashcards.size} flashcards")
            Log.d(TAG, "=======================================")
            
            return batchFlashcards
        }
        
        // ==================== REVIEW EXERCISES ====================
        
        /**
        * Tạo bài tập ôn tập từ flashcard
        */
        suspend fun createReviewExercise(flashcard: Flashcard): ReviewExercise {
            // Random chọn 1 trong 3 dạng bài tập
            val type = ReviewExerciseType.values().random()
            
            return when (type) {
                ReviewExerciseType.LISTEN_AND_WRITE -> createListenAndWriteExercise(flashcard)
                ReviewExerciseType.FILL_IN_BLANK -> createFillInBlankExercise(flashcard)
                ReviewExerciseType.MULTIPLE_CHOICE -> createMultipleChoiceExercise(flashcard)
            }
        }
        
        private fun createListenAndWriteExercise(flashcard: Flashcard): ReviewExercise {
            return ReviewExercise(
                flashcard = flashcard,
                type = ReviewExerciseType.LISTEN_AND_WRITE,
                question = "Nghe và gõ lại từ bạn nghe được",
                correctAnswer = flashcard.word.lowercase()
            )
        }
        
        private fun createFillInBlankExercise(flashcard: Flashcard): ReviewExercise {
            // Parse HTML để lấy câu context
            val sentence = flashcard.contextSentence
                .replace("<b>", "")
                .replace("</b>", "")
                .replace("<u>", "")
                .replace("</u>", "")
                .replace("<i>", "")
                .replace("</i>", "")
            
            // Thay thế từ bằng _____
            val questionText = sentence.replace(
                flashcard.word,
                "_____",
                ignoreCase = true
            )
            
            return ReviewExercise(
                flashcard = flashcard,
                type = ReviewExerciseType.FILL_IN_BLANK,
                question = questionText,
                correctAnswer = flashcard.word.lowercase()
            )
        }
        
        private suspend fun createMultipleChoiceExercise(flashcard: Flashcard): ReviewExercise {
            // Lấy 3 từ khác làm đáp án sai
            val wrongOptions = getRandomWrongOptions(flashcard, count = 3)
            
            // CRITICAL FIX: Pre-shuffle options ONCE in repository
            // This prevents UI jumping when recomposition happens
            val allOptions = (wrongOptions + flashcard.word).shuffled()
            
            return ReviewExercise(
                flashcard = flashcard,
                type = ReviewExerciseType.MULTIPLE_CHOICE,
                question = "Từ nào có nghĩa là: \"${flashcard.word}\"?",
                correctAnswer = flashcard.meaning,
                options = allOptions  // Pre-shuffled - UI must NOT shuffle again
            )
        }
        
    /**
     * Lấy đáp án sai cho Multiple Choice - LUÔN ĐẢM BẢO ĐỦ 3 ĐÁP ÁN SAI
     * 
     * LOGIC 3 TẦNG BẢO VỆ (Ưu tiên Learned → Global → Mock):
     * 
     * Tier 1: Ưu tiên từ "Sổ tay" (Learned Words)
     *   - Lấy từ đã học để ôn lại thụ động
     *   - Lọc trùng với đáp án đúng
     *   - Nếu đủ 3 từ → Dùng luôn
     * 
     * Tier 2: Thiếu thì bù từ Kho Global (Firebase)
     *   - Lấy từ 2 levels đầu, mỗi level 3 topics
     *   - Lọc trùng với Tier 1 và đáp án đúng
     *   - Bù đủ số còn thiếu
     * 
     * Tier 3: Vẫn thiếu → Mock Data (100+ từ tiếng Anh)
     *   - Fallback khi offline/lỗi DB
     *   - ĐẢM BẢO app không bao giờ crash do thiếu đáp án
     * 
     * @return LUÔN TRẢ VỀ ĐÚNG 'count' ĐÁP ÁN (3 đáp án sai = 4 đáp án total)
     */
        /**
         * Lấy đáp án sai (Distractors) là NGHĨA TIẾNG VIỆT
         */
        suspend fun getRandomWrongOptions(correctFlashcard: Flashcard, count: Int): List<String> {
            val wrongOptions = mutableListOf<String>()
            val correctMeaning = correctFlashcard.meaning.lowercase().trim()

            // ========== TIER 1 & 2: Lấy từ Firebase Global (Gộp làm 1 cho gọn) ==========
            // Vì FlashcardProgress chỉ lưu từ tiếng Anh, nên ta buộc phải quét Firebase
            // để lấy nghĩa Tiếng Việt của các từ khác.
            try {
                val globalMeanings = mutableListOf<String>()
                val levels = firebaseRepository.getLevels()

                // Chiến thuật: Quét 2 Level đầu, mỗi level 3 Topic để tìm nghĩa
                for (level in levels.take(2)) {
                    val topics = firebaseRepository.getTopicsByLevel(level.id)
                    for (topic in topics.take(3)) {
                        val topicDetail = firebaseRepository.getTopic(level.id, topic.id)
                        topicDetail?.flashcards?.forEach { fc ->
                            val meaning = fc.meaning.trim()
                            // Điều kiện: Nghĩa phải khác đáp án đúng
                            if (meaning.lowercase() != correctMeaning && meaning.isNotBlank()) {
                                globalMeanings.add(meaning) // Lấy NGHĨA TIẾNG VIỆT
                            }
                        }
                        if (globalMeanings.size >= count * 5) break
                    }
                    if (globalMeanings.size >= count * 5) break
                }

                // Random lấy ra số lượng cần thiết
                val selected = globalMeanings
                    .distinct()
                    .filter { it.lowercase() !in wrongOptions.map { w -> w.lowercase() } }
                    .shuffled()
                    .take(count)

                wrongOptions.addAll(selected)

                if (wrongOptions.size >= count) {
                    return wrongOptions.take(count)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching global meanings", e)
            }
            // Danh sách nghĩa tiếng Việt dự phòng (Dùng khi mạng lỗi hoặc mới học ít từ)
            val BACKUP_MEANINGS = listOf(
                "Con mèo", "Con chó", "Ngôi nhà", "Cái xe", "Quyển sách", "Cái bút", "Xin chào",
                "Trường học", "Tình yêu", "Thời gian", "Tiền bạc", "Nước", "Thức ăn", "Bạn bè",
                "Gia đình", "Công việc", "Hạnh phúc", "Thành phố", "Âm nhạc", "Bác sĩ",
                "Máy tính", "Điện thoại", "Bầu trời", "Mặt trời", "Mặt trăng", "Cây cối",
                "Hoa hồng", "Dòng sông", "Biển cả", "Ngọn núi", "Đám mây", "Cơn mưa",
                "Mùa hè", "Mùa đông", "Buổi sáng", "Buổi tối", "Giấc mơ", "Hy vọng",
                "Sức khỏe", "Bệnh viện", "Cảnh sát", "Quân đội", "Hòa bình", "Chiến tranh",
                "Lịch sử", "Tương lai", "Quá khứ", "Hiện tại", "Thế giới", "Con người"
            )
            // ========== TIER 3: Backup Data (TIẾNG VIỆT) ==========
            // Nếu vẫn thiếu (do mạng lỗi, DB ít từ), dùng list dự phòng Tiếng Việt
            val needed = count - wrongOptions.size
            if (needed > 0) {
                val backups = BACKUP_MEANINGS
                    .filter { it.lowercase() != correctMeaning } // Tránh trùng đáp án đúng
                    .shuffled()
                    .take(needed)
                wrongOptions.addAll(backups)
            }

            return wrongOptions.distinct().take(count)
        }
    suspend fun getRandomWrongOptions2(correctFlashcard: Flashcard, count: Int): List<String> {
        Log.d(TAG, "========== GET WRONG OPTIONS (3-TIER PROTECTION) ==========")
        Log.d(TAG, "Target: $count wrong options for '${correctFlashcard.word}'")
        
        val wrongOptions = mutableListOf<String>()
        val correctWord = correctFlashcard.word.lowercase().trim()
        
        // ========== TIER 1: Ưu tiên từ Sổ tay (Learned Words) ==========
        try {
            val learnedWords = getAllFlashcardProgress()
                .values
                .filter { 
                    it.isInNotebook() && 
                    it.word.lowercase().trim() != correctWord &&
                    it.word.isNotBlank()
                }
                .map { it.word }
                .distinct()
                .shuffled()
            
            val tier1Count = minOf(learnedWords.size, count)
            wrongOptions.addAll(learnedWords.take(tier1Count))
            Log.d(TAG, "✅ TIER 1 (Learned): Got ${tier1Count}/$count from notebook")
            
            if (wrongOptions.size >= count) {
                Log.d(TAG, "🎯 SUCCESS from Tier 1 only! Result: $wrongOptions")
                Log.d(TAG, "=======================================")
                return wrongOptions.take(count)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ TIER 1 FAILED: ${e.message}", e)
        }
        
        // ========== TIER 2: Bù từ Firebase Global ==========
        val neededFromTier2 = count - wrongOptions.size
        if (neededFromTier2 > 0) {
            Log.d(TAG, "⚠️ TIER 2 (Global): Need $neededFromTier2 more options")
            
            try {
                val globalWords = mutableListOf<String>()
                val levels = firebaseRepository.getLevels()
                
                for (level in levels.take(2)) {
                    val topics = firebaseRepository.getTopicsByLevel(level.id)
                    for (topic in topics.take(3)) {
                        val topicDetail = firebaseRepository.getTopic(level.id, topic.id)
                        topicDetail?.flashcards?.let { flashcards ->
                            flashcards.forEach { fc ->
                                if (fc.word.lowercase().trim() != correctWord && fc.word.isNotBlank()) {
                                    globalWords.add(fc.word)
                                }
                            }
                        }
                        
                        // Early exit nếu đã đủ
                        if (globalWords.size >= neededFromTier2 * 5) break
                    }
                    if (globalWords.size >= neededFromTier2 * 5) break
                }
                
                val tier2Words = globalWords
                    .distinct()
                    .filter { it.lowercase().trim() !in wrongOptions.map { w -> w.lowercase().trim() } }
                    .shuffled()
                    .take(neededFromTier2)
                
                wrongOptions.addAll(tier2Words)
                Log.d(TAG, "✅ TIER 2 (Global): Got ${tier2Words.size}/$neededFromTier2 from Firebase")
                
                if (wrongOptions.size >= count) {
                    Log.d(TAG, "🎯 SUCCESS from Tier 1+2! Result: $wrongOptions")
                    Log.d(TAG, "=======================================")
                    return wrongOptions.take(count)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ TIER 2 FAILED: ${e.message}", e)
            }
        }
        
        // ========== TIER 3: Mock Data - EMERGENCY FALLBACK ==========
        val neededFromTier3 = count - wrongOptions.size
        if (neededFromTier3 > 0) {
            Log.d(TAG, "🆘 TIER 3 (Mock): Need $neededFromTier3 more (EMERGENCY FALLBACK)")
            
            // 100+ từ tiếng Anh phổ biến - ĐẢM BẢO luôn đủ
            val mockWords = listOf(
                "Con mèo", "Con chó", "Ngôi nhà", "Cái xe", "Quyển sách", "Cái bút", "Xin chào",
                "Trường học", "Tình yêu", "Thời gian", "Tiền bạc", "Nước", "Thức ăn", "Bạn bè",
                "Gia đình", "Công việc", "Hạnh phúc", "Thành phố", "Âm nhạc", "Bác sĩ",
                "Máy tính", "Điện thoại", "Bầu trời", "Mặt trời", "Mặt trăng", "Cây cối",
                "Hoa hồng", "Dòng sông", "Biển cả", "Ngọn núi", "Đám mây", "Cơn mưa",
                "Mùa hè", "Mùa đông", "Buổi sáng", "Buổi tối", "Giấc mơ", "Hy vọng",
                "Sức khỏe", "Bệnh viện", "Cảnh sát", "Quân đội", "Hòa bình", "Chiến tranh",
                "Lịch sử", "Tương lai", "Quá khứ", "Hiện tại", "Thế giới", "Con người"
            )
            
            val tier3Words = mockWords
                .filter { 
                    it.lowercase() != correctWord && 
                    it.lowercase() !in wrongOptions.map { w -> w.lowercase().trim() }
                }
                .shuffled()
                .take(neededFromTier3)
            
            wrongOptions.addAll(tier3Words)
            Log.d(TAG, "✅ TIER 3 (Mock): Added ${tier3Words.size} English words")
        }
        
        // ========== FINAL VERIFICATION ==========
        val finalOptions = wrongOptions.distinct().take(count)
        
        Log.d(TAG, "")
        Log.d(TAG, "📊 FINAL RESULT:")
        Log.d(TAG, "  - Target: $count wrong options")
        Log.d(TAG, "  - Got: ${finalOptions.size} options")
        Log.d(TAG, "  - Options: $finalOptions")
        
        if (finalOptions.size < count) {
            Log.e(TAG, "")
            Log.e(TAG, "🚨 CRITICAL ERROR: STILL MISSING ${count - finalOptions.size} OPTIONS!")
            Log.e(TAG, "This should NEVER happen with 100+ mock words!")
            Log.e(TAG, "Check: Is correctWord filtering too aggressive?")
            Log.e(TAG, "correctWord = '$correctWord'")
        } else {
            Log.d(TAG, "✅ SUCCESS: Exact $count options delivered!")
        }
        
        Log.d(TAG, "=======================================")
        return finalOptions
    }        // ==================== UPDATE PROGRESS ====================
        
        /**
        * Cập nhật progress sau khi ôn tập (Level Up on Success)
        */
        /**
        * Cập nhật progress sau khi ôn tập
        * RESET & RESTART CLOCK LOGIC:
        * - Đúng: Tăng thang (1→2→3→4→5) + RESTART TIMER from NOW
        * - Sai: Reset về THANG 1 (ôn ngay) + nextReviewDate = NOW (immediate)
        */
        fun updateProgressAfterReview(flashcardId: String, isCorrect: Boolean,reviewTime: Long) {
            Log.d(TAG, "========== CẬP NHẬT THANG (RESET & RESTART CLOCK) ==========")
            Log.d(TAG, "Input: flashcardId=$flashcardId, isCorrect=$isCorrect")
            
            val existing = getFlashcardProgress(flashcardId)
            if (existing == null) {
                Log.e(TAG, "ERROR: Flashcard $flashcardId not found in progress!")
                Log.d(TAG, "===================================")
                return
            }
            
            val currentTimeForDebug = System.currentTimeMillis()
            val now = reviewTime
            
            Log.d(TAG, "[TIMING DEBUG] ========================================")
            Log.d(TAG, "[TIMING DEBUG] reviewTime (từ ViewModel): $reviewTime")
            Log.d(TAG, "[TIMING DEBUG] currentTime (System.now): $currentTimeForDebug")
            Log.d(TAG, "[TIMING DEBUG] Difference: ${(currentTimeForDebug - reviewTime) / 1000}s")
            Log.d(TAG, "[TIMING DEBUG] Using 'now' = reviewTime: $now")
            Log.d(TAG, "[TIMING DEBUG] ========================================")
            
            Log.d(TAG, "Từ: ${existing.word}")
            Log.d(TAG, "Kết quả: ${if (isCorrect) "✓ ĐÚNG" else "✗ SAI"}")
            Log.d(TAG, "Thang TRƯỚC: ${existing.level}")
            Log.d(TAG, "Old nextReviewDate: ${existing.nextReviewDate} (${(existing.nextReviewDate - now)/1000}s from now)")
            
            // CASE A: WRONG → Hard Reset to Level 1
            // CASE B: CORRECT → Progress to next level
            val newLevel = if (isCorrect) {
                minOf(existing.level + 1, 5) // Tăng thang, max = 5
            } else {
                1 // Reset về THANG 1 (ôn ngay)
            }
            
            Log.d(TAG, "Thang SAU: $newLevel (${if (isCorrect) "INCREMENT" else "HARD RESET"})")
            
            // CRITICAL: Calculate nextReviewDate from NOW (not from old due date)
            // This RESTARTS the countdown timer from this exact moment
            val nextReviewDate = calculateNextReviewDate(newLevel, now)
            val interval = nextReviewDate - now
            
            Log.d(TAG, "⏰ RESTART CLOCK:")
            Log.d(TAG, "  - Base time: NOW ($now)")
            Log.d(TAG, "  - Interval for Level $newLevel: ${interval/1000}s (${interval/60000}m)")
            Log.d(TAG, "  - New nextReviewDate: $nextReviewDate")
            Log.d(TAG, "  - Timer starts fresh from NOW")
            Log.d(TAG, "  - Expected countdown from NOW: ${interval/60000} minutes")
            
            if (isCorrect) {
                Log.d(TAG, "✅ CORRECT: Level $newLevel countdown starts NOW (${interval/1000}s)")
            } else {
                Log.d(TAG, "❌ WRONG: Reset to Level 1, due IMMEDIATELY (${interval/1000}s)")
            }
            
            val updated = existing.copy(
                level = newLevel,
                lastReviewDate = now,
                nextReviewDate = nextReviewDate,
                correctCount = if (isCorrect) existing.correctCount + 1 else existing.correctCount,
                wrongCount = if (!isCorrect) existing.wrongCount + 1 else existing.wrongCount
            )
            
            Log.d(TAG, "BEFORE save:")
            Log.d(TAG, "  - level: ${existing.level}")
            Log.d(TAG, "  - nextReviewDate: ${existing.nextReviewDate}")
            
            saveFlashcardProgress(updated)
            
            Log.d(TAG, "AFTER save:")
            Log.d(TAG, "  - level: ${updated.level}")
            Log.d(TAG, "  - nextReviewDate: ${updated.nextReviewDate}")
            
            // VERIFY: Read back from SharedPreferences
            val verified = getFlashcardProgress(flashcardId)
            Log.d(TAG, "VERIFIED read-back:")
            Log.d(TAG, "  - level: ${verified?.level}")
            Log.d(TAG, "  - nextReviewDate: ${verified?.nextReviewDate}")
            Log.d(TAG, "  - isDue: ${verified?.isDueForReview()}")
            
            if (verified?.level != newLevel) {
                Log.e(TAG, "⚠️ CRITICAL ERROR: Level not saved correctly!")
                Log.e(TAG, "  Expected: $newLevel, Got: ${verified?.level}")
            } else {
                Log.d(TAG, "✅ Level update CONFIRMED")
            }
            
            if (verified?.nextReviewDate != nextReviewDate) {
                Log.e(TAG, "⚠️ CRITICAL ERROR: nextReviewDate not saved correctly!")
                Log.e(TAG, "  Expected: $nextReviewDate, Got: ${verified?.nextReviewDate}")
            } else {
                Log.d(TAG, "✅ Timer restart CONFIRMED")
            }
            
            Log.d(TAG, "===================================")
        }
        
        /**
        * Tính ngày ôn tập tiếp theo dựa vào level
        * 
        * CRITICAL: ALWAYS calculate from the fromDate parameter (usually NOW)
        * This ensures the countdown timer RESTARTS from the moment of review completion
        * 
        * TEST MODE ENABLED (User Request: 1 HOUR): 
        * - Thang 1: 0 giây (Ôn ngay - immediate)
        * - Thang 2-5: 1 HOUR (3600000ms) for easier testing
        * 
        * Production intervals (when TEST_MODE = false):
        * Thang 1: 0 phút (Ôn ngay - Từ mới học + Từ làm sai)
        * Thang 2: 10 giờ
        * Thang 3: 3 ngày
        * Thang 4: 7 ngày
        * Thang 5: 10 ngày
        */
        private fun calculateNextReviewDate(level: Int, fromDate: Long): Long {
            if (TEST_MODE) {
                // Level 1 = Immediate (0s), Level 2-5 = 1 HOUR (3600000ms) for testing
                val interval = if (level == 1) 0L else TEST_INTERVAL
                val nextDate = fromDate + interval
                
                Log.d(TAG, "⏰ calculateNextReviewDate (TEST MODE - 1 HOUR):")
                Log.d(TAG, "  - Level: $level")
                Log.d(TAG, "  - Interval: ${interval/1000}s (${interval/60000} minutes)")
                Log.d(TAG, "  - From: $fromDate (base time)")
                Log.d(TAG, "  - Next: $nextDate (base + interval)")
                Log.d(TAG, "  - Formula: nextDate = fromDate + interval")
                Log.d(TAG, "  - Verification: $nextDate = $fromDate + $interval")
                
                return nextDate
            }
            
            // Production intervals
            val intervalMillis = when (level) {
                1 -> LEVEL_1_MINUTES * 60 * 1000  // Thang 1: 0 phút = Ôn ngay
                2 -> LEVEL_2_HOURS * 60 * 60 * 1000  // Thang 2: 10 giờ
                3 -> LEVEL_3_DAYS * 24 * 60 * 60 * 1000  // Thang 3: 3 ngày
                4 -> LEVEL_4_DAYS * 24 * 60 * 60 * 1000  // Thang 4: 7 ngày
                5 -> LEVEL_5_DAYS * 24 * 60 * 60 * 1000  // Thang 5: 10 ngày
                else -> 0L  // Default: immediate
            }
            
            return fromDate + intervalMillis
        }
        
        /**
        * Clear all review data (for testing)
        */
        fun clearAllReviewData() {
            prefs.edit().clear().apply()
            Log.d(TAG, "Cleared all review data")
        }
        
        /**
        * Clear specific progress (for testing)
        */
        fun clearReviewProgress() {
            prefs.edit().remove(KEY_FLASHCARD_PROGRESS).apply()
            Log.d(TAG, "========================================")
            Log.d(TAG, "CLEARED ALL FLASHCARD PROGRESS DATA")
            Log.d(TAG, "App will start fresh - learn words again")
            Log.d(TAG, "========================================")
        }
        
        /**
        * Reset ALL progress to Level 1 (for testing)
        * All words become immediately reviewable
        */
        fun resetAllProgress() {
            val allProgress = getAllFlashcardProgress().toMutableMap()
            val now = System.currentTimeMillis()
            var resetCount = 0
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "RESETTING ALL PROGRESS TO LEVEL 1")
            
            allProgress.forEach { (id, progress) ->
                if (progress.isInNotebook()) {
                    val resetProgress = progress.copy(
                        level = 1,
                        nextReviewDate = calculateNextReviewDate(1, now), // Level 1 = immediate
                        lastReviewDate = now,
                        correctCount = 0,
                        wrongCount = 0
                    )
                    allProgress[id] = resetProgress
                    resetCount++
                    Log.d(TAG, "Reset: '${progress.word}' -> Level 1 (Review Now)")
                }
            }
            
            if (resetCount > 0) {
                val json = gson.toJson(allProgress)
                prefs.edit().putString(KEY_FLASHCARD_PROGRESS, json).apply()
                Log.d(TAG, "Total reset: $resetCount words")
            }
            
            Log.d(TAG, "========================================")
        }
        
        /**
        * Fix old data với intervals cũ - Reset Level 1 words về nextReviewDate = now
        * TEST MODE: Sets to 30 seconds from now
        */
        fun fixOldLevel1Data() {
            val allProgress = getAllFlashcardProgress().toMutableMap()
            var fixedCount = 0
            val now = System.currentTimeMillis()
            
            allProgress.forEach { (id, progress) ->
                if (progress.level == 1 && progress.isInNotebook()) {
                    // Recalculate với interval mới (TEST MODE = 30s or 0 minutes)
                    val fixedProgress = progress.copy(
                        nextReviewDate = calculateNextReviewDate(1, now)
                    )
                    allProgress[id] = fixedProgress
                    fixedCount++
                    
                    if (TEST_MODE) {
                        val timeLeft = (fixedProgress.nextReviewDate - now) / 1000
                        Log.d(TAG, "Fixed Level 1 word: ${progress.word} - next review in $timeLeft seconds")
                    } else {
                        Log.d(TAG, "Fixed Level 1 word: ${progress.word} - nextReviewDate set to NOW")
                    }
                }
            }
            
            if (fixedCount > 0) {
                val json = gson.toJson(allProgress)
                prefs.edit().putString(KEY_FLASHCARD_PROGRESS, json).apply()
                Log.d(TAG, "Fixed $fixedCount Level 1 words with old intervals")
            }
        }
    }
