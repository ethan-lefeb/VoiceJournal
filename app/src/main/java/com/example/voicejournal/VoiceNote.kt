package com.example.voicejournal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



data class VoiceNote(
    val date: String,
    val tag: Set<String>,
    val timestamp: Long = System.currentTimeMillis()
)