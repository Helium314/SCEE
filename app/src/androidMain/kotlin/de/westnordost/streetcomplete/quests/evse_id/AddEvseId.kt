package de.westnordost.streetcomplete.quests.evse_id

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.contains
import java.util.Locale

class AddEvseId :
    OsmElementQuestType<String>,
    AndroidQuest {

    override val icon = R.drawable.ic_quest_charger_ref
    override val wikiLink = "Key:ref:EU:EVSE"
    override val changesetComment = "Add EVSE ID (ref:EU:EVSE)"
    override val enabledInCountries = NoCountriesExcept(
        "AT","BE","BG","CH","CY","CZ","DE","DK","EE","ES","FI","FR","GR","HR",
        "HU","IE","IT","LT","LU","LV","MT","NL","PL","PT","RO","SE","SI","SK"
    )

    override val achievements = listOf(CITIZEN)

    private val baseFilter = """
        nodes, ways with
          (man_made = charge_point or amenity = charging_station)
          and !ref:EU:EVSE
          and (ref:signed != no or !ref:signed)
          and access !~ private|no
    """.toElementFilterExpression()

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_evse_id_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {

        val candidates = mapData.filter(baseFilter)

        return candidates.filter { element ->

            if (element.tags["amenity"] == "charging_station") {

                val geometry =
                    mapData.getGeometry(element.type, element.id) ?: return@filter true

                val bounds = geometry.bounds

                val hasChargePointsInside = mapData
                    .filter("nodes with man_made = charge_point")
                    .any { cp ->
                        val cpGeom =
                            mapData.getGeometry(cp.type, cp.id) ?: return@any false
                        bounds.contains(cpGeom.center)
                    }

                if (hasChargePointsInside) return@filter false
            }

            true
        }.toList()
    }

    // Geometry-dependent → return null to trigger surrounding-data re-check
    override fun isApplicableTo(element: Element): Boolean? =
        if (baseFilter.matches(element)) null else false

    override fun getHighlightedElements(
        element: Element,
        getMapData: () -> MapDataWithGeometry
    ): Sequence<Element> = sequenceOf(element)

    override fun createForm(): AddEvseIdForm = AddEvseIdForm()

    override fun applyAnswerTo(
        answer: String,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        if (answer.startsWith("ref:signed=")) {
            tags["ref:signed"] = answer.substringAfter("=")
            return
        }

        val normalized = answer
            .split(";")
            .map { it.trim().uppercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }
            .joinToString(";")

        if (normalized.isNotEmpty()) {
            tags["ref:EU:EVSE"] = normalized
        }
    }
}
