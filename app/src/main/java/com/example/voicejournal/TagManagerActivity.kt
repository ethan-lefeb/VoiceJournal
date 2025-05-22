package com.example.voicejournal

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.example.voicejournal.TagUtils


class TagManagerActivity : AppCompatActivity() {

    private lateinit var tagInput: EditText
    private lateinit var addButton: Button
    private lateinit var tagList: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_manager)

        sharedPreferences = getSharedPreferences("VoiceJournalPrefs", Context.MODE_PRIVATE)

        tagInput = findViewById(R.id.tag_input)
        addButton = findViewById(R.id.add_tag_button)
        tagList = findViewById(R.id.tag_list)

        addButton.setOnClickListener {
            val newTag = tagInput.text.toString().trim()
            if (newTag.isNotEmpty()) {
                val tags = getSavedTags().toMutableSet()
                if (tags.add(newTag)) {
                    saveTags(tags)
                    renderTags(tags)
                    tagInput.text.clear()
                } else {
                    Toast.makeText(this, "Tag already exists.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        renderTags(getSavedTags())
    }

    override fun onResume() {
        super.onResume()
    }

    private fun renderTags(tags: Set<String>) {
        tagList.removeAllViews()
        tags.sorted().forEach { tag ->
            val tagView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
            }

            val tagText = TextView(this).apply {
                text = tag
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            }

            val deleteButton = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    val currentTags = TagUtils.getCustomTags(this@TagManagerActivity).toMutableSet()
                    currentTags.remove(tag)
                    TagUtils.saveCustomTags(this@TagManagerActivity, currentTags)
                    renderTags(currentTags)
                }
            }

            tagView.addView(tagText)
            tagView.addView(deleteButton)
            tagList.addView(tagView)
        }
    }


    private fun getSavedTags(): Set<String> {
        return sharedPreferences.getStringSet("custom_tags", emptySet()) ?: emptySet()
    }

    private fun saveTags(tags: Set<String>) {
        sharedPreferences.edit().putStringSet("custom_tags", tags).apply()
    }
}