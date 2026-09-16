package com.osabra.gabumon

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout

/** Shows a real GLB when it is packaged; otherwise keeps the existing procedural fallback. */
class DigimonModelView(context: Context, initialDigimon: String = "Gabumon") : FrameLayout(context) {
    private var currentName = initialDigimon
    private var glbView: GlbDigimonView? = null
    private var fallbackView: Digimon3DView? = null

    init {
        isClickable = true
        rebuild(initialDigimon)
    }

    fun setDigimon(name: String) {
        if (name == currentName) return
        currentName = name
        if (glbView != null) glbView?.setDigimon(name) else rebuild(name)
    }

    private fun rebuild(name: String) {
        removeAllViews()
        val hasGlb = try {
            context.assets.open("digimon/$name.glb").close()
            true
        } catch (_: Exception) { false }

        if (hasGlb) {
            glbView = GlbDigimonView(context, name)
            fallbackView = null
            addView(glbView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        } else {
            fallbackView = Digimon3DView(context, name)
            glbView = null
            addView(fallbackView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }
}
