package com.example.voicejournal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var recordButton: Button
    private lateinit var transcriptionTextView: TextView
    private lateinit var tagsTextView: TextView

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var recognizerIntent: Intent
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private var isListening = false
    private var isButtonClickable = true
    private var lastTranscription: String = ""
    private var lastPartial: String = ""
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "VoiceJournalApp"
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val BUTTON_DEBOUNCE_DELAY = 500L
        private const val LONG_DELAY_BEFORE_LISTENING = 1000L
    }

    private val gson = Gson()
    private val journalFileName = "journal_entries.json"

    private val keywordMap = mapOf(
        "note to self" to "Personal",
        "schedule" to "Schedule",
        "deadline" to "Deadline",
        "phineas" to "Cats",
    )

    private fun detectTags(text: String): Set<String> {
        return keywordMap.filter { (phrase, _) ->
            text.contains(phrase, ignoreCase = true)
        }.values.toSet()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        statusTextView = findViewById(R.id.statusTextView)
        recordButton = findViewById(R.id.recordButton)
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        tagsTextView = findViewById(R.id.tagsTextView)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        val menuButton = findViewById<Button>(R.id.menuButton)
        menuButton.setOnClickListener {
            val intent = Intent(this, NoteLogActivity::class.java)
            startActivity(intent)
        }


        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusTextView.text = "Speech recognition not available."
            recordButton.isEnabled = false
            return
        }

        setupRecognitionIntent()

        recordButton.setOnClickListener {
            if (isButtonClickable) {
                debounceButton()
                toggleListening()
            }
        }

        val fakeNoteButton = findViewById<Button>(R.id.fakeNoteButton)
        fakeNoteButton.setOnClickListener {
            val fakeText = "Note to self: check Phineas' schedule and meet the deadline"
            val fakeTags = detectTags(fakeText)
            transcriptionTextView.text = fakeText
            tagsTextView.text = if (fakeTags.isEmpty()) "Tags: None" else "Tags: ${fakeTags.joinToString(", ")}"
            statusTextView.text = "Fake note created."
            saveJournalEntry(fakeText, fakeTags)
        }

        checkAndRequestPermission()
    }

    private fun setupRecognitionIntent() {
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
    }

    private fun prepareRecognizer() {
        cleanupRecognizer()
        statusTextView.text = "Preparing microphone..."

        handler.postDelayed({
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                statusTextView.text = "Ready"
                Log.d(TAG, "Speech recognizer created")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating recognizer: ${e.message}")
                Toast.makeText(this, "Error initializing recognizer", Toast.LENGTH_SHORT).show()
                statusTextView.text = "Error."
            }
        }, 500)
    }

    private fun cleanupRecognizer() {
        speechRecognizer?.let {
            try {
                it.cancel()
                it.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up recognizer: ${e.message}")
            }
        }
    }

    private fun toggleListening() {
        if (isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (isListening) return

        lastPartial = ""
        prepareRecognizer()

        handler.postDelayed({
            try {
                requestAudioFocus()
                speechRecognizer?.startListening(recognizerIntent)
                isListening = true
                recordButton.text = "Stop Listening"
                statusTextView.text = "Listening..."
            } catch (e: Exception) {
                Log.e(TAG, "Start listening failed: ${e.message}")
                statusTextView.text = "Error: ${e.message}"
            }
        }, LONG_DELAY_BEFORE_LISTENING)
    }

    private fun stopListening() {
        if (!isListening) return

        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listening: ${e.message}")
        } finally {
            isListening = false
            recordButton.text = "Start Listening"
            statusTextView.text = "Stopped"
            releaseAudioFocus()
        }
    }

    private fun debounceButton() {
        isButtonClickable = false
        handler.postDelayed({ isButtonClickable = true }, BUTTON_DEBOUNCE_DELAY)
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                statusTextView.text = "Listening..."
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray) {}

            override fun onEndOfSpeech() {
                statusTextView.text = "Processing..."
            }

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No input detected"
                    else -> "Recognition error ($error)"
                }
                statusTextView.text = message
                isListening = false
                recordButton.text = "Start Listening"
                releaseAudioFocus()
                cleanupRecognizer()
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.joinToString(" ") ?: ""

                if (transcript == lastPartial) {
                    Log.d(TAG, "Skipping save: final result same as partial")
                    return
                }

                lastTranscription = transcript
                transcriptionTextView.text = transcript

                val tags = detectTags(transcript)
                tagsTextView.text = if (tags.isEmpty()) "Tags: None" else "Tags: ${tags.joinToString(", ")}"

                statusTextView.text = "Done"
                isListening = false
                recordButton.text = "Start Listening"
                releaseAudioFocus()

                saveJournalEntry(transcript, tags)
            }

            override fun onPartialResults(partials: Bundle) {
                partials.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    lastPartial = it
                    transcriptionTextView.text = it
                }
            }

            override fun onEvent(eventType: Int, params: Bundle) {}
        }
    }

    private fun saveJournalEntry(text: String, tags: Set<String>) {
        val voiceNote = VoiceNote(text, tags.toList(), System.currentTimeMillis())
        val file = File(filesDir, journalFileName)

        val existingNotes: MutableList<VoiceNote> = if (file.exists()) {
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

        existingNotes.add(voiceNote)

        try {
            file.writeText(gson.toJson(existingNotes))
            Log.d(TAG, "Journal entry saved.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save journal entry: ${e.message}")
        }
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            prepareRecognizer()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prepareRecognizer()
            } else {
                statusTextView.text = "Permission denied."
                recordButton.isEnabled = false
            }
        }
    }



    override fun onPause() {
        super.onPause()
        stopListening()
        cleanupRecognizer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        releaseAudioFocus()
        cleanupRecognizer()
    }
}
