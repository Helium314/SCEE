package de.westnordost.streetcomplete.data.overlays

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.fakeStringResource

class SelectedOverlayController(
    private val prefs: Preferences,
    private val overlayRegistry: OverlayRegistry
) : SelectedOverlaySource {

    private val listeners = Listeners<SelectedOverlaySource.Listener>()

    // must have local reference because the listeners are only a weak reference
    private val settingsListener: SettingsListener = prefs.onSelectedOverlayNameChanged {
        listeners.forEach { it.onSelectedOverlayChanged() }
    }

    override var selectedOverlay: Overlay?
        set(value) {
            if (value?.title == fakeStringResource) {
                value.wikiLink?.toIntOrNull()?.let { prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, it) }
                if (prefs.selectedOverlayName == /*CustomOverlay::class.simpleName*/ "CustomOverlay") { // todo, but later...
                    listeners.forEach { it.onSelectedOverlayChanged() }
                }
                prefs.selectedOverlayName = /*CustomOverlay::class.simpleName*/ "CustomOverlay" // todo, but later...
            }
            else if (value != null && value in overlayRegistry) {
                prefs.selectedOverlayName = value.name
            } else {
                prefs.selectedOverlayName = null
            }
        }
        get() = prefs.selectedOverlayName?.let { overlayRegistry.getByName(it) }

    override fun addListener(listener: SelectedOverlaySource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SelectedOverlaySource.Listener) {
        listeners.remove(listener)
    }
}
