package com.bastien.popnote

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bastien.popnote.databinding.ActivityNoteEditBinding

class NoteEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_LABEL = "extra_label"
    }

    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var packageNameExtra: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pkg = intent.getStringExtra(EXTRA_PACKAGE)
        if (pkg == null) {
            finish()
            return
        }
        packageNameExtra = pkg
        val label = intent.getStringExtra(EXTRA_LABEL) ?: pkg

        binding.tvAppName.text = label
        try {
            binding.imgAppIcon.setImageDrawable(packageManager.getApplicationIcon(pkg))
        } catch (e: PackageManager.NameNotFoundException) {
            // icône par défaut si introuvable
        }

        // Si la note possède déjà du texte, il apparaît immédiatement à l'ouverture
        val existingNote = NotesRepository.getNote(this, pkg)
        binding.editNote.setText(existingNote)
        binding.editNote.setSelection(existingNote.length)

        binding.btnSaveNote.setOnClickListener {
            NotesRepository.setNote(this, pkg, binding.editNote.text.toString())
            Toast.makeText(this, "Note enregistrée", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        // Sauvegarde automatique si l'utilisateur quitte sans appuyer sur "Enregistrer"
        if (::packageNameExtra.isInitialized) {
            NotesRepository.setNote(this, packageNameExtra, binding.editNote.text.toString())
        }
    }
}
