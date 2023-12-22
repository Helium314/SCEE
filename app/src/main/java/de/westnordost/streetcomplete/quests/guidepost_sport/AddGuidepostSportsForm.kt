package de.westnordost.streetcomplete.quests.guidepost_sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item

class AddGuidepostSportsForm : AImageListQuestForm<List<GuidepostSport>, GuidepostSportsAnswer>() {

    override val descriptionResId = R.string.quest_recycling_materials_note

    override val items get() = GuidepostSport.selectableValues.map { it.asItem() }
    override val itemsPerRow = 3

    override val maxSelectableItems = -1

    private fun showPickItemForItemAtIndexDialog(index: Int, items: List<Item<List<GuidepostSport>>>) {
        val ctx = context ?: return
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 3) { selected ->
            val newList = imageSelector.items.toMutableList()
            newList[index] = selected
            imageSelector.items = newList
        }.show()
    }

    override fun onClickOk(selectedItems: List<List<GuidepostSport>>) {
        val answer = SelectedGuidepostSports(selectedItems.flatten())
        applyAnswer(answer)
    }
}
