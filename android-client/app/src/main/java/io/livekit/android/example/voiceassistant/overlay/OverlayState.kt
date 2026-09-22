package io.livekit.android.example.voiceassistant.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bridges live call state from the in-app Compose screen to the system-level
 * overlay window (which runs in a Service, outside the screen's lifecycle).
 * Both sides just read/write these flows — no direct references needed.
 */
object OverlayState {
    private val _label = MutableStateFlow("Search or Ask")
    val label = _label.asStateFlow()

    private val _intensity = MutableStateFlow(0.15f)
    val intensity = _intensity.asStateFlow()

    fun update(label: String, intensity: Float) {
        _label.value = label
        _intensity.value = intensity
    }
}
