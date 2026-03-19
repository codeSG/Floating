package com.example.floating
data class TranslateRequest(
    val text: String
)

data class TranslateResponse(
    val source_language: String,
    val target_language: String,
    val translation: String
)