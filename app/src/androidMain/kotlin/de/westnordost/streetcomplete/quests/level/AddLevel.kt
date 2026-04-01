package de.westnordost.streetcomplete.quests.level

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.quests.BooleanQuestSettingsDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddLevel : OsmElementQuestType<String>, AndroidQuest {

    /* only nodes because ways/relations are not likely to be floating around freely in a mall
     * outline */
    private val filter by lazy { """
        nodes with
          !level
          and (name or brand or noname = yes or name:signed = no)
    """.toElementFilterExpression() }

    /* including any kind of public transport station because even really large bus stations feel
     * like small airport terminals, like Mo Chit 2 in Bangkok*/
    private val mallFilter by lazy { """
        ways, relations with
         shop = mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
    """.toElementFilterExpression() }

    override val changesetComment = "Determine on which level shops are in a building"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.quest_level
    override val title = Res.string.quest_level_title2
    /* disabled because in a mall with multiple levels, if there are nodes with no level defined,
     * it really makes no sense to tag something as vacant if the level is not known. Instead, if
     * the user cannot find the place on any level in the mall, delete the element completely. */
    override val isReplacePlaceEnabled = false
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        if (prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false))
            return getApplicableElementsForMoreLevels(mapData)

        // get geometry of all malls in the area
        val mallGeometries = mapData
            .filter { mallFilter.matches(it) }
            .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }
        if (mallGeometries.isEmpty()) return emptyList()

        val multiLevelMallGeometries = getMultiLevelMallGeometries(mallGeometries, mapData)
        if (multiLevelMallGeometries.isEmpty()) return emptyList()

        // now, return all shops that have no level tagged and are inside those multi-level malls
        val shopsWithoutLevel = mapData
            .filter(filter)
            .filter { it.isPlace() }
            .toMutableList()
        if (shopsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()

        for (mallGeometry in multiLevelMallGeometries) {
            val it = shopsWithoutLevel.iterator()
            while (it.hasNext()) {
                val shop = it.next()
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(shop)
                it.remove() // shop can only be in one mall
            }
        }
        return result
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false))
            return isApplicableToForMoreLevels(element)
        if (!filter.matches(element) || !element.isPlace()) return false
        // for shops with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    override fun createForm() = AddLevelForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["level"] = answer
    }

    // allows other buildings as long as they have more than one level
    private val mallAndMoreFilter by lazy { """
        ways, relations with
         shop = mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
         or (building and building:levels > 1)
    """.toElementFilterExpression() }

    private val nodesWithoutLevelFilter by lazy { "nodes with !level".toElementFilterExpression() }

    private fun getApplicableElementsForMoreLevels(mapData: MapDataWithGeometry): Iterable<Element> {
        // get geometry of all malls in the area
        val geometries = mapData
            .filter { mallAndMoreFilter.matches(it) }
            .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }
        if (geometries.isEmpty()) return emptyList()

        val doctors = mapData.filter { it.isDoctorWithoutLevel() }
        val result = mutableListOf<Element>()
        doctors.forEach docs@{ doc ->
            geometries.forEach { geometry ->
                if (mapData.getGeometry(doc.type, doc.id)?.center?.isInMultipolygon(geometry.polygons) != true)
                    return@forEach
                result.add(doc)
                return@docs
            }
        }

        val multiLevelMallGeometries = getMultiLevelMallGeometries(geometries, mapData)
        if (multiLevelMallGeometries.isEmpty()) return result

        // now, return all shops that have no level tagged and are inside those multi-level malls
        val shopsWithoutLevel = mapData
            .filter(nodesWithoutLevelFilter)
            .filter { it.isPlace() && !it.isDoctorWithoutLevel() }
            .toMutableList()
        if (shopsWithoutLevel.isEmpty()) return result

        for (mallGeometry in multiLevelMallGeometries) {
            val it = shopsWithoutLevel.iterator()
            while (it.hasNext()) {
                val shop = it.next()
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(shop)
                it.remove() // shop can only be in one mall
            }
        }
        return result
    }

    private fun isApplicableToForMoreLevels(element: Element): Boolean? {
        if (!nodesWithoutLevelFilter.matches(element) || !element.isPlace()) return false
        return null
    }

    override val hasQuestSettings = true

    @Composable override fun QuestSettings(onDismissRequest: () -> Unit) {
        BooleanQuestSettingsDialog(
            prefs,
            questPrefix(prefs) + PREF_MORE_LEVELS,
            false,
            R.string.quest_settings_level_title,
            R.string.quest_settings_level_more,
            R.string.quest_settings_level_default,
            onDismissRequest
        )
    }
}

private fun Element.isDoctorWithoutLevel() = !tags.containsKey("level") && when (tags["amenity"]) {
    "doctors", "dentist" -> true
    else -> when (tags["healthcare"]) {
        "doctor", "dentist", "psychotherapist", "physiotherapist" -> true
        else -> false
    }
}

const val PREF_MORE_LEVELS = "qs_AddLevel_more_levels"
