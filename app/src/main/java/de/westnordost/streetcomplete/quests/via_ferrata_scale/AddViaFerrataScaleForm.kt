package de.westnordost.streetcomplete.quests.via_ferrata_scale

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.view.isGone
import de.westnordost.streetcomplete.util.ktx.asImageSpan
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddViaFerrataScaleForm : AImageListQuestForm<ViaFerrataScale, ViaFerrataScale>() {

    override val items: List<DisplayItem<ViaFerrataScale>> get() = listOf(
        ViaFerrataScale.ZERO,
        ViaFerrataScale.ONE,
        ViaFerrataScale.TWO,
        ViaFerrataScale.THREE,
        ViaFerrataScale.FOUR,
        ViaFerrataScale.FIVE,
        ViaFerrataScale.SIX
    ).toItems()

    // todo: quest_viaFerrataScale_hint text is missing!

    override val itemsPerRow = 1
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_smoothness
    }

    override fun onClickOk(selectedItems: List<ViaFerrataScale>) {
        applyAnswer(selectedItems.first())
    }
}
