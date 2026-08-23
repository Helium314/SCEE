package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.create_poi
import de.westnordost.streetcomplete.resources.ic_add_poi
import de.westnordost.streetcomplete.resources.ic_edit_tags
import de.westnordost.streetcomplete.resources.quest_generic_answer_show_edit_tags

val tagEdit = object : ElementEditType {
    override val changesetComment = "Edit element"
    override val icon = Res.drawable.ic_edit_tags
    override val title = Res.string.quest_generic_answer_show_edit_tags
    override val wikiLink: String? = null
    override val name = "TagEdit"
}

val addNodeEdit = object : ElementEditType {
    override val icon = Res.drawable.ic_add_poi
    override val title = Res.string.create_poi
    override val wikiLink = null
    override val changesetComment = "Add node"
    override val name = "AddNode"
}
