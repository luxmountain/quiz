package com.uilover.project247.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Manager class để xử lý Text-to-Speech cho việc phát âm từ vựng tiếng Anh
 * 
 * Cách sử dụng:
 * 1. Khởi tạo trong Activity/ViewModel:
 *    val ttsManager = TextToSpeechManager(context)
 * 
 * 2. Phát âm một từ:
 *    ttsManager.speak("breakfast")
 * 
 * 3. Dọn dẹp khi không dùng nữa:
 *    ttsManager.shutdown()
 */
class TextToSpeechManager(
    context: Context,
    private val onInitSuccess: (() -> Unit)? = null,
    private val onInitFailure: (() -> Unit)? = null
) {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    
    companion object {
        private const val TAG = "TextToSpeechManager"
        const val SPEECH_RATE_SLOW = 0.7f
        const val SPEECH_RATE_NORMAL = 1.0f
        const val SPEECH_RATE_FAST = 1.3f
        const val PITCH_LOW = 0.8f
        const val PITCH_NORMAL = 1.0f
        const val PITCH_HIGH = 1.2f
    }
    
    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "English language is not supported")
                    isInitialized = false
                    onInitFailure?.invoke()
                } else {
                    isInitialized = true
                    // Thiết lập tốc độ và cao độ giọng nói mặc định
                    textToSpeech?.setSpeechRate(SPEECH_RATE_NORMAL)
                    textToSpeech?.setPitch(PITCH_NORMAL)
                    Log.d(TAG, "TextToSpeech initialized successfully")
                    onInitSuccess?.invoke()
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
                isInitialized = false
                onInitFailure?.invoke()
            }
        }
        
        // Thiết lập listener để theo dõi trạng thái phát âm
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "Started speaking: $utteranceId")
            }
            
            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "Finished speaking: $utteranceId")
            }
            
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error speaking: $utteranceId")
            }
        })
    }
    
    /**
     * Phát âm một từ hoặc câu bằng tiếng Anh
     * @param text Text cần phát âm
     * @param queueMode QUEUE_FLUSH (xóa hàng đợi) hoặc QUEUE_ADD (thêm vào hàng đợi)
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        if (!isInitialized) {
            Log.w(TAG, "TextToSpeech is not initialized yet")
            return
        }
        
        if (text.isBlank()) {
            Log.w(TAG, "Cannot speak empty text")
            return
        }
        
        textToSpeech?.speak(text, queueMode, null, text)
    }
    
    /**
     * Phát âm với callback khi hoàn thành
     */
    fun speakWithCallback(
        text: String,
        onComplete: () -> Unit
    ) {
        if (!isInitialized) {
            Log.w(TAG, "TextToSpeech is not initialized yet")
            return
        }
        
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            
            override fun onDone(utteranceId: String?) {
                if (utteranceId == text) {
                    onComplete()
                }
            }
            
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error speaking: $utteranceId")
            }
        })
        
        speak(text)
    }
    
    /**
     * Thiết lập tốc độ đọc
     * @param rate Tốc độ (0.5 - 2.0). Mặc định là 1.0
     */
    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }
    
    /**
     * Thiết lập cao độ giọng nói
     * @param pitch Cao độ (0.5 - 2.0). Mặc định là 1.0
     */
    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }
    
    /**
     * Dừng phát âm hiện tại
     */
    fun stop() {
        textToSpeech?.stop()
    }
    
    /**
     * Kiểm tra xem TTS có đang phát âm không
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    /**
     * Kiểm tra xem TTS đã khởi tạo thành công chưa
     */
    fun isReady(): Boolean {
        return isInitialized
    }
    
    /**
     * Dọn dẹp tài nguyên - GỌI HÀM NÀY TRONG onDestroy() của Activity
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        isInitialized = false
        Log.d(TAG, "TextToSpeech shutdown")
    }
}
