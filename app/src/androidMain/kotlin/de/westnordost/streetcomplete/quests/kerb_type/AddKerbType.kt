package de.westnordost.streetcomplete.quests.kerb_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddKerbType : OsmFilterQuestType<KerbType>(), AndroidQuest {

    override val elementFilter = """
        ways with
          barrier = kerb
          and !kerb
    """.trimIndent()

    override val changesetComment = "Specify kerb types"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.quest_kerb_type
    override val title = Res.string.quest_kerb_type_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    override fun createForm() = AddKerbTypeForm()

    override fun applyAnswerTo(answer: KerbType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == KerbType.NO_KERB) {
            tags.remove("barrier")
            tags["no:barrier"] = "kerb"
        }
    }
}
