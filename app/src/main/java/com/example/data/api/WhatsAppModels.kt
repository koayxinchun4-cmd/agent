package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhatsAppTextMessage(
    val body: String
)

@JsonClass(generateAdapter = true)
data class WhatsAppSendMessageRequest(
    @Json(name = "messaging_product") val messagingProduct: String = "whatsapp",
    val to: String,
    val type: String = "text",
    val text: WhatsAppTextMessage
)

@JsonClass(generateAdapter = true)
data class WhatsAppMessageId(
    val id: String = ""
)

@JsonClass(generateAdapter = true)
data class WhatsAppContactInfo(
    @Json(name = "input") val input: String? = null,
    @Json(name = "wa_id") val waId: String? = null
)

@JsonClass(generateAdapter = true)
data class WhatsAppSendMessageResponse(
    @Json(name = "messaging_product") val messagingProduct: String = "whatsapp",
    val contacts: List<WhatsAppContactInfo>? = null,
    val messages: List<WhatsAppMessageId>? = null,
    val error: WhatsAppErrorResponse? = null
)

@JsonClass(generateAdapter = true)
data class WhatsAppErrorResponse(
    val message: String = "",
    val type: String? = null,
    val code: Int? = null,
    @Json(name = "error_subcode") val errorSubcode: Int? = null,
    @Json(name = "fbtrace_id") val fbtraceId: String? = null
)

data class WhatsAppWebhookConfig(
    val token: String = "",
    val phoneNumberId: String = "",
    val verifyToken: String = "my_custom_verify_token_123",
    val adminPhoneNumber: String = ""
)

data class WhatsAppSimulationMessage(
    val id: String,
    val sender: String, // "admin" or "gemini" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFunctionCall: Boolean = false,
    val functionName: String? = null,
    val functionArgs: String? = null,
    val functionResult: String? = null
)
