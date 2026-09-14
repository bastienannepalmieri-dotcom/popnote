package com.bastien.popnote

import android.content.Context
import android.content.SharedPreferences

/**
 * Stockage simple (SharedPreferences) :
 *  - la liste des packages "surveillés" (dont l'ouverture déclenche la popup)
 *  - le texte de la note associée à chaque package
 */
object NotesRepository {

    private const val PREFS = "app_notes_prefs"
    const val KEY_SELECTED = "selected_apps"
    private const val NOTE_PREFIX = "note_"

    fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getSelectedApps(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_SELECTED, emptySet()) ?: emptySet()

    fun isSelected(context: Context, packageName: String): Boolean =
        getSelectedApps(context).contains(packageName)

    fun setAppSelected(context: Context, packageName: String, selected: Boolean) {
        val current = HashSet(getSelectedApps(context))
        if (selected) current.add(packageName) else current.remove(packageName)
        prefs(context).edit().putStringSet(KEY_SELECTED, current).apply()
    }

    fun getNote(context: Context, packageName: String): String =
        prefs(context).getString(NOTE_PREFIX + packageName, "") ?: ""

    fun setNote(context: Context, packageName: String, text: String) {
        prefs(context).edit().putString(NOTE_PREFIX + packageName, text).apply()
    }

    fun hasNote(context: Context, packageName: String): Boolean =
        getNote(context, packageName).isNotBlank()
}
