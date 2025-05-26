package com.example.voicejournal

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import android.widget.Button
import com.google.android.material.datepicker.MaterialDatePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class NoteLogActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val gson = Gson()
    private var notes: MutableList<VoiceNote> = mutableListOf()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_log)

        listView = findViewById(R.id.noteListView)
        notes = loadNotes().toMutableList()

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            notes.map { formatNote(it) }.toMutableList()
        )
        listView.adapter = adapter

        listView.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteDialog(position)
            true
        }

        val calendarButton: Button = findViewById(R.id.calendarButton)
        val clearFilterButton: Button = findViewById(R.id.clearFilterButton)

        calendarButton.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
                val filteredNotes = filterNotesByDate(notes, selectedDateMillis)
                updateListView(filteredNotes)
            }
        }

        clearFilterButton.setOnClickListener {
            updateListView(notes)
        }
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

    private fun filterNotesByDate(notes: List<VoiceNote>, selectedDate: Long): List<VoiceNote> {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 86_400_000 // Add 1 day in milliseconds

        return notes.filter {
            it.timestamp in startOfDay..endOfDay
        }
    }

    private fun updateListView(filteredNotes: List<VoiceNote>) {
        adapter.clear()
        adapter.addAll(filteredNotes.map { formatNote(it) })
        adapter.notifyDataSetChanged()
    }

    private fun saveNotes() {
        val file = File(filesDir, "journal_entries.json")
        file.writeText(gson.toJson(notes))
    }

    private fun formatNote(note: VoiceNote): String {
        val tagText = if (note.tags.isEmpty()) "No tags" else note.tags.joinToString(", ")
        return "🗒 ${note.text}\n🏷 Tags: $tagText\n🕒 ${java.text.DateFormat.getDateTimeInstance().format(note.timestamp)}"
    }

    private fun showDeleteDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")
        builder.setPositiveButton("Delete") { _, _ ->
            notes.removeAt(position)
            saveNotes()
            refreshListView()
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun refreshListView() {
        updateListView(notes)
    }
}
