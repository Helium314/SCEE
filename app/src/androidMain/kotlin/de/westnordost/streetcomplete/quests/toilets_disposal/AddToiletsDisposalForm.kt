package de.westnordost.streetcomplete.quests.toilets_disposal

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm

class AddToiletsDisposalForm :
    ARadioGroupQuestForm<ToiletsDisposalType, ToiletsDisposalType>() {

    override val items = ToiletsDisposalType.entries

    @Composable
    override fun BoxScope.ItemContent(item: ToiletsDisposalType) {
        Text(
            stringResource(
                when (item) {
                    ToiletsDisposalType.FLUSH ->
                        R.string.quest_toilets_disposal_flush

                    ToiletsDisposalType.PIT_LATRINE ->
                        R.string.quest_toilets_disposal_pit

                    ToiletsDisposalType.CHEMICAL ->
                        R.string.quest_toilets_disposal_chemical
                }
            )
        )
    }
}
