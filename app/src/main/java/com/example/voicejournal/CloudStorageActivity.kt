package com.example.voicejournal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class CloudStorageActivity : AppCompatActivity() {

    private lateinit var cloudStatusTextView: TextView
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var manualExportLauncher: ActivityResultLauncher<String>
    private lateinit var manualImportLauncher: ActivityResultLauncher<String>

    companion object {
        private const val REQUEST_CODE_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_storage)

        cloudStatusTextView = findViewById(R.id.cloudStatusTextView)
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult()
                    cloudStatusTextView.text = getString(R.string.signed_in_as, account?.email ?: "Unknown")
                } catch (e: Exception) {
                    Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    cloudStatusTextView.text = getString(R.string.not_signed_in)
                }
            } else {
                Toast.makeText(this, getString(R.string.sign_in_cancelled), Toast.LENGTH_SHORT).show()
                cloudStatusTextView.text = getString(R.string.not_signed_in)
            }
        }
        manualExportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                ManualStorageHelper.exportLogsToUri(this, uri)
            }
        }
        manualImportLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                ManualStorageHelper.importLogsFromUri(this, uri)
            }
        }

        findViewById<Button>(R.id.exportButton).setOnClickListener {
            // TODO: Export to Google Drive
            Toast.makeText(this, "Google Drive export coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.importButton).setOnClickListener {
            // TODO: Import from Google Drive
            Toast.makeText(this, "Google Drive import coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.manualExportButton).setOnClickListener {
            manualExportLauncher.launch("voice_notes_backup.json")
        }

        findViewById<Button>(R.id.manualImportButton).setOnClickListener {
            manualImportLauncher.launch("application/json")
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            signInToDrive()
        }
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            cloudStatusTextView.text = getString(R.string.signed_in_as, account.email ?: "Unknown")
        } else {
            cloudStatusTextView.text = getString(R.string.not_signed_in)
        }
    }

    private fun signInToDrive() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(this, signInOptions)
        signInLauncher.launch(client.signInIntent)
    }
}