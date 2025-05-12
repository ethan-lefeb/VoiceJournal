package com.example.voicejournal

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class NoteLogActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val gson = Gson()
    private val sharedPrefsName = "VoiceJournalPrefs"
    private val notesKey = "voice_notes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_log)

        listView = findViewById(R.id.noteListView)
        val notes = loadNotes()

        val noteStrings = notes.map {
            val tagText = if (it.tags.isEmpty()) "No tags" else it.tags.joinToString(", ")
            "🗒 ${it.text}\n🏷 Tags: $tagText\n🕒 ${java.text.DateFormat.getDateTimeInstance().format(it.timestamp)}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, noteStrings)
        listView.adapter = adapter
    }

    private fun loadNotes(): List<VoiceNote> {
        val file = File(filesDir, "journal_entries.json")
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<VoiceNote>>() {}.type
            return gson.fromJson(json, type)
        }
        return emptyList()
    }
}

