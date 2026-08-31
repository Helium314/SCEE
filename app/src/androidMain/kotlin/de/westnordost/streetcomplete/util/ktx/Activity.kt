package de.westnordost.streetcomplete.util.ktx

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> AppCompatActivity.observe(flow: Flow<T>, collector: FlowCollector<T>) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect {
                collector.emit(it)
            }
        }
    }
}

fun AppCompatActivity.loadFileKit() = FileKit.init(this)
