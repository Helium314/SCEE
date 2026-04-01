package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_shelter_type_disabled_msg
import de.westnordost.streetcomplete.resources.quest_shelter_type_title

class AddShelterType : OsmFilterQuestType<ShelterType>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          amenity = shelter
          and !shelter_type
    """
    override val changesetComment = "Specify shelter types"
    override val wikiLink = "Key:shelter_type"
    override val icon = R.drawable.ic_quest_shelter_type
    override val title = Res.string.quest_shelter_type_title
    override val isDeleteElementEnabled = true
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)
    override val defaultDisabledMessage = Res.string.quest_shelter_type_disabled_msg

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = shelter")

    override fun createForm() = AddShelterTypeForm()

    override fun applyAnswerTo(answer: ShelterType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shelter_type"] = answer.osmValue
    }
}

