package com.bastien.popnote.overlay

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.WindowManager
import com.bastien.popnote.NotesRepository
import com.bastien.popnote.databinding.OverlayNoteBinding

class NoteOverlayService : Service() {

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_LABEL = "extra_label"
    }

    private var windowManager: WindowManager? = null
    private var binding: OverlayNoteBinding? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pkg = intent?.getStringExtra(EXTRA_PACKAGE)
        if (pkg == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Si une popup est déjà affichée, on ne l'empile pas une deuxième fois
        if (binding != null) {
            return START_NOT_STICKY
        }

        val label = intent.getStringExtra(EXTRA_LABEL) ?: pkg
        showOverlay(pkg, label)
        return START_NOT_STICKY
    }

    private fun showOverlay(pkg: String, label: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val b = OverlayNoteBinding.inflate(LayoutInflater.from(this))
        binding = b

        b.tvOverlayAppName.text = label
        try {
            b.imgOverlayIcon.setImageDrawable(packageManager.getApplicationIcon(pkg))
        } catch (e: PackageManager.NameNotFoundException) {
            // pas grave si l'icône n'est pas trouvée
        }

        // La note existante (si elle contient déjà du texte) apparaît directement
        val existingNote = NotesRepository.getNote(this, pkg)
        b.editOverlayNote.setText(existingNote)
        b.editOverlayNote.setSelection(existingNote.length)

        b.btnOverlaySave.setOnClickListener {
            NotesRepository.setNote(this, pkg, b.editOverlayNote.text.toString())
            removeOverlay()
        }

        b.btnOverlayClose.setOnClickListener {
            // On enregistre aussi en fermant, pour ne jamais perdre ce qui a été tapé
            NotesRepository.setNote(this, pkg, b.editOverlayNote.text.toString())
            removeOverlay()
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager?.addView(b.root, params)
    }

    private fun removeOverlay() {
        binding?.let {
            try {
                windowManager?.removeView(it.root)
            } catch (e: Exception) {
                // vue déjà retirée
            }
        }
        binding = null
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
