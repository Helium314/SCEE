package de.westnordost.streetcomplete.quests.external

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.databinding.QuestOsmoseExternalBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.toast
import org.koin.android.ext.android.inject

class ExternalForm(private val externalList: ExternalList) : AbstractOsmQuestForm<Boolean>() {

    override val contentLayoutResId = R.layout.quest_osmose_external
    private val binding by contentViewBinding(QuestOsmoseExternalBinding::bind)

    private val osmQuestController: OsmQuestController by inject()

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_external_remove) {
            val key = ElementKey(element.type, element.id)
            externalList.remove(key)
            osmQuestController.delete(questKey as OsmQuestKey)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val key = ElementKey(element.type, element.id)
        val text = externalList.questsMap[key]
        if (text == null) {
            context?.toast(R.string.quest_external_osmose_not_found)
            osmQuestController.delete(questKey as OsmQuestKey)
            return
        }
        setTitle(resources.getString(R.string.quest_external_title, text))
    }

}