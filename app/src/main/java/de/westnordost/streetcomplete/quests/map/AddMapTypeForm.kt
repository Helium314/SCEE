package de.westnordost.streetcomplete.quests.map

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.map.MapType
import de.westnordost.streetcomplete.quests.via_ferrata_scale.toItems
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddMapTypeForm : AImageListQuestForm<MapType, MapType>() {

    override val items: List<DisplayItem<MapType>> get() = listOf(
        MapType.ZERO,
        MapType.ONE,
        MapType.TWO,
        MapType.THREE
    ).toItems()

    override val itemsPerRow = 1
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_via_ferrata_scale
    }

    override fun onClickOk(selectedItems: List<MapType>) {
        applyAnswer(selectedItems.first())
    }
}


