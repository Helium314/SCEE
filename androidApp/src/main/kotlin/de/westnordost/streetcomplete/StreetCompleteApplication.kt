package de.westnordost.streetcomplete

import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.app.Application
import android.content.ComponentCallbacks2
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseConfigurator
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.screens.settings.LAST_KNOWN_DB_VERSION
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.Locale

class StreetCompleteApplication : Application() {

    private val crashReportsUncaughtExceptionHandler: CrashReportsUncaughtExceptionHandler by inject()
    private val prefs: Preferences by inject()
    private val cacheTrimmer: CacheTrimmer by inject()
    private val applicationInitializer: ApplicationInitializer by inject()

    private val settingsListeners = mutableListOf<SettingsListener>()

    override fun onCreate() {
        super.onCreate()

        // got a crash report where prefs were not initialized, not sure how this can happen for a
        // single person and not for everyone, but this should help (means that we keep using android-specific prefs interface)
        Prefs.sharedPreferences = getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
        ApplicationConstants.DEBUG = packageName.endsWith(".debug")

        startKoin {
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(androidModule, commonModule)
        }

        Prefs.preferences = prefs
        require(StreetCompleteDatabaseConfigurator.version == LAST_KNOWN_DB_VERSION.toInt()) { "update database import/export" }

        crashReportsUncaughtExceptionHandler.install()

        applicationInitializer.initialize()

        updateDefaultLocales()
        updateTheme(prefs.theme)

        settingsListeners += prefs.onLanguageChanged { updateDefaultLocales() }
        settingsListeners += prefs.onThemeChanged { updateTheme(it) }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // very low on memory -> drop caches
                cacheTrimmer.clearCaches()
                Log.i("StreetCompleteApplication", "onTrimMemory, level $level: ${getMemString()}")
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // memory needed, but not critical -> trim only
                Log.i("StreetCompleteApplication", "onTrimMemory, level $level: ${getMemString()}")
                cacheTrimmer.trimCaches()
            }
        }
    }

    private fun updateDefaultLocales() {
        val locales = getSelectedLocales(prefs)
        Locale.setDefault(locales.get(0))
        LocaleList.setDefault(getSelectedLocales(prefs))
    }

    private fun getMemString(): String {
        val memInfo = MemoryInfo()
        getSystemService<ActivityManager>()?.getMemoryInfo(memInfo)
        return "${memInfo.availMem / 0x100000L} MB of ${memInfo.totalMem / 0x100000L} available, mem low: ${memInfo.lowMemory}, mem low threshold: ${memInfo.threshold / 0x100000L} MB"
    }

    private fun updateTheme(theme: Theme) {
        if (theme == Theme.DARK_CONTRAST || theme == Theme.DARK)
            // night mode off to trigger reload (maybe there is a way to do it without this, but at least ir works...)
            AppCompatDelegate.setDefaultNightMode(Theme.LIGHT.appCompatNightMode)
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
    }
}

private val Theme.appCompatNightMode: Int get() = when (this) {
    Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    Theme.DARK, Theme.DARK_CONTRAST -> AppCompatDelegate.MODE_NIGHT_YES
    Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
