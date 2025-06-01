package com.example.voicejournal

import android.content.Context
import android.content.SharedPreferences

object TagUtils {

    private const val PREFS_NAME = "VoiceJournalPrefs"
    private const val TAG_MAP_KEY = "custom_tags"
    private const val KEYWORD_MAP_KEY = "keyword_mappings"

    val defaultKeywordMap = mapOf(
        "note to self" to "Personal",
        "schedule" to "Schedule",
        "deadline" to "Deadline",
        "phineas" to "Cats",
        "meeting" to "Work",
        "appointment" to "Schedule",
        "reminder" to "Personal",
        "work" to "Work",
        "family" to "Family",
        "health" to "Health",
        "exercise" to "Health",
        "grocery" to "Shopping",
        "shopping" to "Shopping"
    )

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCustomTags(context: Context): Set<String> {
        return getSharedPreferences(context).getStringSet(TAG_MAP_KEY, emptySet()) ?: emptySet()
    }

    fun saveCustomTags(context: Context, tags: Set<String>) {
        getSharedPreferences(context).edit().putStringSet(TAG_MAP_KEY, tags).apply()
    }
    fun getCustomKeywordMappings(context: Context): Map<String, String> {
        val prefs = getSharedPreferences(context)
        val mappingsJson = prefs.getString(KEYWORD_MAP_KEY, "{}")
        return emptyMap()
    }
    fun saveCustomKeywordMappings(context: Context, mappings: Map<String, String>) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(KEYWORD_MAP_KEY, "{}").apply()
    }
    fun getKeywordMap(context: Context): Map<String, String> {
        val customMappings = getCustomKeywordMappings(context)
        return defaultKeywordMap + customMappings
    }
    fun getAllTags(context: Context): Set<String> {
        val defaultTags = defaultKeywordMap.values.toSet()
        val customTags = getCustomTags(context)
        return defaultTags + customTags
    }
}