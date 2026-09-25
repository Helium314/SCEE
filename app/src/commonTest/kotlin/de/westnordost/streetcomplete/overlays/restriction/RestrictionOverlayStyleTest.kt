package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_restriction_give_way
import de.westnordost.streetcomplete.resources.ic_restriction_stop
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RestrictionOverlayStyleTest {

    private val overlay = RestrictionOverlay()

    @Test fun `stop and give_way nodes get point styles with icons`() {
        val stop = node(1, tags = mapOf("highway" to "stop"))
        val giveWay = node(2, tags = mapOf("highway" to "give_way"))
        val other = node(3, tags = mapOf("highway" to "traffic_signals"))
        val mapData = TestMapDataWithGeometry(listOf(stop, giveWay, other))

        val styles = overlay.getStyledElements(mapData).associate { it.first.id to it.second }

        val stopStyle = assertIs<OverlayStyle.Point>(styles[1])
        assertEquals(Res.drawable.ic_restriction_stop, stopStyle.icon)

        val giveWayStyle = assertIs<OverlayStyle.Point>(styles[2])
        assertEquals(Res.drawable.ic_restriction_give_way, giveWayStyle.icon)

        assertEquals(null, styles[3])
    }
}
