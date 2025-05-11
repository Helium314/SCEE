package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddArtworkTypeForm : AImageListQuestForm<ArtworkType, ArtworkType>() {

    override val items = ArtworkType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ArtworkType>) {
        applyAnswer(selectedItems.single())
    }
}
