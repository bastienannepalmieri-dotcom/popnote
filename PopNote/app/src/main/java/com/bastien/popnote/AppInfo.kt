package com.bastien.popnote

import android.graphics.drawable.Drawable

/**
 * Immuable : chaque changement (sélection, note) crée une copie via .copy(),
 * ce qui permet à la liste d'utiliser DiffUtil pour ne redessiner que
 * les lignes réellement modifiées.
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSelected: Boolean,
    val hasNote: Boolean
)
