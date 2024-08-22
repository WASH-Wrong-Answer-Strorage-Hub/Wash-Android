package com.wash.washandroid.model

import com.google.gson.annotations.SerializedName

data class ChatOCRRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<ChatOCRMessage>
)

data class ChatOCRMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: List<Content>
)

data class Content(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("image_url") val imageUrl: ImageUrl? = null
)

data class ImageUrl(
    @SerializedName("url") val url: String
)

data class ChatOCRResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("message") val message: ResponseChatOCRMessage
)

data class ResponseChatOCRMessage(
    @SerializedName("content") val content: String
)
