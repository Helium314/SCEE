package de.westnordost.streetcomplete.testutils

import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.data.preferences.Preferences
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock

// mock SharedPreferences that always return default value
fun mockPrefs(): SharedPreferences {
    val prefs: SharedPreferences = mock()
    val string = Capture.slot<String>()
    every { prefs.getString(any(), capture(string)) } returns string.get()
    val int = Capture.slot<Int>()
    every { prefs.getInt(any(), capture(int)) } returns int.get()
    val long = Capture.slot<Long>()
    every { prefs.getLong(any(), capture(long)) } returns long.get()
    // style above doesn't work for boolean ("Cannot cast java.lang.Boolean to boolean")
    every { prefs.getBoolean(any(), true) } returns true
    every { prefs.getBoolean(any(), false) } returns false
    return prefs
}

fun mockPrefs2(): ObservableSettings {
    val prefs: ObservableSettings = mock()
    val string = Capture.slot<String>()
    every { prefs.getString(any(), capture(string)) } returns string.get()
    val int = Capture.slot<Int>()
    every { prefs.getInt(any(), capture(int)) } returns int.get()
    val long = Capture.slot<Long>()
    every { prefs.getLong(any(), capture(long)) } returns long.get()
    // style above doesn't work for boolean ("Cannot cast java.lang.Boolean to boolean")
    every { prefs.getBoolean(any(), true) } returns true
    every { prefs.getBoolean(any(), false) } returns false
    return prefs
}

fun mockPrefs3(): Preferences {
    val prefs: Preferences = mock()
    val obs = mockPrefs2()
    every { prefs.prefs } returns obs
    val string = Capture.slot<String>()
    every { prefs.getString(any(), capture(string)) } returns string.get()
    val int = Capture.slot<Int>()
    every { prefs.getInt(any(), capture(int)) } returns int.get()
    val long = Capture.slot<Long>()
    every { prefs.getLong(any(), capture(long)) } returns long.get()
    // style above doesn't work for boolean ("Cannot cast java.lang.Boolean to boolean")
    every { prefs.getBoolean(any(), true) } returns true
    every { prefs.getBoolean(any(), false) } returns false
    return prefs
}
