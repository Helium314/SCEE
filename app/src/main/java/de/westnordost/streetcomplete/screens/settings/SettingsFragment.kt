package de.westnordost.streetcomplete.screens.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.quest.QuestController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.*
import de.westnordost.streetcomplete.databinding.DialogDeleteCacheBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.measure.MeasureActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowLinksActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsActivity
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.purge
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

/** Shows the settings screen */
class SettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by inject()
    private val downloadedTilesDao: DownloadedTilesDao by inject()
    private val noteController: NoteController by inject()
    private val mapDataController: MapDataController by inject()
    private val questController: QuestController by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestTypeSource: VisibleQuestTypeSource by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val  visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val db: Database by inject()

    interface Listener {
        fun onClickedQuestSelection()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override val title: String get() = getString(R.string.action_settings)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("quests")?.setOnPreferenceClickListener {
            listener?.onClickedQuestSelection()
            true
        }

        findPreference<Preference>("quests.advanced_resurvey")?.setOnPreferenceClickListener {
            val layout = LinearLayout(context)
            layout.setPadding(30,10,30,10)
            layout.orientation = LinearLayout.VERTICAL
            val keyText = TextView(context)
            keyText.text = "Always resurvey these keys if there is no check date key"
            val keyEditText = EditText(context)
            keyEditText.inputType = InputType.TYPE_CLASS_TEXT
            keyEditText.hint = "separate multiple keys using ,"
            keyEditText.setText(prefs.getString(Prefs.RESURVEY_KEYS, ""))

            val dateText = TextView(context)
            dateText.text = "Instead of using resurvey intervals, resurvey above keys if check date older than:"
            val dateEditText = EditText(context)
            dateEditText.inputType = InputType.TYPE_CLASS_TEXT
            dateEditText.hint = "format must be YYYY-MM-DD"
            dateEditText.setText(prefs.getString(Prefs.RESURVEY_DATE, ""))

            layout.addView(keyText)
            layout.addView(keyEditText)
            layout.addView(dateText)
            layout.addView(dateEditText)

            AlertDialog.Builder(requireContext())
                .setTitle("Set advances resurvey date and keys")
                .setView(layout)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.edit {
                        putString(Prefs.RESURVEY_DATE, dateEditText.text.toString())
                        putString(Prefs.RESURVEY_KEYS, keyEditText.text.toString())
                    }
                    resurveyIntervalsUpdater.update()
                }
             .show()
            true
        }

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1)
            )
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setNeutralButton(R.string.delete_confirmation_both) { _, _ -> lifecycleScope.launch {
                    deleteTiles()
                    deleteCache()}
                }
                .setPositiveButton(R.string.delete_tiles_confirmation) { _, _ -> lifecycleScope.launch { deleteTiles() }}
                .setNegativeButton(R.string.delete_data_confirmation) { _, _ -> lifecycleScope.launch { deleteCache() }}
                .show()
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_dialog_message)
                .setPositiveButton(R.string.restore_confirmation) { _, _ -> lifecycleScope.launch {
                    val hidden = questController.unhideAll()
                    context?.toast(getString(R.string.restore_hidden_success, hidden), Toast.LENGTH_LONG)
                } }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<Preference>("ignore_notes")?.setOnPreferenceClickListener {
            val text = EditText(context)
            text.setText(prefs.getStringSet(Prefs.DONT_SHOW_NOTES_FROM_THESE_USERS, emptySet())?.joinToString(","))
            text.hint = "use comma to separate user names / ids"
            text.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            val layout = LinearLayout(context)
            layout.setPadding(30,10,30,10)
            layout.addView(text)

            AlertDialog.Builder(requireContext())
                .setTitle("enter user ids or names to ignore")
                .setView(layout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val content = text.text.split(",").map { it.trim().lowercase() }.toSet()
                    prefs.edit().putStringSet(Prefs.DONT_SHOW_NOTES_FROM_THESE_USERS, content).apply()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }

        findPreference<Preference>("debug.links")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowLinksActivity::class.java))
            true
        }

        findPreference<Preference>("debug.ar_measure_horizontal")?.setOnPreferenceClickListener {
            startActivity(MeasureActivity.createIntent(requireContext(), false))
            true
        }

        findPreference<Preference>("debug.ar_measure_vertical")?.setOnPreferenceClickListener {
            startActivity(MeasureActivity.createIntent(requireContext(), true))
            true
        }

        findPreference<Preference>("export")?.setOnPreferenceClickListener {
            fun export(name: String) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, "$name.txt")
                    type = "application/text"
                }
                val requestCode = when (name) {
                    "settings" -> REQUEST_CODE_SETTINGS_EXPORT
                    "hidden_quests" ->  REQUEST_CODE_HIDDEN_EXPORT
                    "presets" ->  REQUEST_CODE_PROFILES_EXPORT
                    else -> 0
                }
                startActivityForResult(intent, requestCode)
            }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_export)
                .setNegativeButton(R.string.import_export_settings) { _,_ -> export("settings") }
                .setNeutralButton(R.string.import_export_hidden_quests) { _,_ -> export("hidden_quests") }
                .setPositiveButton(R.string.import_export_profiles)  { _,_ -> export("presets") }
                .show()

            true
        }

        findPreference<Preference>("import")?.setOnPreferenceClickListener {
            fun import(name: String) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*" // can't select text file if setting to application/text
                }
                val requestCode = when (name) {
                    "settings" -> REQUEST_CODE_SETTINGS_IMPORT
                    "hidden_quests" ->  REQUEST_CODE_HIDDEN_IMPORT
                    "presets" ->  REQUEST_CODE_PROFILES_IMPORT
                    else -> 0
                }
                startActivityForResult(intent, requestCode)
            }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_import)
                .setMessage(R.string.import_warning)
                .setNegativeButton(R.string.import_export_settings) { _,_ -> import("settings") }
                .setNeutralButton(R.string.import_export_hidden_quests) { _,_ -> import("hidden_quests") }
                .setPositiveButton(R.string.import_export_profiles)  { _,_ -> import("presets") }
                .show()

            true
        }

        buildLanguageSelector()
    }

    private fun buildLanguageSelector() {
        val entryValues = resources.getYamlObject<MutableList<String>>(R.raw.languages)
        val entries = entryValues.map {
            val locale = Locale.forLanguageTag(it)
            val name = locale.displayName
            val nativeName = locale.getDisplayName(locale)
            return@map nativeName + if (name != nativeName) " — $name" else ""
        }.toMutableList()

        // add default as first element
        entryValues.add(0, "")
        entries.add(0, getString(R.string.language_default))

        findPreference<ListPreference>("language.select")?.also {
            it.entries = entries.toTypedArray()
            it.entryValues = entryValues.toTypedArray()
        }
    }

    override fun onStart() {
        super.onStart()
        findPreference<Preference>("quests")?.summary = getQuestPreferenceSummary()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    @SuppressLint("InflateParams")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Prefs.AUTOSYNC -> {
                if (Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) != Prefs.Autosync.ON) {
                    AlertDialog.Builder(requireContext())
                        .setView(layoutInflater.inflate(R.layout.dialog_tutorial_upload, null))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            Prefs.THEME_SELECT -> {
                val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
                AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
                activity?.let { ActivityCompat.recreate(it) }
            }
            Prefs.LANGUAGE_SELECT -> {
                setDefaultLocales(getSelectedLocales(requireContext()))
                activity?.let { ActivityCompat.recreate(it) }
            }
            Prefs.RESURVEY_INTERVALS -> {
                resurveyIntervalsUpdater.update()
            }
            Prefs.SHOW_QUEST_GEOMETRIES -> {
                visibleQuestTypeController.setAllVisible(listOf(), true) // trigger reload
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DialogPreferenceCompat) {
            val fragment = preference.createDialog()
            fragment.arguments = bundleOf("key" to preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private suspend fun deleteCache() = withContext(Dispatchers.IO) {
        downloadedTilesDao.removeAll()
        mapDataController.clear()
        noteController.clear()
    }

    private suspend fun deleteTiles() = withContext(Dispatchers.IO) {
        context?.externalCacheDir?.purge()
    }

    private fun getQuestPreferenceSummary(): String {
        val presetName = questPresetsSource.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        val hasCustomPresets = questPresetsSource.getAll().isNotEmpty()
        val presetStr = if (hasCustomPresets) getString(R.string.pref_subtitle_quests_preset_name, presetName) + "\n" else ""

        val enabledCount = questTypeRegistry.filter { visibleQuestTypeSource.isVisible(it) }.count()
        val totalCount = questTypeRegistry.size
        val enabledStr = getString(R.string.pref_subtitle_quests, enabledCount, totalCount)

        return presetStr + enabledStr
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return
        when (requestCode) {
            REQUEST_CODE_SETTINGS_EXPORT -> {
                val f = File(context?.applicationInfo?.dataDir + File.separator + "shared_prefs" + File.separator + context?.applicationInfo?.packageName + "_preferences.xml")
                val uri = data.data ?: return
                if (!f.exists()) return
                val os = activity?.contentResolver?.openOutputStream(uri)?.bufferedWriter() ?: return
                val lines = f.readLines().filterNot {
                    it.contains("TangramPinsSpriteSheet") || it.contains("oauth.") || it.contains("osm.")
                }
                os.write(lines.joinToString("\n"))
                os.close()
                // there is some SharedPreferencesBackupHelper, but can't access this without some app backup thing apparently
            }
            REQUEST_CODE_HIDDEN_EXPORT -> {
                val uri = data.data ?: return
                val os = activity?.contentResolver?.openOutputStream(uri)?.bufferedWriter() ?: return
                val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()

                val hiddenQuests = db.query(OsmQuestsHiddenTable.NAME) { c ->
                    c.getLong(OsmQuestsHiddenTable.Columns.ELEMENT_ID).toString() + "," +
                        c.getString(OsmQuestsHiddenTable.Columns.ELEMENT_TYPE) + "," +
                        c.getString(OsmQuestsHiddenTable.Columns.QUEST_TYPE) + "," +
                        c.getLong(OsmQuestsHiddenTable.Columns.TIMESTAMP)
                }
                val hiddenNotes = db.query(NoteQuestsHiddenTable.NAME) { c->
                    c.getLong(NoteQuestsHiddenTable.Columns.NOTE_ID).toString() + "," +
                        c.getLong(NoteQuestsHiddenTable.Columns.TIMESTAMP)
                }

                os.use {
                    it.write(version.toString())
                    it.write("\nquests\n")
                    it.write(hiddenQuests.joinToString("\n"))
                    it.write("\nnotes\n")
                    it.write(hiddenNotes.joinToString("\n"))
                }
                os.close()
            }
            REQUEST_CODE_PROFILES_EXPORT -> {
                val uri = data.data ?: return
                val os = activity?.contentResolver?.openOutputStream(uri)?.bufferedWriter() ?: return
                val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()

                val presets = db.query(QuestPresetsTable.NAME) { c ->
                    c.getLong(QuestPresetsTable.Columns.QUEST_PRESET_ID).toString() + "," +
                        c.getString(QuestPresetsTable.Columns.QUEST_PRESET_NAME)
                }
                val orders = db.query(QuestTypeOrderTable.NAME) { c->
                    c.getLong(QuestTypeOrderTable.Columns.QUEST_PRESET_ID).toString() + "," +
                        c.getString(QuestTypeOrderTable.Columns.BEFORE) + "," +
                        c.getString(QuestTypeOrderTable.Columns.AFTER)
                }
                val visibities = db.query(VisibleQuestTypeTable.NAME) { c ->
                    c.getLong(VisibleQuestTypeTable.Columns.QUEST_PRESET_ID).toString() + "," +
                        c.getString(VisibleQuestTypeTable.Columns.QUEST_TYPE) + "," +
                        c.getLong(VisibleQuestTypeTable.Columns.VISIBILITY).toString()
                }

                os.use {
                    it.write(version.toString())
                    it.write("\npresets\n")
                    it.write(presets.joinToString("\n"))
                    it.write("\norders\n")
                    it.write(orders.joinToString("\n"))
                    it.write("\nvisibilities\n")
                    it.write(visibities.joinToString("\n"))
                }
                os.close()
            }
            REQUEST_CODE_SETTINGS_IMPORT -> {
                // how to make sure shared prefs are re-read from the new file?
                // probably need to restart app on import
                val f = File(context?.applicationInfo?.dataDir + File.separator + "shared_prefs" + File.separator + context?.applicationInfo?.packageName + "_preferences.xml")
                val uri = data.data ?: return
                val inputStream = activity?.contentResolver?.openInputStream(uri) ?: return
                val t = inputStream.reader().readText()
                if (!t.startsWith("<?xml version")) {
                    context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                    return
                }
                f.writeText(t)

                restartApp()
            }
            REQUEST_CODE_HIDDEN_IMPORT -> {
                // do not delete existing hidden quests
                val uri = data.data ?: return
                val input = activity?.contentResolver?.openInputStream(uri)?.bufferedReader() ?: return
                val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
                if (input.readLine() != version.toString()) return // TODO: handle this once the version changes
                if (input.readLine() != "quests") {
                    context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                    return
                }
                val lines = input.readLines()

                val quests = mutableListOf<Array<Any?>>()
                val notes = mutableListOf<Array<Any?>>()
                var currentThing = "quests"
                for (line in lines) {
                    if (line == "notes") {
                        currentThing = "notes"
                        continue
                    }
                    val split = line.split(",")
                    if (split.size < 2) break
                    when (currentThing) {
                        "quests" -> quests.add(arrayOf(split[0].toLong(), split[1], split[2], split[3].toLong()))
                        "notes" -> notes.add(arrayOf(split[0].toLong(), split[1].toLong()))
                    }
                }

                db.insertMany(OsmQuestsHiddenTable.NAME,
                    arrayOf(OsmQuestsHiddenTable.Columns.ELEMENT_ID,
                        OsmQuestsHiddenTable.Columns.ELEMENT_TYPE,
                        OsmQuestsHiddenTable.Columns.QUEST_TYPE,
                        OsmQuestsHiddenTable.Columns.TIMESTAMP),
                    quests,
                    conflictAlgorithm = ConflictAlgorithm.REPLACE
                )
                db.insertMany(NoteQuestsHiddenTable.NAME,
                    arrayOf(NoteQuestsHiddenTable.Columns.NOTE_ID,
                        NoteQuestsHiddenTable.Columns.TIMESTAMP),
                    notes,
                    conflictAlgorithm = ConflictAlgorithm.REPLACE
                )

                // definitely need to reset visible quests
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
                // maybe more?
            }
            REQUEST_CODE_PROFILES_IMPORT -> {
                val uri = data.data ?: return
                val input = activity?.contentResolver?.openInputStream(uri)?.bufferedReader() ?: return
                val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
                if (input.readLine() != version.toString()) return // TODO: handle this once the version changes
                if (input.readLine() != "presets") {
                    context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                    return
                }
                val lines = input.readLines()

                val presets = mutableListOf<Array<Any?>>()
                val orders = mutableListOf<Array<Any?>>()
                val visibilities = mutableListOf<Array<Any?>>()
                var currentThing = "presets"
                for (line in lines) {
                    when (line) {
                        "orders" -> {currentThing = "orders"; continue}
                        "visibilities" -> {currentThing = "visibilities"; continue}
                    }
                    val split = line.split(",")
                    if (split.size < 2) break
                    when (currentThing) {
                        "presets" -> presets.add(arrayOf(split[0].toLong(), split[1]))
                        "orders" -> orders.add(arrayOf(split[0].toLong(), split[1], split[2]))
                        "visibilities" -> visibilities.add(arrayOf(split[0].toLong(), split[1], split[2].toLong()))
                    }
                }

                // delete existing data in both tables
                db.delete(QuestPresetsTable.NAME)
                db.delete(QuestTypeOrderTable.NAME)
                db.delete(VisibleQuestTypeTable.NAME)

                db.insertMany(QuestPresetsTable.NAME,
                    arrayOf(QuestPresetsTable.Columns.QUEST_PRESET_ID, QuestPresetsTable.Columns.QUEST_PRESET_NAME),
                    presets
                )
                db.insertMany(QuestTypeOrderTable.NAME,
                    arrayOf(QuestTypeOrderTable.Columns.QUEST_PRESET_ID,
                        QuestTypeOrderTable.Columns.BEFORE,
                        QuestTypeOrderTable.Columns.AFTER),
                    orders
                )
                db.insertMany(VisibleQuestTypeTable.NAME,
                    arrayOf(VisibleQuestTypeTable.Columns.QUEST_PRESET_ID,
                        VisibleQuestTypeTable.Columns.QUEST_TYPE,
                        VisibleQuestTypeTable.Columns.VISIBILITY),
                    visibilities
                )

                // set selected preset to default
                prefs.edit().putLong(Prefs.SELECTED_QUESTS_PRESET, 0).apply()
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
                // TODO: also allow adding the profile with new id(s)
            }
        }
    }

    private fun restartApp() {
        val i = Intent(requireContext(), this.javaClass)
        val pi = PendingIntent.getActivity(requireContext(), 234, i, PendingIntent.FLAG_CANCEL_CURRENT)
        val a = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // according to stackoverflow, restart does not work due to some restrictions added in Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setTitle(R.string.restart_required)
                .setMessage(R.string.restart_required_message)
                .setPositiveButton(android.R.string.ok) { _,_ ->
                    a.set(AlarmManager.RTC, System.currentTimeMillis() + 50, pi)
                    exitProcess(0)
                }
                .show()
        } else {
            a.set(AlarmManager.RTC, System.currentTimeMillis() + 50, pi)
            exitProcess(0)
        }
    }

}

private const val REQUEST_CODE_SETTINGS_EXPORT = 532527
private const val REQUEST_CODE_HIDDEN_EXPORT = 532528
private const val REQUEST_CODE_PROFILES_EXPORT = 532529
private const val REQUEST_CODE_SETTINGS_IMPORT = 67367487
private const val REQUEST_CODE_HIDDEN_IMPORT = 67367488
private const val REQUEST_CODE_PROFILES_IMPORT = 67367489
