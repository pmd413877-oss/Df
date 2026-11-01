package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerManager(private val context: Context) {

    sealed class SpeechState {
        object Idle : SpeechState()
        object Ready : SpeechState()
        data class Listening(val partialText: String = "", val amplitude: Float = 0f) : SpeechState()
        data class Result(val text: String) : SpeechState()
        data class Error(val message: String, val isPermanent: Boolean = false) : SpeechState()
    }

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(languageCode: String = "bn") {
        if (!isSpeechRecognitionAvailable()) {
            _speechState.value = SpeechState.Error("Speech recognition service is not available on this device.")
            return
        }

        if (isListening) {
            cancel()
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        _speechState.value = SpeechState.Ready
                    }

                    override fun onBeginningOfSpeech() {
                        _speechState.value = SpeechState.Listening(amplitude = 0.2f)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize rmsdB (typically -2 to 10 dB) to 0.0 .. 1.0
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                        val currentText = (_speechState.value as? SpeechState.Listening)?.partialText ?: ""
                        _speechState.value = SpeechState.Listening(partialText = currentText, amplitude = normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error (অডিও রেকর্ডিংয়ে ত্রুটি হয়েছে)"
                            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error (স্পিচ সার্ভিসে সমস্যা)"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required (মাইক্রোফোন পারমিশন প্রয়োজন)"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition (ইন্টারনেট সংযোগ চেক করুন)"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout while recognizing speech (ইন্টারনেট টাইমআউট হয়েছে)"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again (কথা বোঝা যায়নি, আবার বলুন)"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy (মাইক্রোফোন প্রস্তুত হচ্ছে...)"
                            SpeechRecognizer.ERROR_SERVER -> "Server error from speech recognition (সার্ভার সমস্যা)"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Please tap and speak (কোনো কথা শোনা যায়নি, ট্যাপ করে বলুন)"
                            else -> "Speech recognition error ($error)"
                        }
                        _speechState.value = SpeechState.Error(
                            message = message,
                            isPermanent = (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognizedText = matches?.firstOrNull()?.trim() ?: ""
                        if (recognizedText.isNotBlank()) {
                            _speechState.value = SpeechState.Result(recognizedText)
                        } else {
                            _speechState.value = SpeechState.Error("Could not recognize any words.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim() ?: ""
                        if (partial.isNotBlank()) {
                            _speechState.value = SpeechState.Listening(partialText = partial, amplitude = 0.5f)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                val localeTag = when (languageCode) {
                    "bn" -> "bn-BD"
                    "hi" -> "hi-IN"
                    "ar" -> "ar-SA"
                    "es" -> "es-ES"
                    else -> "en-US"
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            _speechState.value = SpeechState.Error("Failed to start speech recognition: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            speechRecognizer?.stopListening()
        }
    }

    fun cancel() {
        isListening = false
        speechRecognizer?.cancel()
        _speechState.value = SpeechState.Idle
    }

    fun reset() {
        cancel()
        _speechState.value = SpeechState.Idle
    }

    fun destroy() {
        isListening = false
        speechRecognizer?.destroy()
        speechRecognizer = null
        _speechState.value = SpeechState.Idle
    }
}
