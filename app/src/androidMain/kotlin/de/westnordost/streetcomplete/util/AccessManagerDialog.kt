package de.westnordost.streetcomplete.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.databinding.DialogAccessManagerBinding
import de.westnordost.streetcomplete.databinding.RowAccessBinding
import de.westnordost.streetcomplete.util.dialogs.showAddConditionalDialog
import de.westnordost.streetcomplete.util.ktx.dpToPx

class AccessManagerDialog(
    context: Context,
    tags: Map<String, String>,
    onClickOk: (StringMapChangesBuilder) -> Unit
) : AlertDialog(context) {

    private val binding = DialogAccessManagerBinding.inflate(LayoutInflater.from(context))

    // original tags filtered to access keys, but parsed into sets
    private val originalAccessTagsSets: Map<String, Set<String>> =
        tags.filterKeys { key -> accessKeys.any { it == key || key.startsWith("$it:") } }
            .mapValues { (_, v) -> parseValues(v) }
    // working copy: mutable sets we update from UI
    private val newAccessTags: MutableMap<String, MutableSet<String>> =
        LinkedHashMap(originalAccessTagsSets.mapValues { (_, v) -> v.toMutableSet() })

    init {
        binding.addConditionalButton.setOnClickListener {
            showAddConditionalDialog(
                context,
                accessKeys.toList(),
                listOf("yes", "no", "delivery", "destination"),
                null
            ) { k, v ->
                // ensure set exists
                val set = newAccessTags.getOrPut(k) { mutableSetOf() }
                set.add(v)
                createAccessTagViews()
            }
        }
        binding.addButton.setOnClickListener { showAddAccessDialog(context) }
        createAccessTagViews()
        setMessage(context.getString(R.string.access_manager_message))
        setView(binding.root)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ -> }
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            val builder = StringMapChangesBuilder(tags)
            newAccessTags.forEach { (k, set) ->
                val joined = serializeValues(set)
                val origJoined = originalAccessTagsSets[k]?.let { serializeValues(it) }
                if (origJoined != joined) {
                    if (joined.isEmpty()) builder.remove(k) else builder[k] = joined
                }
            }
            originalAccessTagsSets.keys.forEach { k ->
                if (k !in newAccessTags || newAccessTags[k].isNullOrEmpty()) builder.remove(k)
            }
            onClickOk(builder)
        }
        setOnShowListener {
            updateOkButton()
        }
    }

    private fun updateOkButton() {
        val origMap = originalAccessTagsSets.mapValues { (_, s) -> serializeValues(s) }
        val newMap = newAccessTags.mapValues { (_, s) -> serializeValues(s) }
        getButton(BUTTON_POSITIVE)?.isEnabled = origMap != newMap
    }

    private fun createAccessTagViews() {
        binding.accessTags.removeAllViews()
        newAccessTags.forEach { (key, set) ->
            binding.accessTags.addView(accessView(key, set))
        }
        updateOkButton()
    }

    private fun accessView(key: String, valuesSet: MutableSet<String>): View {
        val view = RowAccessBinding.inflate(LayoutInflater.from(context))
        view.keyText.text = key

        // Clear any previous dynamic checkbox container if present
        // We assume row_access.xml has a ViewGroup with id 'valueContainer'
        val container = view.valueContainer // provided in XML

        container.removeAllViews()

        // create checkbox for each known accessValue; if the current value is not in accessValues, show it first as checked
        val existing = valuesSet.toMutableSet()
        // Only show currently selected values in the compact list
        val valuesToShow: List<String> = existing.sorted()

        for (valStr in valuesToShow) {
            val check = CheckBox(binding.root.context)
            check.text = valStr
            check.isChecked = valStr in existing
            check.setOnCheckedChangeListener { _, checked ->
                if (!checked) {
                    valuesSet.remove(valStr)
                    if (valuesSet.isEmpty()) {
                        newAccessTags.remove(key)
                    }
                    createAccessTagViews()
                }
                updateOkButton()
            }
            // small padding
            check.setPadding(0, context.resources.dpToPx(2).toInt(), 0, context.resources.dpToPx(2).toInt())
            container.addView(check)
        }

        // delete button removes the whole key
        view.deleteButton.setOnClickListener {
            newAccessTags.remove(key)
            createAccessTagViews()
        }

        view.root.setPadding(0, context.resources.dpToPx(4).toInt(), 0, context.resources.dpToPx(4).toInt())
        return view.root
    }

    // Show dialog to add a new key -> choose key then multi-choice values
    private fun showAddAccessDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.add_access)
            .setSingleChoiceItems(accessKeys, -1) { di, i ->
                val key = accessKeys[i]
                // multi-choice values dialog
                val checked = BooleanArray(accessValues.size)
                Builder(context)
                    .setTitle(R.string.manage_access)
                    .setMultiChoiceItems(accessValues, checked) { _, idx, isChecked ->
                        checked[idx] = isChecked
                    }
                    .setPositiveButton(android.R.string.ok) { di2, _ ->
                        val selected = accessValues
                            .withIndex()
                            .filter { checked[it.index] }
                            .map { it.value }
                        if (selected.isNotEmpty()) {
                            val set = newAccessTags.getOrPut(key) { mutableSetOf() }
                            set.addAll(selected)
                            createAccessTagViews()
                        }
                        di2.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                di.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {

        // helper: parse semicolon separated values into set
        private fun parseValues(v: String): MutableSet<String> =
            v.split(';').map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()

        // serialize set into stable ; separated string
        private fun serializeValues(values: Set<String>): String =
            values.filter { it.isNotBlank() }.toSet().sorted().joinToString(";")
    }
}

// Access keys and values are used in multiple places (dialogs, overlays)
val accessKeys = arrayOf( // sorted by number of uses
    "access", // 18m
    "foot", // 7m
    "bicycle", // 7m
    "bus", // 3.5m
    "motor_vehicle", // 2m
    "horse", // 1.6m
    "hgv", // 790k
    "motorcar", // 590k
    "motorcycle", // 580k
    "vehicle", // 350k
    "moped", // 235k
    "mofa", // 200k
    "golf_cart", // 158k
    "psv", // 115k
    "hazmat", // 87k
    "dog", // 80k
    "bdouble", // 60k
    "ski", // 60k
    "goods", // 41k
    "taxi", // 23k
    "carriage", // 20k
    "hov", // 20k
    "disabled", // 13.5k
    "tourist_bus", // 13k
    "atv", // 12k
    "hand_cart", // 6.8k
    "inline_skates", // 5k
    "speed_pedelec", // 3.7k
    "motorhome", // 3.5k
    "trailer", // 2.7k
    "ohv", // 2.4k
    "caravan", // 2k
    "coach", // 1.7k
    "carpool", // 1.5k
    "hgv_articulated", // 1k
    "small_electric_vehicle", // 800
    "auto_rickshaw", // 625
    "electric_bicycle", // 335
    "cycle_rickshaw", // 78
    "nev", // 62
    "kick_scooter", // 60
)

val accessValues = arrayOf(
    "yes",
    "no",
    "private",
    "permissive",
    "permit",
    "destination",
    "delivery",
    "customers",
    "designated", // not for access
    "use_sidepath", // usually for foot / bicycle
    "dismount", // bicycle
    "agricultural",
    "forestry",
    "discouraged", // really required explicit sign
    //"variable", doesn't make sense without supporting access:lanes
)
