package de.westnordost.streetcomplete.quests.roof_material

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddRoofMaterialForm : AImageListQuestForm<RoofMaterial, RoofMaterial>() {

    override val items = RoofMaterial.entries.map { it.asItem() }

    override val itemsPerRow = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select
    }

    override fun onClickOk(selectedItems: List<RoofMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
