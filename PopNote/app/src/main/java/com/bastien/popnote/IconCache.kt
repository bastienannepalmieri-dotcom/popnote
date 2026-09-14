package com.bastien.popnote

import android.graphics.drawable.Drawable

/**
 * Charger une icône d'app (PackageManager.loadIcon) est l'opération la plus
 * coûteuse quand on liste toutes les apps installées. On la met en cache
 * en mémoire pour ne la faire qu'une fois par app, même si la liste est
 * rechargée plusieurs fois (ex: à chaque onResume).
 */
object IconCache {
    private val cache = HashMap<String, Drawable>()

    fun getOrLoad(packageName: String, loader: () -> Drawable): Drawable {
        return cache.getOrPut(packageName) { loader() }
    }

    fun clear() {
        cache.clear()
    }
}
