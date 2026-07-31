package de.westnordost.streetcomplete.quests.tree

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddTreeDenotation : OsmFilterQuestType<TreeDenotationAnswer>(), AndroidQuest {
    override val elementFilter = "nodes with natural = tree and !denotation"
    override val changesetComment = "Specify tree denotation"
    override val wikiLink = "Key:denotation"
    override val icon = R.drawable.quest_tree_denotation
    override val title = Res.string.quest_tree_denotation_title
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override val isDeleteElementEnabled = true

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with natural = tree")

    override fun createForm() = AddTreeDenotationForm()

    override fun applyAnswerTo(answer: TreeDenotationAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is TreeDenotation -> tags["denotation"] = answer.osmValue
            NotTreeButStump -> tags["natural"] = "tree_stump"
        }
    }
}
