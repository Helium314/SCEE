package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable

open class TextInputForm<T> : AbstractOsmQuestForm<T>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    open val keyboardType = KeyboardType.Text
    open val capitalization = KeyboardCapitalization.Sentences
    @Composable open fun hint(): String? = null

    protected lateinit var text: MutableState<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            text = rememberSerializable {
                mutableStateOf("")
            }
            var value by remember { mutableStateOf(TextFieldValue()) }
            ProvideTextStyle(LocalTextStyle.current.merge(fontWeight = FontWeight.Bold)) {
                TextField(
                    value = value,
                    onValueChange = {
                        text.value = it.text
                        value = it
                        checkIsFormComplete()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done,
                        capitalization = capitalization
                    ),
                    label = { hint()?.let { Text(it) } }
                )
            }
        } }
    }

    override fun isFormComplete(): Boolean = text.value.isNotBlank()

    override fun isRejectingClose(): Boolean = !isFormComplete()
}
