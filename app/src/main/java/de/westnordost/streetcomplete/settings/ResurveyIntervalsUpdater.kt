package de.westnordost.streetcomplete.settings

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.DEFAULT
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.EVEN_LESS_OFTEN
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.LESS_OFTEN
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.MORE_OFTEN
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.valueOf
import de.westnordost.streetcomplete.data.elementfilter.filters.CompareTagAge
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.meta.toCheckDate

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
class ResurveyIntervalsUpdater(private val prefs: SharedPreferences) {
    fun update() {
        RelativeDate.MULTIPLIER = multiplier
        // parse date from prefs, null if bad format
        CompareTagAge.resurveyDate = prefs.getString(Prefs.RESURVEY_DATE, "")?.toCheckDate()
        // set element filter list
        CompareTagAge.resurveyKeys = prefs.getString(Prefs.RESURVEY_KEYS, "")!!.split(",").map { it.trim() }
        // plus: on change of any of those two, call this update function!
    }

    private val multiplier: Float get() = when (intervalsPreference) {
        EVEN_LESS_OFTEN -> 4.0f
        LESS_OFTEN -> 2.0f
        DEFAULT -> 1.0f
        MORE_OFTEN -> 0.5f
    }

    private val intervalsPreference: Prefs.ResurveyIntervals get() =
        valueOf(prefs.getString(Prefs.RESURVEY_INTERVALS, "DEFAULT")!!)
}
