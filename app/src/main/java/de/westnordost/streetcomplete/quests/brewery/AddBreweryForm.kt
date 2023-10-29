package de.westnordost.streetcomplete.quests.brewery

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBrewerySuggestionBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.mostCommonWithin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddBreweryForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_brewery_suggestion
    private val binding by contentViewBinding(QuestBrewerySuggestionBinding::bind)

    private val breweries = mutableSetOf<String>()

    private val brewery get() = binding.breweryInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (suggestions.isEmpty()) { // load suggestions
            requireContext().assets.open("brewery/brewerySuggestions.txt").bufferedReader()
                .lineSequence().forEach { if (it.isNotBlank()) suggestions.add(it.trim().intern()) }
        }

        binding.breweryInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                (lastPickedAnswers + suggestions).distinct()
            )
        )
        binding.breweryInput.onItemClickListener = AdapterView.OnItemClickListener { _, t, _, _ ->
            val brewery = (t as? TextView)?.text?.toString() ?: return@OnItemClickListener
            if (!breweries.add(brewery)) return@OnItemClickListener // we don't want duplicates
            onAddedBrewery(brewery)
        }

        binding.breweryInput.doAfterTextChanged { checkIsFormComplete() }
        binding.breweryInput.doOnLayout { binding.breweryInput.dropDownWidth = binding.breweryInput.width - requireContext().dpToPx(60).toInt() }

        binding.addBreweryButton.setOnClickListener {
            if (!isFormComplete() || binding.breweryInput.text.isBlank()) return@setOnClickListener
            breweries.add(brewery)
            onAddedBrewery(brewery)
        }
        viewLifecycleScope.launch {
            delay(20) // delay, because otherwise dropdown is not anchored at the correct view
            binding.breweryInput.showDropDown()
        }
    }

    override fun onClickOk() {
        breweries.removeAll { it.isBlank() }
        if (breweries.isNotEmpty()) favs.add(breweries)
        if (brewery.isNotBlank()) favs.add(brewery)
        if (brewery.isBlank())
            applyAnswer(breweries.joinToString(";"))
        else
            applyAnswer((breweries + listOf(brewery)).joinToString(";"))
    }

    override fun isFormComplete() = (brewery.isNotBlank() || breweries.isNotEmpty()) && !brewery.contains(";") && !breweries.contains(brewery)

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
        )
    }

    private fun onAddedBrewery(brewery: String) {
        binding.currentBreweries.text = breweries.joinToString(";")
        binding.breweryInput.text.clear()
        (binding.breweryInput.adapter as ArrayAdapter<String>).remove(brewery)
        viewLifecycleScope.launch {
            delay(30) // delay, because otherwise dropdown disappears immediately (also the remove apparently is done in background, so it needs some time)
            binding.breweryInput.showDropDown()
        }
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 20, historyCount = 50, first = 1)
            .toList()
    }

    companion object {
        private val suggestions = mutableListOf<String>()
    }
}
