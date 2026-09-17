package com.osabra.gabumon

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * Anime-style Digimon viewer.
 *
 * The bundled GLB assets are low-poly/faceted and do not match the smooth
 * proportions of the anime designs. Until high-detail anime-quality assets
 * replace them, use the smooth procedural renderer for every Digimon so the
 * characters do not appear as angular geometric figures.
 */
class DigimonModelView(context: Context, initialDigimon: String = "Gabumon") : FrameLayout(context) {
    private var currentName = initialDigimon
    private var fallbackView: Digimon3DView? = null

    init {
        isClickable = true
        rebuild(initialDigimon)
    }

    fun setDigimon(name: String) {
        if (name == currentName) return
        currentName = name
        fallbackView?.setDigimon(name)
    }

    private fun rebuild(name: String) {
        removeAllViews()
        fallbackView = Digimon3DView(context, name)
        addView(
            fallbackView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
}
