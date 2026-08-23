package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES_FOR_TRACKTYPES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRoadSurface : OsmFilterQuestType<Surface>() {

    override val elementFilter = """
        ways with (
          ${prefs.getString("${questPrefix(prefs)}qs_${name}_element_selection", highwaySelection)}
        )
        and (
          !surface
          or surface ~ ${INVALID_SURFACES.joinToString("|")}
          or (
            surface ~ paved|unpaved
            and !surface:note
            and !note:surface
            and !check_date:surface
          )
          or surface ~ ${UNPAVED_SURFACES.joinToString("|")} and surface older today -6 years
          or surface older today -12 years
          ${INVALID_SURFACES_FOR_TRACKTYPES.entries.joinToString("\n") { (tracktype, surfaces) ->
              "or tracktype = $tracktype and surface ~ ${surfaces.joinToString("|")}"
          }}
        )
        and (access !~ private|no or (foot and foot !~ private|no))
        and ice_road != yes
    """

    override val changesetComment = "Specify road surfaces"
    override val wikiLink = "Key:surface"
    override val icon = Res.drawable.quest_street_surface
    override val title = Res.string.quest_streetSurface_title
    override val achievements = listOf(CAR, BICYCLIST)

    @Composable
    override fun Form(on: (QuestAction<Surface>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = Surface.getSelectableValuesForWays(countryInfo.countryCode),
            itemContent = { item ->
                ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
            },
            favoriteKey = "AddRoadSurfaceForm",
            title = stringResource(
                if (element.tags["area"] == "yes") Res.string.quest_streetSurface_square_title
                else Res.string.quest_streetSurface_title
            ),
        )
    }

    override fun applyAnswerTo(answer: Surface, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }

    override val hasQuestSettings = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        FullElementSelectionDialog(prefs, "${questPrefix(prefs)}qs_${name}_element_selection", Res.string.quest_settings_element_selection, highwaySelection, onDismissRequest)
    }
}

private val highwaySelection = """
    highway ~ ${listOf(
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian", "track", "busway"
).joinToString("|")
}
          or highway = service and service !~ driveway|slipway
""".trimIndent()
