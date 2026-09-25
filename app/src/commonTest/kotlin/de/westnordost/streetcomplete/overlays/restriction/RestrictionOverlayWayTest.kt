package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestrictionOverlayWayTest {

    @Test fun `getShortRestrictionValue prefers restriction then conditional then vehicle`() {
        assertEquals("no_left_turn", mapOf("restriction" to "no_left_turn").getShortRestrictionValue())
        assertEquals(
            "only_straight_on",
            mapOf("restriction:conditional" to "only_straight_on @ (Mo-Fr)").getShortRestrictionValue()
        )
        assertEquals(
            "no_u_turn",
            mapOf("restriction:hgv" to "no_u_turn").getShortRestrictionValue()
        )
        assertNull(emptyMap<String, String>().getShortRestrictionValue())
    }

    @Test fun `getWeightRestrictions finds plain and conditional keys`() {
        val way = way(tags = mapOf(
            "highway" to "secondary",
            "maxweight" to "7.5",
            "maxaxleload:conditional" to "8 @ (wet)",
        ))
        val restrictions = getWeightRestrictions(way)
        assertEquals(2, restrictions.size)
        assertTrue(restrictions.any { it.type == MaxWeightType.MAX_WEIGHT && it.weight == "7.5" })
        assertTrue(restrictions.any { it.type == MaxWeightType.MAX_AXLE_LOAD })
    }

    @Test fun `createTurnRestrictionRelation uses from via to roles`() {
        val from = way(1, nodes = listOf(1, 2))
        val to = way(2, nodes = listOf(2, 3))
        val via = node(2, LatLon(1.0, 2.0))
        val relation = createTurnRestrictionRelation(
            fromWay = from,
            toWay = to,
            viaNode = via,
            restrictionType = "no_left_turn",
            signed = true,
        )
        assertEquals(0L, relation.id)
        assertEquals("restriction", relation.tags["type"])
        assertEquals("no_left_turn", relation.tags["restriction"])
        assertNull(relation.tags["explicit"])
        assertEquals(
            listOf(
                RelationMember(ElementType.WAY, 1, "from"),
                RelationMember(ElementType.WAY, 2, "to"),
                RelationMember(ElementType.NODE, 2, "via"),
            ),
            relation.members
        )
    }

    @Test fun `createTurnRestrictionRelation marks unsigned with explicit=yes`() {
        val relation = createTurnRestrictionRelation(
            fromWay = way(1, nodes = listOf(1, 2)),
            toWay = way(2, nodes = listOf(2, 3)),
            viaNode = node(2),
            restrictionType = "only_right_turn",
            signed = false,
        )
        assertEquals("yes", relation.tags["explicit"])
    }

    @Test fun `withTurnRestrictionType updates plain restriction`() {
        val relation = rel(tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"))
        assertEquals(
            "no_right_turn",
            withTurnRestrictionType(relation, "no_right_turn").tags["restriction"]
        )
    }

    @Test fun `withTurnRestrictionType updates conditional-only restriction`() {
        val relation = rel(tags = mapOf(
            "type" to "restriction",
            "restriction:conditional" to "no_left_turn @ (Mo-Fr)",
        ))
        val updated = withTurnRestrictionType(relation, "no_u_turn")
        assertEquals("no_u_turn @ (Mo-Fr)", updated.tags["restriction:conditional"])
    }

    @Test fun `withSwappedFromTo swaps roles`() {
        val relation = Relation(
            id = 1,
            members = listOf(
                RelationMember(ElementType.WAY, 10, "from"),
                RelationMember(ElementType.WAY, 20, "to"),
                RelationMember(ElementType.NODE, 30, "via"),
            ),
            tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"),
        )
        val swapped = withSwappedFromTo(relation)
        assertEquals("to", swapped.members[0].role)
        assertEquals("from", swapped.members[1].role)
        assertEquals("via", swapped.members[2].role)
        assertEquals(10L, swapped.members[0].ref)
        assertEquals(20L, swapped.members[1].ref)
    }

    @Test fun `from to swap allowed only for draft relations`() {
        val draft = createTurnRestrictionRelation(
            fromWay = way(1, nodes = listOf(1, 2)),
            toWay = way(2, nodes = listOf(2, 3)),
            viaNode = node(2),
            restrictionType = "no_left_turn",
            signed = true,
        )
        assertTrue(isTurnRestrictionFromToSwapAllowed(draft))
        assertFalse(
            isTurnRestrictionFromToSwapAllowed(
                draft.copy(id = 42L)
            )
        )
    }

    @Test fun `draft swap updates roles used for create and way role helper`() {
        val from = way(10, nodes = listOf(1, 2))
        val to = way(20, nodes = listOf(2, 3))
        val draft = createTurnRestrictionRelation(
            fromWay = from,
            toWay = to,
            viaNode = node(2),
            restrictionType = "only_right_turn",
            signed = true,
        )
        assertEquals("from", wayRoleInTurnRestriction(draft, from.id))
        assertEquals("to", wayRoleInTurnRestriction(draft, to.id))

        val swapped = withSwappedFromTo(draft)
        assertTrue(isTurnRestrictionFromToSwapAllowed(swapped))
        assertEquals("to", wayRoleInTurnRestriction(swapped, from.id))
        assertEquals("from", wayRoleInTurnRestriction(swapped, to.id))
        assertEquals(
            listOf(
                RelationMember(ElementType.WAY, 10, "to"),
                RelationMember(ElementType.WAY, 20, "from"),
                RelationMember(ElementType.NODE, 2, "via"),
            ),
            swapped.members
        )
    }

    @Test fun `existing relation swap helper still swaps members but is not allowed in UI`() {
        val existing = rel(
            id = 99,
            members = listOf(
                RelationMember(ElementType.WAY, 1, "from"),
                RelationMember(ElementType.WAY, 2, "to"),
                RelationMember(ElementType.NODE, 3, "via"),
            ),
            tags = mapOf("type" to "restriction", "restriction" to "no_u_turn"),
        )
        assertFalse(isTurnRestrictionFromToSwapAllowed(existing))
        val swapped = withSwappedFromTo(existing)
        assertEquals("to", swapped.members[0].role)
        assertEquals("from", swapped.members[1].role)
        assertEquals(99L, swapped.id)
    }

    @Test fun `withExceptions writes and clears except tag`() {
        val relation = rel(tags = mapOf("type" to "restriction", "restriction" to "no_u_turn"))
        assertEquals(
            "bicycle;bus",
            withExceptions(relation, listOf("bicycle", "bus")).tags["except"]
        )
        assertNull(withExceptions(relation, emptyList()).tags["except"])
    }

    @Test fun `withOnlyFor moves restriction to vehicle key`() {
        val relation = rel(tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"))
        val hgv = withOnlyFor(relation, "hgv")
        assertNull(hgv.tags["restriction"])
        assertEquals("no_left_turn", hgv.tags["restriction:hgv"])

        val cleared = withOnlyFor(hgv, null)
        assertEquals("no_left_turn", cleared.tags["restriction"])
        assertNull(cleared.tags["restriction:hgv"])
    }

    @Test fun `withImplicitSigned toggles implicit tag`() {
        val relation = rel(tags = mapOf("type" to "restriction", "restriction" to "no_u_turn"))
        assertEquals("yes", withImplicitSigned(relation, signed = false).tags["implicit"])
        assertNull(withImplicitSigned(relation, signed = true).tags["implicit"])
    }

    @Test fun `getInitialRestrictionPreferComplete prefers supported complete turn`() {
        val supported = TurnRestriction(
            rel(
                id = 1,
                members = listOf(
                    RelationMember(ElementType.WAY, 1, "from"),
                    RelationMember(ElementType.WAY, 2, "to"),
                    RelationMember(ElementType.NODE, 3, "via"),
                ),
                tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"),
            )
        )
        val weight = WeightRestriction(way(tags = mapOf("maxweight" to "5")), MaxWeightType.MAX_WEIGHT, "5")
        val incomplete = TurnRestriction(
            rel(
                id = 2,
                members = listOf(
                    RelationMember(ElementType.WAY, 1, "from"),
                    RelationMember(ElementType.WAY, 2, "to"),
                    RelationMember(ElementType.NODE, 3, "via"),
                ),
                tags = mapOf("type" to "restriction", "restriction" to "no_right_turn"),
            )
        )
        assertEquals(
            supported,
            getInitialRestrictionPreferComplete(listOf(incomplete, weight, supported)) { it.id == 1L }
        )
        assertEquals(
            weight,
            getInitialRestrictionPreferComplete(listOf(incomplete, weight)) { false }
        )
    }

    @Test fun `isSupportedTurnRestriction validates members and type`() {
        assertTrue(
            rel(
                members = listOf(
                    RelationMember(ElementType.WAY, 1, "from"),
                    RelationMember(ElementType.WAY, 2, "to"),
                    RelationMember(ElementType.NODE, 3, "via"),
                ),
                tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"),
            ).isSupportedTurnRestriction()
        )
        // Historical: missing via still counts as "supported type shape" (completeness is separate)
        assertTrue(
            rel(
                members = listOf(
                    RelationMember(ElementType.WAY, 1, "from"),
                    RelationMember(ElementType.WAY, 2, "to"),
                ),
                tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"),
            ).isSupportedTurnRestriction()
        )
        assertFalse(
            rel(
                members = listOf(
                    RelationMember(ElementType.WAY, 1, "from"),
                    RelationMember(ElementType.WAY, 2, "to"),
                    RelationMember(ElementType.NODE, 3, "via"),
                ),
                tags = mapOf("type" to "restriction", "restriction" to "no_entry"),
            ).isSupportedTurnRestriction()
        )
        assertFalse(
            rel(
                members = listOf(
                    RelationMember(ElementType.WAY, 1, "from"),
                    RelationMember(ElementType.WAY, 2, "to"),
                    RelationMember(ElementType.NODE, 3, "via"),
                    RelationMember(ElementType.WAY, 4, "via"),
                ),
                tags = mapOf("type" to "restriction", "restriction" to "no_left_turn"),
            ).isSupportedTurnRestriction()
        )
    }

    @Test fun `getIconForTurnRestriction covers supported types`() {
        assertNotNull(getIconForTurnRestriction("no_left_turn"))
        assertNotNull(getIconForTurnRestriction("only_straight_on"))
        assertEquals(
            getIconForTurnRestriction(null),
            getIconForTurnRestriction("something_else")
        )
    }
}
