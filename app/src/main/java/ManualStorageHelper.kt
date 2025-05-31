package com.example.voicejournal

import android.content.Context
import android.net.Uri
import android.widget.Toast
import java.io.IOException
import com.example.voicejournal.TagUtils

object ManualStorageHelper {

    fun exportLogsToUri(context: Context, uri: Uri) {
        try {
            val voiceNoteManager = VoiceNoteManager(context)
            val allNotes = voiceNoteManager.getAllJournalEntries()

            val gson = com.google.gson.Gson()
            val jsonString = gson.toJson(allNotes)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
                Toast.makeText(context, "Notes exported successfully!", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "Failed to open file for writing", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unexpected error during export: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun importLogsFromUri(context: Context, uri: Uri) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: run {
                Toast.makeText(context, "Failed to open file for reading", Toast.LENGTH_SHORT).show()
                return
            }
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<VoiceNote>>() {}.type
            val importedNotes: List<VoiceNote> = gson.fromJson(jsonString, type)

            if (importedNotes.isEmpty()) {
                Toast.makeText(context, "No notes found in the selected file", Toast.LENGTH_SHORT).show()
                return
            }
            showImportConfirmationDialog(context, importedNotes)

        } catch (e: com.google.gson.JsonSyntaxException) {
            Toast.makeText(context, "Invalid file format. Please select a valid voice notes backup file.", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unexpected error during import: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImportConfirmationDialog(context: Context, importedNotes: List<VoiceNote>) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Import Notes")
        builder.setMessage("Found ${importedNotes.size} notes to import. This will add them to your existing notes. Continue?")

        builder.setPositiveButton("Import") { _, _ ->
            performImport(context, importedNotes)
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(context, "Import cancelled", Toast.LENGTH_SHORT).show()
        }

        builder.setCancelable(true)
        builder.show()
    }

    private fun performImport(context: Context, importedNotes: List<VoiceNote>) {
        try {
            val voiceNoteManager = VoiceNoteManager(context)
            var successCount = 0

            for (note in importedNotes) {
                val success = voiceNoteManager.saveJournalEntry(note.text, note.tags.toSet())
                if (success) {
                    successCount++
                }
            }

            if (successCount == importedNotes.size) {
                Toast.makeText(context, "Successfully imported $successCount notes!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Imported $successCount out of ${importedNotes.size} notes", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to import notes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}