package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class AddServiceBuildingOperator : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes, ways, relations with building = service and !operator and !name and !brand"
    override val commitMessage = "Add service building operator"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_operator_title

    override fun createForm() = AddServiceBuildingOperatorForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("operator", answer)
    }
}
