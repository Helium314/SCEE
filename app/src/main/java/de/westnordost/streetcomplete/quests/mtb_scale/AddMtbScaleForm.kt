package de.westnordost.streetcomplete.quests.mtb_scale

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddMtbScaleForm : AImageListQuestForm<MtbScale, MtbScale>() {

    override val items: List<DisplayItem<MtbScale>> get() = MtbScale.entries.toItems()

    override val itemsPerRow = 1
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_via_mtb_scale
    }

    override fun onClickOk(selectedItems: List<MtbScale>) {
        applyAnswer(selectedItems.first())
    }
}
