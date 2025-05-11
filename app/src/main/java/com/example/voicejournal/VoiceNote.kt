package com.example.voicejournal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



data class VoiceNote(
    val text: String,
    val tags: List<String>,
    val timestamp: Long
)
