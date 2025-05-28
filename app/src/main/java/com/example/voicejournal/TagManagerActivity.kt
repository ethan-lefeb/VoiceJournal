package com.example.voicejournal

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import org.json.JSONObject
import com.example.voicejournal.TagUtils


class TagManagerActivity : AppCompatActivity() {

    private lateinit var tagInput: EditText
    private lateinit var addButton: Button
    private lateinit var tagList: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var triggerInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_manager)

        sharedPreferences = getSharedPreferences("VoiceJournalPrefs", Context.MODE_PRIVATE)

        tagInput = findViewById(R.id.tag_input)
        triggerInput = findViewById(R.id.trigger_input)
        addButton = findViewById(R.id.add_tag_button)
        tagList = findViewById(R.id.tag_list)

        addButton.setOnClickListener {
            val newTag = tagInput.text.toString().trim()
            val triggerPhrase = triggerInput.text.toString().trim()
            if (newTag.isNotEmpty() && triggerPhrase.isNotEmpty()) {
                val tagMap = getTagTriggers()
                if (!tagMap.containsKey(newTag)) {
                    tagMap[newTag] = triggerPhrase
                    saveTagTriggers(tagMap)
                    renderTags(tagMap)
                    tagInput.text.clear()
                    triggerInput.text.clear()
                } else {
                    Toast.makeText(this, "Tag already exists.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        renderTags(getTagTriggers())
    }

    override fun onResume() {
        super.onResume()
    }

    private fun renderTags(tags: Map<String, String>) {
        tagList.removeAllViews()
        tags.toSortedMap().forEach { (tag, trigger) ->
            val tagView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
            }

            val tagText = TextView(this).apply {
                text = "$tag (\"$trigger\")"
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            }

            val deleteButton = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    val tagMap = getTagTriggers()
                    tagMap.remove(tag)
                    saveTagTriggers(tagMap)
                    renderTags(tagMap)
                }
            }

            tagView.addView(tagText)
            tagView.addView(deleteButton)
            tagList.addView(tagView)
        }
    }


    private fun getTagTriggers(): MutableMap<String, String> {
        val jsonString = sharedPreferences.getString("tag_triggers", "{}")
        val jsonObject = JSONObject(jsonString ?: "{}")
        val result = mutableMapOf<String, String>()
        for (key in jsonObject.keys()) {
            result[key] = jsonObject.getString(key)
        }
        return result
    }

    private fun saveTagTriggers(tagMap: Map<String, String>) {
        val jsonObject = JSONObject(tagMap)
        sharedPreferences.edit().putString("tag_triggers", jsonObject.toString()).apply()
    }
}