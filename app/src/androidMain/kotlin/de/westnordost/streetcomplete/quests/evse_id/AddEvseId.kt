package de.westnordost.streetcomplete.quests.evse_id

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import java.util.Locale

class AddEvseId : OsmFilterQuestType<String>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          (man_made = charge_point or amenity = charging_station)
          and !ref:EU:EVSE
          and !ref:signed=no
          and access !~ private|no
    """

    override val changesetComment = "Add EVSE ID (ref:EU:EVSE)"
    override val wikiLink = "Key:ref:EU:EVSE"
    override val icon = R.drawable.ic_quest_charger_ref
    override val enabledInCountries = NoCountriesExcept(
        "AT","BE","BG","CY","CZ","DE","DK","EE","ES","FI","FR","GR","HR",
        "HU","IE","IT","LT","LU","LV","MT","NL","PL","PT","RO","SE","SI","SK"
    )
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_evse_id_title

    override fun getHighlightedElements(
        element: Element,
        getMapData: () -> MapDataWithGeometry
    ) =
        getMapData().filter(
            "nodes, ways with man_made = charge_point or amenity = charging_station"
        )

    override fun createForm() = AddEvseIdForm()

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
