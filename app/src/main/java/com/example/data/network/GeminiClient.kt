package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Resolves the active API key: user-configured key takes precedence,
     * otherwise falls back to BuildConfig.GEMINI_API_KEY if present and valid.
     */
    fun resolveApiKey(userKey: String): String {
        if (userKey.isNotBlank()) return userKey.trim()

        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildConfigKey = field.get(null) as? String ?: ""
            if (buildConfigKey.isNotBlank() && !buildConfigKey.contains("MY_GEMINI_API_KEY")) {
                buildConfigKey.trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun sanitizeModel(model: String?): String {
        val trimmed = model?.trim() ?: ""
        return when {
            trimmed.isBlank() || trimmed == "gemini-2.5-flash" -> "gemini-1.5-flash"
            trimmed.startsWith("models/") -> trimmed.removePrefix("models/")
            else -> trimmed
        }
    }

    /**
     * Tests an API key with a real minimal generateContent request.
     */
    suspend fun testConnection(apiKey: String, model: String = "gemini-1.5-flash"): GeminiResult {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return GeminiResult.Error("API key cannot be empty (API কী খালি হতে পারে না)", isAuthError = true)
        }

        val sanitizedModel = sanitizeModel(model)
        val testRequest = GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello! Answer with only the word OK."))
                )
            ),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 10)
        )

        return try {
            var response = apiService.generateContent(
                model = sanitizedModel,
                apiKey = trimmedKey,
                request = testRequest
            )

            // Auto-recovery if model returned 404
            if (response.code() == 404 && sanitizedModel != "gemini-1.5-flash") {
                response = apiService.generateContent(
                    model = "gemini-1.5-flash",
                    apiKey = trimmedKey,
                    request = testRequest
                )
            }

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    GeminiResult.Success("সফলভাবে সংযোগ হয়েছে! Gemini উত্তর দিয়েছে: ${text.trim()}")
                } else {
                    GeminiResult.Success("সফলভাবে সংযোগ হয়েছে! API Status 200 OK.")
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: ""
                when (errorCode) {
                    400 -> GeminiResult.Error("Bad Request (400): অনুগ্রহ করে মডেল ও সেটিংস পরীক্ষা করুন। Details: $errorBody")
                    401, 403 -> GeminiResult.Error("API কী ভুল বা অননুমোদিত ($errorCode)। অনুগ্রহ করে সঠিক Gemini API Key দিন।", isAuthError = true)
                    404 -> GeminiResult.Error("Gemini মডেলটি পাওয়া যায়নি ($errorCode)।")
                    429 -> GeminiResult.Error("API কোটা শেষ বা রেট লিমিট অতিক্রম করেছে ($errorCode)। কিছুক্ষণ পর চেষ্টা করুন।", isQuotaError = true)
                    else -> GeminiResult.Error("API Error ($errorCode): $errorBody")
                }
            }
        } catch (e: IOException) {
            GeminiResult.Error("Network error: ইন্টারনেট সংযোগ চেক করুন। Google AI সার্ভারের সাথে যোগাযোগ করা যাচ্ছে না।")
        } catch (e: Exception) {
            GeminiResult.Error("Unexpected error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Sends a user query to Gemini and returns the assistant's reply.
     */
    suspend fun askGemini(
        question: String,
        userApiKey: String,
        languageCode: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        model: String = "gemini-1.5-flash"
    ): GeminiResult {
        val activeKey = resolveApiKey(userApiKey)
        if (activeKey.isBlank()) {
            return GeminiResult.Error(
                message = "Gemini API key পাওয়া যায়নি। অনুগ্রহ করে অ্যাপের সেটিংস থেকে আপনার Google AI Studio API key দিন।",
                isAuthError = true
            )
        }

        val sanitizedModel = sanitizeModel(model)

        val languagePrompt = when (languageCode) {
            "bn" -> "You are 'Zara AI', an advanced, futuristic voice-powered personal assistant running natively on the user's Android device. Your primary language is Bengali (বাংলা). You are intelligent, loyal, highly efficient, and polite. You can control apps, search YouTube and the web, screen and answer incoming calls on user's behalf, and manage messages. Respond conversationally, concisely, and warmly in clear, natural Bengali. Avoid long essay paragraphs; write clear sentences optimized for spoken audio delivery."
            "hi" -> "You are 'Zara AI', an advanced, voice-powered personal assistant on Android. Respond in natural, conversational Hindi (हिन्दी). Keep answers concise, polite, and optimized for spoken delivery."
            "ar" -> "You are 'Zara AI', an advanced, voice-powered personal assistant on Android. Respond in natural, conversational Arabic. Keep answers concise and easy to listen to."
            "es" -> "You are 'Zara AI', an advanced, voice-powered personal assistant on Android. Respond in natural, conversational Spanish. Keep answers concise and easy to listen to."
            else -> "You are 'Zara AI', an advanced, futuristic voice-powered personal assistant running natively on the user's Android phone. You are intelligent, loyal, and efficient. You can manage apps, automate YouTube/web searches, screen calls on behalf of the user, and read messages. Respond conversationally, clearly, and concisely in engaging English, optimized for spoken Text-to-Speech audio."
        }

        // Build conversation turns from recent history
        val contentsList = mutableListOf<GeminiContent>()
        recentHistory.takeLast(4).forEach { (pastQ, pastA) ->
            contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = pastQ))))
            contentsList.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = pastA))))
        }
        contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = question))))

        val request = GeminiGenerateContentRequest(
            contents = contentsList,
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = languagePrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 800
            )
        )

        return try {
            var response = apiService.generateContent(
                model = sanitizedModel,
                apiKey = activeKey,
                request = request
            )

            // Auto-recovery if initial model returned 404
            if (response.code() == 404 && sanitizedModel != "gemini-1.5-flash") {
                response = apiService.generateContent(
                    model = "gemini-1.5-flash",
                    apiKey = activeKey,
                    request = request
                )
            }

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    GeminiResult.Success(text.trim())
                } else {
                    val blockReason = body?.promptFeedback?.blockReason
                    if (blockReason != null) {
                        GeminiResult.Error("রেসপন্স ব্লক করা হয়েছে: $blockReason")
                    } else {
                        GeminiResult.Error("Gemini থেকে খালি উত্তর এসেছে। অনুগ্রহ করে প্রশ্নটি আবার করুন।")
                    }
                }
            } else {
                val code = response.code()
                when (code) {
                    401, 403 -> GeminiResult.Error("Gemini API key টি সঠিক নয় বা মেয়াদোত্তীর্ণ ($code)। সেটিংস থেকে নতুন কী দিন।", isAuthError = true)
                    429 -> GeminiResult.Error("Gemini API ব্যবহারের কোটা শেষ ($code)। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।", isQuotaError = true)
                    404 -> GeminiResult.Error("Gemini মডেল পাওয়া যায়নি ($code)। মডেল সেটিংস চেক করুন।")
                    else -> GeminiResult.Error("API request error ($code). অনুগ্রহ করে ইন্টারনেট ও সেটিংস চেক করুন।")
                }
            }
        } catch (e: IOException) {
            GeminiResult.Error("ইন্টারনেট সংযোগ পাওয়া যাচ্ছে না। ইন্টারনেট কানেকশন চেক করে আবার চেষ্টা করুন।")
        } catch (e: Exception) {
            GeminiResult.Error("সার্ভিস ত্রুটি: ${e.localizedMessage ?: "Unexpected error"}")
        }
    }
}
