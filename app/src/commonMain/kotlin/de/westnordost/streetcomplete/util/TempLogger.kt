package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsSource
import de.westnordost.streetcomplete.data.logs.toChar
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.toInstant
import de.westnordost.streetcomplete.util.logs.Logger
import kotlinx.datetime.LocalDateTime

object TempLogger : Logger, LogsSource {
    override fun e(tag: String, message: String, exception: Throwable?) {
        if (exception == null) {
            synchronized(logLines) { log(LogLine('E', tag, message)) }
        } else {
            synchronized(logLines) { log(LogLine('E', tag, "$message\n${exception.stackTraceToString()}")) }
        }
    }

    override fun w(tag: String, message: String, exception: Throwable?) {
        if (exception == null) {
            synchronized(logLines) { log(LogLine('W', tag, message)) }
        } else {
            synchronized(logLines) { log(LogLine('W', tag, "$message\n${exception.stackTraceToString()}")) }
        }
    }

    override fun i(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('I', tag, message)) }
    }

    override fun d(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('D', tag, message)) }
    }

    override fun v(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('V', tag, message)) }
    }

    private fun log(line: LogLine) {
        synchronized(logLines) {
            if (logLines.size > 12000) // clear oldest entries if list gets too long
                logLines.subList(0, 2000).clear()
            logLines.add(line)
        }
    }

    private val logLines: MutableList<LogLine> = ArrayList(2000)

    /** returns a copy of [logLines] */
    fun getLog() = synchronized(logLines) { logLines.toList() }

    override fun getLogs(
        levels: Set<LogLevel>,
        messageContains: String?,
        newerThan: Long?,
        olderThan: Long?
    ): List<LogMessage> {
        val charLevels = levels.mapTo(hashSetOf()) { it.toChar() }
        return synchronized(logLines) {
            logLines.asSequence().filter {
                if (it.level !in charLevels) return@filter false
                if (messageContains != null && !it.message.contains(messageContains, true))return@filter false
                if (newerThan != null && it.time.toInstant().toEpochMilliseconds() < newerThan) return@filter false
                if (olderThan != null && it.time.toInstant().toEpochMilliseconds() > olderThan) return@filter false
                true
            }.map { line ->
                LogMessage(
                    LogLevel.entries.first { it.toChar() == line.level },
                    line.tag,
                    line.message,
                    null,
                    line.time.toInstant().toEpochMilliseconds()
                )
            }.toList()
        }
    }

    private val listeners = Listeners<LogsSource.Listener>()

    override fun addListener(listener: LogsSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: LogsSource.Listener) {
        listeners.remove(listener)
    }
}

data class LogLine(val level: Char, val tag: String, val message: String,) {
    val time = LocalDateTime.now()
    override fun toString(): String = // should look like a normal android log line
        "${time.toString().replace('T', ' ')} $level $tag: $message"
}
