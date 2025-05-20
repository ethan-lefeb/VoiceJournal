package com.example.voicejournal

import android.content.Context
import android.content.SharedPreferences

object TagUtils {

    private const val PREFS_NAME = "VoiceJournalPrefs"
    private const val TAG_MAP_KEY = "custom_tags"

    val defaultKeywordMap = mapOf(
        "note to self" to "Personal",
        "schedule" to "Schedule",
        "deadline" to "Deadline",
        "phineas" to "Cats",
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

    fun getKeywordMap(context: Context): Map<String, String> {
        val tags = getCustomTags(context)
        return tags.associateWith { it }  // Keyword → Tag

        fun getAllTags(context: Context): Set<String> {
            return defaultKeywordMap.values.toSet() + getCustomTags(context)
        }
    }
}
