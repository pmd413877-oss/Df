package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices: StateFlow<List<Voice>> = _availableVoices.asStateFlow()

    private var currentRate: Float = 0.98f
    private var currentPitch: Float = 1.0f
    private var currentLanguageCode: String = "bn"

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                _ttsReady.value = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })

                // Configure language, rate and pitch
                setLanguage(currentLanguageCode)
                setSpeechRate(currentRate)
                setSpeechPitch(currentPitch)

                // Load available voices
                try {
                    val voices = tts?.voices?.toList() ?: emptyList()
                    _availableVoices.value = voices

                    // Auto-select best Bengali voice if language is Bengali
                    if (currentLanguageCode == "bn") {
                        autoSelectBestBengaliVoice(voices)
                    }
                } catch (e: Exception) {
                    // Ignored on devices where voices are restricted
                }

                // If there was any pending speech request, execute it now
                pendingSpeechText?.let { text ->
                    pendingSpeechText = null
                    speak(text)
                }
            } else {
                isInitialized = false
                _ttsReady.value = false
            }
        }
    }

    private fun autoSelectBestBengaliVoice(voices: List<Voice>) {
        try {
            val bengaliVoices = voices.filter { voice ->
                voice.locale.language.equals("bn", ignoreCase = true)
            }
            if (bengaliVoices.isNotEmpty()) {
                // Prefer high-quality female or natural Google Bengali voice if present
                val preferred = bengaliVoices.firstOrNull {
                    it.name.contains("female", ignoreCase = true) ||
                            it.name.contains("bd", ignoreCase = true) ||
                            it.quality >= Voice.QUALITY_HIGH
                } ?: bengaliVoices.first()

                tts?.voice = preferred
            }
        } catch (e: Exception) {
            // Fallback to default
        }
    }

    fun setSpeechRate(rate: Float) {
        currentRate = rate.coerceIn(0.6f, 1.8f)
        tts?.setSpeechRate(currentRate)
    }

    fun setSpeechPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.6f, 1.6f)
        tts?.setPitch(currentPitch)
    }

    fun setLanguage(languageCode: String) {
        currentLanguageCode = languageCode
        val locale = when (languageCode) {
            "bn" -> {
                // Try Bangladesh Bengali first, then India Bengali
                val bdLocale = Locale.forLanguageTag("bn-BD")
                val supportCheck = tts?.isLanguageAvailable(bdLocale)
                if (supportCheck != null && supportCheck >= TextToSpeech.LANG_AVAILABLE) {
                    bdLocale
                } else {
                    Locale.forLanguageTag("bn-IN")
                }
            }
            "hi" -> Locale.forLanguageTag("hi-IN")
            "ar" -> Locale.forLanguageTag("ar-SA")
            "es" -> Locale.forLanguageTag("es-ES")
            else -> Locale.US
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            if (languageCode == "bn") {
                // Try generic bn locale
                tts?.setLanguage(Locale.forLanguageTag("bn"))
            } else {
                tts?.setLanguage(Locale.US)
            }
        }

        // Refresh voice preference
        try {
            val voices = tts?.voices?.toList() ?: emptyList()
            if (languageCode == "bn" && voices.isNotEmpty()) {
                autoSelectBestBengaliVoice(voices)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun setVoiceByName(voiceName: String) {
        if (voiceName.isBlank()) return
        try {
            val matchingVoice = tts?.voices?.firstOrNull { it.name == voiceName }
            if (matchingVoice != null) {
                tts?.voice = matchingVoice
            }
        } catch (e: Exception) {
            // Voice not found or not supported
        }
    }

    /**
     * Cleans text to produce natural, human-sounding voice output:
     * - Strips markdown asterisks, hashtags, bullet dashes, backticks
     * - Strips emojis so TTS doesn't awkwardly say symbols
     * - Replaces complex punctuation with natural breathing pauses
     */
    fun cleanTextForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        return rawText
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), "কোড বাদ দেওয়া হয়েছে।")
            .replace(Regex("`[^`]+`")) { it.value.replace("`", "") }
            // Remove markdown bold/italics
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            // Remove markdown headers
            .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
            // Remove markdown bullet points (- or * at start of lines)
            .replace(Regex("^[\\-*•]\\s+", RegexOption.MULTILINE), "")
            // Remove numbering like "1. ", "2. "
            .replace(Regex("^\\d+\\.\\s+", RegexOption.MULTILINE), "")
            // Remove emojis and special non-spoken symbols
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
            // Remove web links
            .replace(Regex("https?://\\S+"), "লিংক")
            // Clean extra spaces
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{2,}"), "\n")
            .trim()
    }

    fun speak(text: String, utteranceId: String = "zara_response_${System.currentTimeMillis()}") {
        val cleaned = cleanTextForSpeech(text)
        if (cleaned.isBlank()) return

        if (!isInitialized) {
            pendingSpeechText = cleaned
            return
        }

        stop()
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignored
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
