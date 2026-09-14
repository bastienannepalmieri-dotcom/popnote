package com.bastien.popnote.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.bastien.popnote.NotesRepository
import com.bastien.popnote.overlay.NoteOverlayService

/**
 * Écoute les changements de fenêtre au premier plan. Quand une app
 * "surveillée" passe au premier plan, on démarre le service qui affiche
 * la popup avec la note.
 *
 * Optimisation : la liste des apps surveillées est mise en cache en mémoire
 * (selectedAppsCache) au lieu d'être relue depuis le disque à chaque
 * événement d'accessibilité, qui peuvent être très fréquents. Le cache est
 * invalidé automatiquement via un listener dès que la liste change.
 */
class AppWatcherService : AccessibilityService(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var lastForegroundPackage: String? = null
    private var selectedAppsCache: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        selectedAppsCache = NotesRepository.getSelectedApps(this)
        NotesRepository.prefs(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == NotesRepository.KEY_SELECTED) {
            selectedAppsCache = NotesRepository.getSelectedApps(this)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        // Ignore soi-même et les changements de fenêtre à l'intérieur de la même app
        if (pkg == packageName || pkg == lastForegroundPackage) return
        lastForegroundPackage = pkg

        // Ignore les claviers / barres système
        if (pkg.contains("inputmethod") || pkg == "com.android.systemui") return

        if (selectedAppsCache.contains(pkg)) {
            if (!Settings.canDrawOverlays(this)) return // permission pas encore accordée
            val label = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) {
                pkg
            }
            val intent = Intent(this, NoteOverlayService::class.java)
            intent.putExtra(NoteOverlayService.EXTRA_PACKAGE, pkg)
            intent.putExtra(NoteOverlayService.EXTRA_LABEL, label)
            startService(intent)
        }
    }

    override fun onInterrupt() {
        // rien à faire
    }

    override fun onDestroy() {
        NotesRepository.prefs(this).unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }
}
