package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.view.toAndroidResourceId
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression for StyleableOverlayMapComponent / GeometryMarkersMapComponent NPE:
 * every RestrictionOverlay map icon must resolve via copyIconsToAndroid / IconIndex.
 *
 * Prefer [restrictionOverlayAndroidMapIcons] so newly added overlay map icons fail loudly.
 */
class RestrictionOverlayAndroidIconTest {

    @Test fun `all RestrictionOverlay Android map icons resolve to resource ids`() {
        val icons = restrictionOverlayAndroidMapIcons()
        assertTrue(icons.isNotEmpty())
        for (icon in icons) {
            assertNotNull(
                icon.toAndroidResourceId(),
                "Missing Android IconIndex mapping for $icon — add it to copyIconsToAndroid"
            )
        }
    }
}
