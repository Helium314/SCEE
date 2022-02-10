package de.westnordost.streetcomplete.quests.healthcare_speciality

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags

class AddHealthcareSpeciality : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
         amenity = doctors
         and name and !healthcare:speciality
    """
    override val changesetComment = "Add healthcare specialities"
    override val wikiLink = "Key:healthcare:speciality"
    override val icon = R.drawable.ic_quest_healthcare_speciality

    override fun getTitle(tags: Map<String, String>) = R.string.quest_healthcare_speciality_title

    override fun createForm() = AddHealthcareSpecialityForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["healthcare:speciality"] = answer
    }
}
