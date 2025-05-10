package com.example.voicejournal

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NoteLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_log)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Placeholder notes
        val sampleNotes = listOf(
            VoiceNote("2025-05-01", "Project"),
            VoiceNote("2025-05-03", "Reminder"),
            VoiceNote("2025-05-05", "Idea Dump")
        )

        val adapter = NoteAdapter(sampleNotes)
        recyclerView.adapter = adapter
    }
}