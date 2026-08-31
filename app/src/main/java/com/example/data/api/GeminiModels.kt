package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    @Json(name = "topK") val topK: Int? = 40,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 4096
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?,
    @Json(name = "usageMetadata") val usageMetadata: UsageMetadata?,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?,
    val finishReason: String?,
    val index: Int?
)

@JsonClass(generateAdapter = true)
data class UsageMetadata(
    @Json(name = "promptTokenCount") val promptTokenCount: Int? = 0,
    @Json(name = "candidatesTokenCount") val candidatesTokenCount: Int? = 0,
    @Json(name = "totalTokenCount") val totalTokenCount: Int? = 0
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?
)
