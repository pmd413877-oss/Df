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

class SpeechRecognizerManager(private val context: Context) {

    sealed class SpeechState {
        object Idle : SpeechState()
        object Ready : SpeechState()
        data class Listening(val partialText: String = "", val amplitude: Float = 0f) : SpeechState()
        data class Result(val text: String) : SpeechState()
        data class Error(val message: String, val isPermanent: Boolean = false, val isNetwork: Boolean = false) : SpeechState()
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
            _speechState.value = SpeechState.Error(
                message = "ডিভাইসে স্পিচ রিকগনিশন সার্ভিস প্রস্তুত নেই। গুগল স্পিচ সার্ভিস আপডেট করুন।",
                isPermanent = true
            )
            return
        }

        if (isListening) {
            cancel()
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        _speechState.value = SpeechState.Ready
                    }

                    override fun onBeginningOfSpeech() {
                        _speechState.value = SpeechState.Listening(amplitude = 0.25f)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1.0f)
                        val currentText = (_speechState.value as? SpeechState.Listening)?.partialText ?: ""
                        _speechState.value = SpeechState.Listening(partialText = currentText, amplitude = normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        when (error) {
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                                _speechState.value = SpeechState.Error(
                                    message = "ইন্টারনেট সংযোগে একটু সমস্যা হচ্ছে, দয়া করে আপনার নেটওয়ার্ক কানেকশনটি একবার দেখে নিন।",
                                    isNetwork = true
                                )
                            }
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                _speechState.value = SpeechState.Error(
                                    message = "মাইক্রোফোন পারমিশন প্রয়োজন। জারার সাথে কথা বলতে পারমিশন এলাউ করুন।",
                                    isPermanent = true
                                )
                            }
                            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                _speechState.value = SpeechState.Error(
                                    message = "কথাটি স্পষ্ট শুনতে পাইনি, মাইক্রোফোনে ট্যাপ করে আরেকবার বলুন।"
                                )
                            }
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                cancel()
                                _speechState.value = SpeechState.Idle
                            }
                            SpeechRecognizer.ERROR_AUDIO -> {
                                _speechState.value = SpeechState.Error("অডিও রেকর্ডিংয়ে সাময়িক সমস্যা হয়েছে, পুনরায় চেষ্টা করুন।")
                            }
                            else -> {
                                _speechState.value = SpeechState.Error("কথা বুঝতে সমস্যা হয়েছে, অনুগ্রহ করে আবার বলুন।")
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognizedText = matches?.firstOrNull()?.trim() ?: ""
                        if (recognizedText.isNotBlank()) {
                            _speechState.value = SpeechState.Result(recognizedText)
                        } else {
                            _speechState.value = SpeechState.Error("কথাটি বুঝতে পারিনি, আরেকবার বলুন।")
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
                if (languageCode == "bn") {
                    putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("bn-BD", "bn-IN", "bn"))
                }
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            _speechState.value = SpeechState.Error("ভয়েস ইনপুট চালু করতে সমস্যা হয়েছে, আবার চেষ্টা করুন।")
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    fun cancel() {
        isListening = false
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            // Ignored
        }
        _speechState.value = SpeechState.Idle
    }

    fun reset() {
        cancel()
    }

    fun destroy() {
        isListening = false
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignored
        }
        speechRecognizer = null
        _speechState.value = SpeechState.Idle
    }
}
