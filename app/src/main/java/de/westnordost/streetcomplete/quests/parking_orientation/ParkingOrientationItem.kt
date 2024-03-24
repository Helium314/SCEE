package de.westnordost.streetcomplete.quests.parking_orientation

import android.content.Context
import de.westnordost.streetcomplete.R

import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.quests.parking_orientation.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.util.ktx.asBitmapDrawable

import de.westnordost.streetcomplete.osm.street_parking.StreetParkingDrawable
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL as DISPLAY_PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR as DISPLAY_PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL as DISPLAY_DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.OFF_STREET as DISPLAY_OFF_STREET
fun ParkingOrientation.asItem(context: Context): Item2<ParkingOrientation> {

    val display_val = when (this) {
        PARALLEL -> DISPLAY_PARALLEL
        DIAGONAL -> DISPLAY_DIAGONAL
        PERPENDICULAR -> DISPLAY_PERPENDICULAR
    }
    val drawable = DrawableImage(StreetParkingDrawable(context, display_val, DISPLAY_OFF_STREET, false, 128, 128, R.drawable.ic_car1).asBitmapDrawable(context.resources))
    return Item2(this, drawable, ResText(titleResId), null)

}
private val ParkingOrientation.titleResId: Int get() = when (this) {
        PARALLEL ->      R.string.quest_parking_orientation_answer_parallel
        DIAGONAL ->      R.string.quest_parking_orientation_answer_diagonal
        PERPENDICULAR -> R.string.quest_parking_orientation_answer_perpendicular
    }
