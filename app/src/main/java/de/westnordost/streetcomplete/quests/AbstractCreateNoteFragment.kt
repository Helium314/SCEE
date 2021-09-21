package de.westnordost.streetcomplete.quests

import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.View
import android.widget.EditText
import androidx.fragment.app.add
import androidx.fragment.app.commit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

/** Abstract base class for a bottom sheet that lets the user create a note */
abstract class AbstractCreateNoteFragment : AbstractBottomSheetFragment() {

    protected abstract val noteInput: EditText
    protected abstract val okButton: View
    protected abstract val gpxButton: View

    private val attachPhotoFragment: AttachPhotoFragment?
        get() = childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as AttachPhotoFragment?

    private val noteText get() = noteInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        if (savedInstanceState == null) {
            childFragmentManager.commit { add<AttachPhotoFragment>(R.id.attachPhotoFragment) }
        }

        noteInput.addTextChangedListener(TextChangedWatcher { updateOkButtonEnablement() })
        okButton.setOnClickListener { onClickOk() }
        gpxButton.setOnClickListener { onClickGpx() }

        updateOkButtonEnablement()
    }

    private fun onClickOk() {
        onComposedNote(noteText, attachPhotoFragment?.imagePaths.orEmpty())
    }

    private fun onClickGpx() {
        onComposedNote(noteText)
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose() =
        noteText.isNotEmpty() || attachPhotoFragment?.imagePaths?.isNotEmpty() == true

    private fun updateOkButtonEnablement() {
        if (noteText.isNotEmpty()) {
            okButton.popIn()
            gpxButton.popIn()
        } else {
            okButton.popOut()
            gpxButton.popOut()
        }
    }

    protected abstract fun onComposedNote(text: String, imagePaths: List<String>)
    protected abstract fun onComposedNote(text: String)
}
