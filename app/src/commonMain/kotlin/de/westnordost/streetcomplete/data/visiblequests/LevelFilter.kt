package de.westnordost.streetcomplete.data.visiblequests

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.Mockable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Controller for filtering all quests that are hidden because they are on the wrong level */
@Mockable
class LevelFilter internal constructor(private val prefs: ObservableSettings) : KoinComponent {
    var isEnabled = false
        set(value) {
            if (field == value) return
            field = value
            reload()
        }
    var allowedLevel: String? = null
//        private set
    lateinit var allowedLevelTags: Set<String>
//        private set

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()
    private val visibleEditTypeController: VisibleEditTypeController by inject()

    init { reload() }

    fun reload() {
        allowedLevel = prefs.getString(Prefs.ALLOWED_LEVEL, "").let { if (it.isBlank()) null else it.trim() }
        allowedLevelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref").split(",").toHashSet()

        val overlayController = selectedOverlaySource as? SelectedOverlayController
        val tempOverlay = overlayController?.selectedOverlay
        if (tempOverlay != null) {
            // reload overlay (if enabled), also triggers quest reload unless HIDE_OVERLAY_QUESTS disabled
            overlayController.selectedOverlay = null
            overlayController.selectedOverlay = tempOverlay
            if (!prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true))
                visibleEditTypeController.setVisibilities(emptyMap()) // trigger reload
        } else {
            visibleEditTypeController.setVisibilities(emptyMap()) // trigger reload
        }
    }

    fun isVisible(quest: Quest): Boolean =
        !isEnabled || when (quest) {
            is OsmQuest -> levelAllowed(mapDataSource.get(quest.elementType, quest.elementId))
            is ExternalSourceQuest -> levelAllowed(quest.elementKey?.let { mapDataSource.get(it.type, it.id) })
            else -> true
        }

    fun levelAllowed(element: Element?): Boolean {
        if (!isEnabled) return true
        val tags = element?.tags ?: return true
        val levelTags = tags.filterKeys { allowedLevelTags.contains(it) }
        if (levelTags.isEmpty()) return allowedLevel == null
        val allowedLevel = allowedLevel ?: return false
        levelTags.values.forEach { value ->
            val levels = value.split(";")
            if (allowedLevel == "*") return true // we have anything in an allowed tag, that's enough
            if (allowedLevel.startsWith('<')) {
                val maxLevel = allowedLevel.substring(1).trim().toFloatOrNull()
                if (maxLevel != null)
                    levels.forEach { level ->
                        level.toFloatOrNull()?.let { if (it < maxLevel) return true }
                    }
            }
            if (allowedLevel.startsWith('>')) {
                val minLevel = allowedLevel.substring(1).trim().toFloatOrNull()
                if (minLevel != null)
                    levels.forEach { level ->
                        level.toFloatOrNull()?.let { if (it > minLevel) return true }
                    }
            }
            if (levels.contains(allowedLevel)) return true
            if (value == allowedLevel) return true // maybe user entered 0;1
        }
        when (tags["location"]) {
            "underground" -> return allowedLevel == "<0" || allowedLevel == "< 0"
            "overhead", "roof", "rooftop", "bridge", "pole" -> return allowedLevel == ">0" || allowedLevel == "> 0"
            "overground", "surface" -> allowedLevel == "0"
        }
        return false
    }
}
