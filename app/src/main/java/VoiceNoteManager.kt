package com.example.voicejournal

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class VoiceNoteManager(private val context: Context) {

    private val gson = Gson()
    private val journalFileName = "journal_entries.json"

    companion object {
        private const val TAG = "VoiceNoteManager"
    }

    fun saveJournalEntry(text: String, tags: Set<String>): Boolean {
        val voiceNote = VoiceNote(text, tags.toList(), System.currentTimeMillis())
        val file = File(context.filesDir, journalFileName)

        val existingNotes: MutableList<VoiceNote> = loadExistingNotes(file)
        existingNotes.add(voiceNote)

        return try {
            file.writeText(gson.toJson(existingNotes))
            Log.d(TAG, "Journal entry saved successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save journal entry: ${e.message}")
            false
        }
    }

    private fun loadExistingNotes(file: File): MutableList<VoiceNote> {
        return if (file.exists()) {
            try {
                val type = object : TypeToken<MutableList<VoiceNote>>() {}.type
                gson.fromJson(file.readText(), type) ?: mutableListOf()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read existing journal entries: ${e.message}")
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }

    fun getAllJournalEntries(): List<VoiceNote> {
        val file = File(context.filesDir, journalFileName)
        return loadExistingNotes(file)
    }

    fun deleteJournalEntry(timestamp: Long): Boolean {
        val file = File(context.filesDir, journalFileName)
        val existingNotes = loadExistingNotes(file).toMutableList()

        val initialSize = existingNotes.size
        existingNotes.removeAll { it.timestamp == timestamp }

        return if (existingNotes.size < initialSize) {
            try {
                file.writeText(gson.toJson(existingNotes))
                Log.d(TAG, "Journal entry deleted successfully.")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete journal entry: ${e.message}")
                false
            }
        } else {
            Log.w(TAG, "No journal entry found with timestamp: $timestamp")
            false
        }
    }

    fun updateJournalEntry(timestamp: Long, newText: String, newTags: Set<String>): Boolean {
        val file = File(context.filesDir, journalFileName)
        val existingNotes = loadExistingNotes(file).toMutableList()

        val entryIndex = existingNotes.indexOfFirst { it.timestamp == timestamp }

        return if (entryIndex != -1) {
            existingNotes[entryIndex] = VoiceNote(newText, newTags.toList(), timestamp)
            try {
                file.writeText(gson.toJson(existingNotes))
                Log.d(TAG, "Journal entry updated successfully.")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update journal entry: ${e.message}")
                false
            }
        } else {
            Log.w(TAG, "No journal entry found with timestamp: $timestamp")
            false
        }
    }

    fun getJournalEntryCount(): Int {
        return getAllJournalEntries().size
    }

    fun searchJournalEntries(query: String): List<VoiceNote> {
        return getAllJournalEntries().filter {
            it.text.contains(query, ignoreCase = true)
        }
    }

    fun getJournalEntriesByTags(tags: Set<String>): List<VoiceNote> {
        return getAllJournalEntries().filter { note ->
            note.tags.any { tag -> tags.contains(tag) }
        }
    }
}