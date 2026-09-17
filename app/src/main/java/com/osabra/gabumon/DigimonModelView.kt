package com.osabra.gabumon

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * Main 3D Digimon viewer.
 *
 * Use the bundled GLB assets as the source of truth. The procedural fallback
 * used previously made the characters look like collections of geometric
 * primitives, which was especially noticeable on Biyomon and Gabumon.
 */
class DigimonModelView(context: Context, initialDigimon: String = "Gabumon") : FrameLayout(context) {
    private var currentName = initialDigimon
    private var glbView: GlbDigimonView

    init {
        isClickable = true
        glbView = GlbDigimonView(context, initialDigimon)
        addView(
            glbView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun setDigimon(name: String) {
        if (name == currentName) return
        currentName = name
        glbView.setDigimon(name)
    }
}
