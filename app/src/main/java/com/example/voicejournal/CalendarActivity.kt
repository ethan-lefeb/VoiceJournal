package com.example.voicejournal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.io.File
import java.util.Calendar



class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var notes: List<VoiceNote>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        notes = loadNotes()

        val noteDates = notes.map {
            val cal = CalendarDay.from(
                Calendar.getInstance().apply {
                    timeInMillis = it.timestamp
                }
            )
            cal
        }

        calendarView.addDecorator(NoteDayDecorator(noteDates.toSet()))

        calendarView.setOnDateChangedListener { _, date, _ ->
            val millis = Calendar.getInstance().apply {
                set(date.year, date.month - 1, date.day)
            }.timeInMillis
            val filteredNotes = notes.filter {
                val dayStart = millis
                val dayEnd = dayStart + 86_400_000
                it.timestamp in dayStart..dayEnd
            }

            if (filteredNotes.isNotEmpty()) {
                val intent = Intent(this, NoteLogActivity::class.java)
                intent.putExtra("selectedDate", millis)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No notes for this date (this functionality is not complete yet!)", Toast.LENGTH_SHORT).show()
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

