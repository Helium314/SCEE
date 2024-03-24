package de.westnordost.streetcomplete.quests.parking_orientation

import android.content.Context
import de.westnordost.streetcomplete.R

import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.ResText

import de.westnordost.streetcomplete.osm.street_parking.StreetParkingDrawable
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL as SP_PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE as SP_STREET_SIDE
fun ParkingOrientation.asItem(context: Context): Item2<ParkingOrientation> {
    val drawable = DrawableImage(StreetParkingDrawable(context, SP_PARALLEL, SP_STREET_SIDE, false, 128, 128, R.drawable.ic_car1))
    val txts = ResText(titleResId)
    return Item2(this, drawable, ResText(titleResId), null)

}
private val ParkingOrientation.titleResId: Int get() = when (this) {
        PARALLEL ->      R.string.quest_parking_orientation_answer_parallel
        DIAGONAL ->      R.string.quest_parking_orientation_answer_diagonal
        PERPENDICULAR -> R.string.quest_parking_orientation_answer_perpendicular
    }
