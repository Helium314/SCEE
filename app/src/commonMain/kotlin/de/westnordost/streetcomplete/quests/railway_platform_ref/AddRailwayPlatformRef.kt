package de.westnordost.streetcomplete.quests.railway_platform_ref

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.quest.TextInputForm

class AddRailwayPlatformRef : OsmElementQuestType<String> {

    // inspired by https://github.com/streetcomplete/StreetComplete/pull/4315 + comments
    private val platformFilter = """
        ways with
          public_transport = platform
          and railway ~ platform|platform_edge
          and !ref
          and !~ "ref:.*"
          and !local_ref
          and !name
          and noref != yes
          and tram != yes
          and subway != yes
          and light_rail != yes
          and monorail != yes
    """.toElementFilterExpression()

    private val railwayFilter = "ways with railway = rail".toElementFilterExpression()

    // this is somewhat slow if there are matching platforms and a lot of railways, but that's rather
    // uncommon
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val railwayLineGeometries = mapData.ways.mapNotNull {
            // only geometries that are "normal" railways
            if (railwayFilter.matches(it))
                (mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry)?.polylines?.singleOrNull()
            else null
        }
        if (railwayLineGeometries.isEmpty()) return emptyList()

        // this also finds lines on non-matching levels / layers, but that's ok for now
        return mapData.ways.filter { platformFilter.matches(it) }.mapNotNull { platform ->
            val platformLineGeometry = mapData.getWayGeometry(platform.id)?.let {
                // only normal ways or areas
                if (it is ElementPolylinesGeometry)
                    it.polylines.singleOrNull()
                else (it as? ElementPolygonsGeometry)?.polygons?.singleOrNull()
            } ?: return@mapNotNull null
            var nearbyRailways = 0
            // we want at least two nearby railways, because for a single track there is no need for asking ref
            railwayLineGeometries.forEach {
                if (it.distanceTo(platformLineGeometry) < 10.0) {
                    nearbyRailways++
                    if (nearbyRailways > 1)
                        return@mapNotNull platform
                }
            }
            null
        }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (platformFilter.matches(element))
            null
        else
            false

    override val changesetComment = "Specify railway platform refs"
    override val wikiLink = "Tag:railway=platform"
    override val icon = Res.drawable.ic_quest_railway_platform_ref
    override val title = Res.string.quest_railwayPlatformRef_title
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val enabledInCountries = NoCountriesExcept("DE", "FR", "CH", "AT")
    override val defaultDisabledMessage = Res.string.quest_disabled_msg_railway_platform_ref

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        TextInputForm(on)
    }

    // don't allow sth like not signed, because railway platforms should always be signed...
    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags[prefs.getString(PREF_KEY, "ref")] = answer
    }

    override val hasQuestSettings = true

    @Composable override fun QuestSettings(onDismissRequest: () -> Unit) {
        InfoDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.quest_railwayPlatformRef_message)) },
            text = {
                Column {
                    Button({ prefs.putString(PREF_KEY, "local_ref"); onDismissRequest() }, Modifier.fillMaxWidth()) {
                        Text("local_ref")
                    }
                    Button({ prefs.remove(PREF_KEY); onDismissRequest() }, Modifier.fillMaxWidth()) {
                        Text("ref")
                    }
                }
            }
        )
    }
}

private const val PREF_KEY = "qs_AddRailwayPlatformRef_key"
