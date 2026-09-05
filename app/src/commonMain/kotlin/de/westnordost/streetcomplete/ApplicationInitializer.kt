package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.ApplicationConstants.MAX_UNDO_HISTORY_AGE
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.PeriodicCleaner
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.screens.settings.renameUpdatedQuests
import de.westnordost.streetcomplete.screens.settings.renamedQuests
import de.westnordost.streetcomplete.util.TempLogger
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.logs.KermitLogger
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Common things on all platforms that are done at the start of the application. */
class ApplicationInitializer(
    private val preloader: Preloader,
    private val feedsUpdater: FeedsUpdater,
    private val editHistoryController: EditHistoryController,
    private val databaseLogger: DatabaseLogger,
    private val downloadedTilesController: DownloadedTilesController,
    private val prefs: Preferences,
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater,
) {
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("Application"))

    fun initialize() {

        setLoggerInstances()

        resurveyIntervalsUpdater.update()

        feedsUpdater.updateNow()

        scope.launch(Dispatchers.IO) {
            editHistoryController.deleteSyncedOlderThan(nowAsEpochMilliseconds() - MAX_UNDO_HISTORY_AGE)
        }

        scope.launch {
            preloader.preload()
        }

        val lastVersion = prefs.lastDataVersion
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.lastDataVersion = BuildConfig.VERSION_NAME
            if (lastVersion != null) onNewVersion()
            // update prefs referring to renamed quests
            val prefsToRename = Prefs.sharedPreferences.all.filter { pref ->
                val v = pref.value
                renamedQuests.keys.any { pref.key.contains(it) || (v is String && v.contains(it)) }
            }
            val e = Prefs.sharedPreferences.edit()
            prefsToRename.forEach {
                e.remove(it.key)
                when (it.value) {
                    is String -> e.putString(it.key.renameUpdatedQuests(), (it.value as String).renameUpdatedQuests())
                    is Boolean -> e.putBoolean(it.key.renameUpdatedQuests(), it.value as Boolean)
                    is Int -> e.putInt(it.key.renameUpdatedQuests(), it.value as Int)
                    is Long -> e.putLong(it.key.renameUpdatedQuests(), it.value as Long)
                    is Float -> e.putFloat(it.key.renameUpdatedQuests(), it.value as Float)
                    is Set<*> -> e.putStringSet(it.key.renameUpdatedQuests(), it.value as? Set<String>?)
                }
            }
            e.apply()
        }
    }

    private fun onNewVersion() {
        scope.launch {
            withContext(Dispatchers.IO) { downloadedTilesController.invalidateAll() }
        }
    }

    private fun setLoggerInstances() {
        Log.instances.add(KermitLogger())
        if (prefs.getBoolean(Prefs.TEMP_LOGGER, false))
            Log.instances.add(TempLogger)
        else
            Log.instances.add(databaseLogger)
    }
}
