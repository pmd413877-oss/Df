package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequest(
    @field:Json(name = "contents") val contents: List<GeminiContent>,
    @field:Json(name = "systemInstruction") val systemInstruction: GeminiSystemInstruction? = null,
    @field:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @field:Json(name = "role") val role: String? = null,
    @field:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiSystemInstruction(
    @field:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @field:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @field:Json(name = "temperature") val temperature: Double = 0.7,
    @field:Json(name = "maxOutputTokens") val maxOutputTokens: Int = 1000
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponse(
    @field:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @field:Json(name = "promptFeedback") val promptFeedback: GeminiPromptFeedback? = null,
    @field:Json(name = "error") val error: GeminiApiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @field:Json(name = "content") val content: GeminiContent? = null,
    @field:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPromptFeedback(
    @field:Json(name = "blockReason") val blockReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiError(
    @field:Json(name = "code") val code: Int? = null,
    @field:Json(name = "message") val message: String? = null,
    @field:Json(name = "status") val status: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiListModelsResponse(
    @field:Json(name = "models") val models: List<GeminiModelInfo>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiModelInfo(
    @field:Json(name = "name") val name: String,
    @field:Json(name = "displayName") val displayName: String? = null,
    @field:Json(name = "supportedGenerationMethods") val supportedGenerationMethods: List<String>? = null
)

sealed class GeminiResult {
    data class Success(val text: String, val modelUsed: String? = null) : GeminiResult()
    data class Error(val message: String, val isAuthError: Boolean = false, val isQuotaError: Boolean = false) : GeminiResult()
}
