package de.westnordost.streetcomplete.quests.osmose

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class OsmoseQuest(private val db: OsmoseDao, private val prefs: SharedPreferences) : OsmElementQuestType<OsmoseAnswer> {

    override fun getTitle(tags: Map<String, String>) = R.string.quest_osmose_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> =
        arrayOf(tags.toString())

    override val changesetComment = "Fix osmose issues"
    override val wikiLink = "Osmose"
    override val icon = R.drawable.ic_quest_osmose
    override val defaultDisabledMessage = R.string.quest_osmose_message

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val elements = mutableListOf<Element>()
        val map = db.getAll()
        mapData.forEach {
            if (map.contains(ElementKey(it.type, it.id)))
                elements.add(it)
        }
        return elements
    }

    override fun isApplicableTo(element: Element): Boolean =
        db.get(ElementKey(element.type, element.id)) != null

    override fun createForm() = OsmoseForm(db)

    override fun applyAnswerTo(answer: OsmoseAnswer, tags: Tags, timestampEdited: Long) {
        if (answer is AdjustTagAnswer) {
            tags[answer.tag] = answer.newValue
            db.setDone(answer.uuid)
        }
    }

    override val hasQuestSettings = true

    // actual ignoring of stuff happens when downloading
    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        singleTypeElementSelectionDialog(context, prefs, PREF_OSMOSE_ITEMS, "", "set osmose item types to hide, comma separated")

}

private const val PREF_OSMOSE_ITEMS = "quest_osmose_items"
