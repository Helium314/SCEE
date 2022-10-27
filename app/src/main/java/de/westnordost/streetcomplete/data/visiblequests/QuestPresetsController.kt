package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.concurrent.CopyOnWriteArrayList

class QuestPresetsController(
    private val questPresetsDao: QuestPresetsDao,
    private val selectedQuestPresetStore: SelectedQuestPresetStore,
    private val questTypeOrderDao: QuestTypeOrderDao,
    private val visibleQuestTypeDao: VisibleQuestTypeDao,
    private val prefs: SharedPreferences,
) : QuestPresetsSource {

    private val listeners = CopyOnWriteArrayList<QuestPresetsSource.Listener>()

    override var selectedId: Long
        get() = selectedQuestPresetStore.get()
        set(value) {
            selectedQuestPresetStore.set(value)
            onSelectedQuestPresetChanged()
        }

    override val selectedQuestPresetName: String? get() =
        questPresetsDao.getName(selectedId)

    fun add(presetName: String): Long {
        val presetId = questPresetsDao.add(presetName)
        onAddedQuestPreset(presetId, presetName)
        return presetId
    }

    override fun getName(presetId: Long): String? {
        return questPresetsDao.getName(presetId)
    }

    fun rename(presetId: Long, name: String) {
        questPresetsDao.rename(presetId, name)
        onRenamedQuestPreset(presetId, name)
    }

    fun add(presetName: String, copyFromId: Long): Long {
        val presetId = questPresetsDao.add(presetName)
        val order = questTypeOrderDao.getAll(copyFromId)
        questTypeOrderDao.setAll(presetId, order)
        val visibilities = visibleQuestTypeDao.getAll(copyFromId)
        visibleQuestTypeDao.putAll(presetId, visibilities)
        onAddedQuestPreset(presetId, presetName)

        val copyFromQuestSettings = prefs.all.filterKeys { it.startsWith("${copyFromId}_qs_") }
        prefs.edit {
            copyFromQuestSettings.forEach { (key, value) ->
                val newKey = key.replace("${copyFromId}_qs_", "${presetId}_qs_")
                when (value) {
                    is Boolean -> putBoolean(newKey, value)
                    is Int -> putInt(newKey, value)
                    is String -> putString(newKey, value)
                    is Long -> putLong(newKey, value)
                    is Float -> putFloat(newKey, value)
                    is Set<*> -> putStringSet(newKey, value.toSet() as? Set<String>)
                }
            }
        }
        return presetId
    }

    fun delete(presetId: Long) {
        if (presetId == selectedId) {
            selectedId = 0
        }
        questPresetsDao.delete(presetId)
        val presetSettings = prefs.all.keys.filter { it.startsWith("${presetId}_qs_") }
        presetSettings.forEach { prefs.edit().remove(it).apply() }
        onDeletedQuestPreset(presetId)
    }

    override fun getAll(): List<QuestPreset> =
        questPresetsDao.getAll()

    override fun getByName(name: String): QuestPreset? =
        questPresetsDao.getAll().find { it.name == name }

    /* listeners */

    override fun addListener(listener: QuestPresetsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestPresetsSource.Listener) {
        listeners.remove(listener)
    }
    private fun onSelectedQuestPresetChanged() {
        listeners.forEach { it.onSelectedQuestPresetChanged() }
    }
    private fun onAddedQuestPreset(presetId: Long, presetName: String) {
        listeners.forEach { it.onAddedQuestPreset(QuestPreset(presetId, presetName)) }
    }
    private fun onRenamedQuestPreset(presetId: Long, presetName: String) {
        listeners.forEach { it.onRenamedQuestPreset(QuestPreset(presetId, presetName)) }
    }
    private fun onDeletedQuestPreset(presetId: Long) {
        listeners.forEach { it.onDeletedQuestPreset(presetId) }
    }
}
