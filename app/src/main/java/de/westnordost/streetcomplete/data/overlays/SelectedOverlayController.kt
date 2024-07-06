package de.westnordost.streetcomplete.data.overlays

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.util.Listeners

class SelectedOverlayController(
    private val prefs: ObservableSettings,
    private val overlayRegistry: OverlayRegistry
) : SelectedOverlaySource {

    private val listeners = Listeners<SelectedOverlaySource.Listener>()

    private val selectedOverlayByPref =
        prefs.getStringOrNull(Prefs.SELECTED_OVERLAY)?.let {
            if (it.contains(CustomOverlay::class.simpleName.toString())) {
                overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
            } else {
                overlayRegistry.getByName(it)
            }
        }

    override var selectedOverlay: Overlay? = selectedOverlayByPref
        set(value) {
            field = value
            if (value != null && value in overlayRegistry || value is Overlay) {
                prefs.putString(Prefs.SELECTED_OVERLAY, value.name)
            } else {
                prefs.remove(Prefs.SELECTED_OVERLAY)
            }
            listeners.forEach { it.onSelectedOverlayChanged() }
        }

    override fun addListener(listener: SelectedOverlaySource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SelectedOverlaySource.Listener) {
        listeners.remove(listener)
    }
}
