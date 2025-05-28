package com.example.voicejournal

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var notes: List<VoiceNote>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        notes = loadNotes()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val startOfDay = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            val filteredNotes = notes.filter { note ->
                note.timestamp in startOfDay..endOfDay
            }

            println("Debug: Selected date: $year-${month+1}-$dayOfMonth")
            println("Debug: Start of day: $startOfDay")
            println("Debug: End of day: $endOfDay")
            println("Debug: Found ${filteredNotes.size} notes")
            filteredNotes.forEach { note ->
                val noteDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date(note.timestamp))
                println("Debug: Note timestamp: ${note.timestamp} ($noteDate)")
            }

            if (filteredNotes.isNotEmpty()) {
                val intent = Intent(this, NoteLogActivity::class.java)
                intent.putExtra("selectedDate", startOfDay)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "No notes for ${month + 1}/${dayOfMonth}/${year}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadNotes(): List<VoiceNote> {
        val file = File(filesDir, "journal_entries.json")
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<VoiceNote>>() {}.type
            return Gson().fromJson(json, type)
        }
        return emptyList()
    }
}