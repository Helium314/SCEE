package de.westnordost.streetcomplete.data.visiblequests

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.Quest

/** Controller for filtering all quests that are hidden because they are on the wrong level */
class LevelFilter internal constructor(
    private val sharedPrefs: SharedPreferences,
    private val mapDataController: MapDataController,
    private val visibleQuestTypeController: VisibleQuestTypeController
) {

    private var isEnabled = false
    private var allowedLevel: String? = null
    private lateinit var allowedLevelTags: List<String>

    init {
        reload()
    }

    private fun reload() {
        allowedLevel = sharedPrefs.getString(Prefs.ALLOWED_LEVEL, "").let { if (it.isNullOrBlank()) null else it.trim() }
        allowedLevelTags = sharedPrefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref")!!.split(",")
    }

    fun isVisible(quest: Quest): Boolean =
        !isEnabled ||
            (quest is OsmQuest && quest.levelAllowed())

    private fun OsmQuest.levelAllowed(): Boolean {
        val tags = mapDataController.get(this.elementType, this.elementId)?.tags ?: return true
        val levelTags = tags.filterKeys { allowedLevelTags.contains(it) }
        if (levelTags.isEmpty()) return allowedLevel == null
        if (allowedLevel == null) return false // level tags not empty
        levelTags.values.forEach { value ->
            val levels = value.split(";")
            if (allowedLevel!!.startsWith('<')) {
                val maxLevel = allowedLevel!!.substring(1).trim().toFloatOrNull()
                if (maxLevel != null)
                    levels.forEach { level ->
                        level.toFloatOrNull()?.let { if (it < maxLevel) return true }
                    }
            }
            if (allowedLevel!!.startsWith('>')) {
                val minLevel = allowedLevel!!.substring(1).trim().toFloatOrNull()
                if (minLevel != null)
                    levels.forEach { level ->
                        level.toFloatOrNull()?.let { if (it > minLevel) return true }
                    }
            }
            if (levels.contains(allowedLevel)) return true
            if (value == allowedLevel) return true // maybe user entered 0;1
        }
        return false
    }

    fun showLevelFilterDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose tags to check")
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val levelTags = sharedPrefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref")!!.split(",")

        val levelText = TextView(context)
        levelText.text = "enter level value, or filter using > and <"

        val level = EditText(context)
        level.inputType = InputType.TYPE_CLASS_TEXT
        level.hint = "leave empty to show not tagged"
        level.setText(sharedPrefs.getString(Prefs.ALLOWED_LEVEL, ""))

        val enable = SwitchCompat(context)
        enable.text = "enable level filter"
        enable.isChecked = isEnabled

        val tagLevel = CheckBox(context)
        tagLevel.text = "level"
        tagLevel.isChecked = levelTags.contains("level")

        val tagRepeatOn = CheckBox(context)
        tagRepeatOn.text = "repeat_on"
        tagRepeatOn.isChecked = levelTags.contains("repeat_on")

        val tagLevelRef = CheckBox(context)
        tagLevelRef.text = "level:ref"
        tagLevelRef.isChecked = levelTags.contains("level:ref")

        val tagAddrFloor = CheckBox(context)
        tagAddrFloor.text = "addr:floor"
        tagAddrFloor.isChecked = levelTags.contains("addr:floor")

        linearLayout.addView(tagLevel)
        linearLayout.addView(tagRepeatOn)
        linearLayout.addView(tagLevelRef)
        linearLayout.addView(tagAddrFloor)
        linearLayout.addView(levelText)
        linearLayout.addView(level)
        linearLayout.addView(enable)
        linearLayout.setPadding(30,10,30,10)
        builder.setView(linearLayout)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val levelTagList = mutableListOf<String>()
            if (tagLevel.isChecked) levelTagList.add("level")
            if (tagRepeatOn.isChecked) levelTagList.add("repeat_on")
            if (tagLevelRef.isChecked) levelTagList.add("level:ref")
            if (tagAddrFloor.isChecked) levelTagList.add("addr:floor")
            sharedPrefs.edit {
                putString(Prefs.ALLOWED_LEVEL_TAGS, levelTagList.joinToString(","))
                putString(Prefs.ALLOWED_LEVEL, level.text.toString())
            }
            isEnabled = enable.isChecked
            reload()

            visibleQuestTypeController.setAllVisible(listOf(), true) // trigger reload
        }
        builder.show()
    }

}
