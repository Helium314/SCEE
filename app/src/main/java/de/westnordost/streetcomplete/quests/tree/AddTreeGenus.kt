package de.westnordost.streetcomplete.quests.tree

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags

class AddTreeGenus : OsmFilterQuestType<Tree>() {

    override val elementFilter = """
        nodes with
          natural = tree
          and !genus and !~"genus:.*"
          and !species and !~"species:.*"
          and !taxon and !~"taxon:.*"
    """
    override val changesetComment = "Add tree genus/species"
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming
    override val wikiLink = "Key:genus"
    override val icon = R.drawable.ic_quest_tree

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tree_genus_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with natural = tree")

    override fun createForm() = AddTreeGenusForm()

    override fun applyAnswerTo(answer: Tree, tags: Tags, timestampEdited: Long) {
        if (answer.isSpecies)
            tags["species"] = answer.name
        else
            tags["genus"] = answer.name
    }
}
