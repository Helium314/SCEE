package de.westnordost.streetcomplete.quests.orchard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddOrchardType : OsmFilterQuestType<OrchardType>(), AndroidQuest {

    override val elementFilter = """
        ways, relations with landuse = orchard and !orchard
    """
    override val changesetComment = "Specify orchard type"
    override val wikiLink = "Key:orchard"
    override val icon = R.drawable.quest_apple
    override val title = Res.string.quest_orchard_type_title
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun createForm() = AddOrchardTypeForm()

    override fun applyAnswerTo(answer: OrchardType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["orchard"] = answer.osmValue
    }
}
