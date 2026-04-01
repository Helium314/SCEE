package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.*

actual val tagEdit = object : ElementEditType {
    override val changesetComment = "Edit element"
    override val icon = R.drawable.ic_edit_tags
    override val title = Res.string.quest_generic_answer_show_edit_tags
    override val wikiLink: String? = null
    override val name = "TagEdit"
}

actual val addNodeEdit = object : ElementEditType {
    override val icon = R.drawable.ic_add_poi
    override val title = Res.string.create_poi
    override val wikiLink = null
    override val changesetComment = "Add node"
    override val name = "AddNode"
}
