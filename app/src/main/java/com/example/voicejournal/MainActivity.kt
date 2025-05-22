package com.example.voicejournal

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var transcriptionTextView: TextView
    private lateinit var saveButton: Button

    private var lastPartial: String = ""
    private var lastSavedTranscript: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        transcriptionTextView = findViewById(R.id.transcriptionTextView)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val transcript = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
                val cleanedTranscript = transcript.trim().lowercase()
                val cleanedPartial = lastPartial.trim().lowercase()

                if (cleanedTranscript == cleanedPartial || cleanedTranscript == lastSavedTranscript) {
                    Log.d("VoiceJournal", "Skipping duplicate transcript: $cleanedTranscript")
                    return
                }

                transcriptionTextView.text = transcript
                saveTranscription(transcript)
                lastPartial = ""
                lastSavedTranscript = cleanedTranscript
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    lastPartial = it
                    // Optional: show temporary UI feedback if needed
                    Log.d("VoiceJournal", "Partial result: $it")
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        saveButton.setOnClickListener {
            val transcript = transcriptionTextView.text.toString()
            if (transcript.isNotBlank()) {
                saveTranscription(transcript)
            }
        }

        startListening()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }

    private fun saveTranscription(transcript: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "$timestamp: $transcript\n"
        openFileOutput("transcriptions.txt", MODE_APPEND).use {
            it.write(entry.toByteArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
