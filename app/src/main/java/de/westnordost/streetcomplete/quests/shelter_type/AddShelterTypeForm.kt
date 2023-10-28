package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PUBLIC_TRANSPORT
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PICNIC_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.GAZEBO
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.WEATHER_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.LEAN_TO
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PAVILION
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.BASIC_HUT
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.SUN_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.FIELD_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.ROCK_SHELTER
import de.westnordost.streetcomplete.view.image_select.Item

class AddShelterTypeForm : AImageListQuestForm<ShelterType, ShelterType>() {

    override val items = listOf(
        Item(PUBLIC_TRANSPORT, R.drawable.shelter_type_public_transport, R.string.quest_shelterType_public_transport),
        Item(PICNIC_SHELTER, R.drawable.shelter_type_picnic_shelter, R.string.quest_shelterType_picnic_shelter),
        Item(GAZEBO, R.drawable.shelter_type_gazebo, R.string.quest_shelterType_gazebo),
        Item(WEATHER_SHELTER, R.drawable.shelter_type_weather_shelter, R.string.quest_shelterType_weather_shelter),
        Item(LEAN_TO, R.drawable.shelter_type_lean_to, R.string.quest_shelterType_lean_to),
        Item(PAVILION, R.drawable.shelter_type_pavilion, R.string.quest_shelterType_pavilion) ,
        Item(BASIC_HUT, R.drawable.shelter_type_basic_hut, R.string.quest_shelterType_basic_hut),
        Item(SUN_SHELTER, R.drawable.shelter_type_sun_shelter, R.string.quest_shelterType_sun_shelter),
        Item(FIELD_SHELTER, R.drawable.shelter_type_field_shelter, R.string.quest_shelterType_field_shelter),
        Item(ROCK_SHELTER, R.drawable.shelter_type_rock_shelter, R.string.quest_shelterType_rock_shelter)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ShelterType>) {
        applyAnswer(selectedItems.single())
    }
}
