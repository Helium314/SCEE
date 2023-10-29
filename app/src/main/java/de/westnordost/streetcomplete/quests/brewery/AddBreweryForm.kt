package de.westnordost.streetcomplete.quests.brewery

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.AMultiValueQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.math.enlargedBy
import org.koin.android.ext.android.inject

class AddBreweryForm : AMultiValueQuestForm() {

    private val mapDataSource: MapDataWithEditsSource by inject()

    override fun getConstantSuggestions() =
        requireContext().assets.open("brewery/brewerySuggestions.txt").bufferedReader().readLines()

    override val addAnotherValueResId = R.string.quest_brewery_add_more

    override fun getVariableSuggestions(): Collection<String> {
        val data = mapDataSource.getMapDataWithGeometry(geometry.getBounds().enlargedBy(100.0))
        val suggestions = hashSetOf<String>()
        data.filter("nodes, ways with brewery").forEach {
            it.tags["brewery"]?.let { suggestions.addAll(it.split(";")) }
        }
        suggestions.remove("yes")
        suggestions.remove("various")
        return suggestions
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_brewery_is_various) {
            applyAnswer("various")
            true
        }
    )
}
