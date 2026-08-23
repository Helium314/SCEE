package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.common.ClearIcon
import de.westnordost.streetcomplete.ui.common.SearchIcon
import de.westnordost.streetcomplete.ui.common.VerticalDivider
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import org.jetbrains.compose.resources.stringResource

/** A search field and a list of results for features below. */
@Composable
fun FeatureSearch(
    onSelectedFeature: (Feature) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    geometryType: GeometryType? = null,
    countryCode: String? = null,
    filterFn: (Feature) -> Boolean = { true },
    codesOfDefaultFeatures: List<String> = emptyList(),
) {
    val state = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var search by remember { mutableStateOf("") }
    val languages = remember { getLanguagesForFeatureDictionary() }
    val defaultFeatures = remember(codesOfDefaultFeatures, featureDictionary, languages, countryCode) {
        codesOfDefaultFeatures.mapNotNull { id ->
            featureDictionary.getById(
                id = id,
                languages = languages,
                country = countryCode
            )
        }
    }
    val features = remember(search, featureDictionary, languages, countryCode, geometryType, filterFn, defaultFeatures) {
        if (search.isNotEmpty()) {
            featureDictionary.getByTerm(
                search = search,
                languages = languages,
                country = countryCode,
                geometry = geometryType,
            ).filter(filterFn).take(50).toList()
        } else defaultFeatures
    }

    Column(
        modifier = modifier
    ) {
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            placeholder = {
                Text(stringResource(Res.string.quest_shop_gone_replaced_answer_hint2))
            },
            leadingIcon = { SearchIcon() },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) { ClearIcon() }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                showKeyboardOnFocus = true,
                imeAction = ImeAction.None,
                hintLocales = LocaleList.current,
            ),
        )
        Divider()
        if (features.isEmpty()) {
            CenteredLargeTitleHint(stringResource(Res.string.no_search_results))
        } else if (search.isNotEmpty()) {
            FeaturesColumn(
                features = features,
                onClickFeature = onSelectedFeature,
                featureDictionary = featureDictionary,
                modifier = Modifier.fillMaxWidth(),
                countryCode = countryCode,
                searchText = search
            )
        } else {
            val (iconOnly, normal) = features.partition { it.id in iconOnlyFeatures }
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                FeaturesColumn(
                    features = normal,
                    onClickFeature = onSelectedFeature,
                    featureDictionary = featureDictionary,
                    modifier = Modifier.fillMaxWidth(0.75f),
                    countryCode = countryCode,
                    searchText = search
                )
                if (iconOnly.isNotEmpty()) {
                    VerticalDivider()
                    IconFeaturesColumn(
                        features = iconOnly,
                        onClickFeature = onSelectedFeature,
                    )
                }
            }
        }
    }
}

/** Show a list of features that can be selected */
@Composable
private fun FeaturesColumn(
    features: List<Feature>,
    onClickFeature: (Feature) -> Unit,
    featureDictionary: FeatureDictionary,
    modifier: Modifier = Modifier,
    countryCode: String? = null,
    searchText: String? = null,
) {
    val state = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .fadingVerticalScrollEdges(state.scrollIndicatorState, 32.dp),
    ) {
        items(features) { feature ->
            Box(Modifier
                .fillMaxWidth()
                .clickable { onClickFeature(feature) }
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FeatureItem(
                    feature = feature,
                    featureDictionary = featureDictionary,
                    countryCode = countryCode,
                    searchText = searchText,
                    iconSize = 22.5.dp // preset icons are 15x15 px, so this is 1.5x
                )
            }
        }
    }
}

@Composable
private fun IconFeaturesColumn(
    features: List<Feature>,
    onClickFeature: (Feature) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = modifier
    ) {
        items(features) { feature ->
            Box(Modifier
                .width(56.dp)
                .clickable { onClickFeature(feature) }
                .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                val icon = iconOnlyFeatures[feature.id] ?: R.drawable.preset_maki_marker_stroked
                val tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                if (LocalResources.current.getResourceEntryName(icon).startsWith("preset_"))
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(icon),
                        contentDescription = null,
                        modifier = modifier.align(Alignment.Center),
                        tint = tint,
                    )
                else
                    Image(
                        painter = androidx.compose.ui.res.painterResource(icon),
                        contentDescription = null,
                        modifier = modifier.align(Alignment.Center),
                    )
            }
        }
    }
}

// todo (from old dialog)
//  optional position for local language
//  but this requires countryInfos, which are currently Android only

// todo: weird mix of pin icons, quest icons, temaki icons
//  ideally all would be same style, especially avoid monochrome temaki icons
//  the colors really help a lot for finding the right icon very quickly
private val iconOnlyFeatures = mapOf(
    "amenity/bench" to R.drawable.preset_temaki_bench,
    "amenity/lounger" to R.drawable.preset_temaki_lounger,
    "amenity/bicycle_parking" to R.drawable.quest_bicycle_parking,
    "amenity/motorcycle_parking" to R.drawable.quest_motorcycle_parking,
    "leisure/picnic_table" to R.drawable.preset_maki_picnic_site,
    "amenity/waste_basket" to R.drawable.preset_maki_waste_basket,
    "amenity/recycling_container" to R.drawable.quest_recycling_container,
    "amenity/bicycle_repair_station" to R.drawable.quest_bicycle_repair,
    "amenity/drinking_water" to R.drawable.quest_drinking_water,
    "emergency/fire_hydrant" to R.drawable.quest_fire_hydrant,
    "amenity/vending_machine" to R.drawable.preset_temaki_vending_machine,
    "amenity/vending_machine/cigarettes" to R.drawable.preset_temaki_vending_cigarettes,
    "amenity/vending_machine/excrement_bags" to R.drawable.preset_temaki_vending_pet_waste,
    "amenity/vending_machine/public_transport_tickets" to R.drawable.preset_temaki_vending_tickets,
    "amenity/vending_machine/drinks" to R.drawable.preset_temaki_vending_cold_drink,
    "amenity/atm" to R.drawable.quest_money,
    "natural/tree" to R.drawable.quest_tree,
    "tourism/information/guidepost" to R.drawable.quest_destination,
    "amenity/post_box" to R.drawable.quest_mail,
    "amenity/charging_station" to R.drawable.quest_car_charger,
    "highway/street_lamp" to R.drawable.preset_temaki_street_lamp_arm,
    "man_made/surveillance/camera" to R.drawable.quest_surveillance_camera,
    "highway/speed_camera" to R.drawable.preset_temaki_security_camera,
    "highway/crossing/unmarked" to R.drawable.quest_pedestrian,
    "highway/crossing/uncontrolled" to R.drawable.quest_pedestrian_crossing,
    "highway/crossing/traffic_signals" to R.drawable.quest_blind_traffic_lights_sound,
    "highway/traffic_signals" to R.drawable.quest_traffic_lights,
    "barrier/kerb" to R.drawable.quest_kerb_tactile_paving,
    "barrier/kerb/flush" to R.drawable.preset_temaki_kerb_flush,
    "barrier/kerb/rolled" to R.drawable.preset_temaki_kerb_rolled,
    "barrier/kerb/raised" to R.drawable.preset_temaki_kerb_raised,
    "barrier/kerb/lowered" to R.drawable.preset_temaki_kerb_lowered,
    "barrier/bollard" to R.drawable.preset_temaki_bollard,
    "traffic_calming/table" to R.drawable.preset_temaki_speed_table,
    "traffic_calming/bump" to R.drawable.preset_temaki_speed_bump,
    "entrance" to R.drawable.quest_door,
    "highway/stop" to R.drawable.preset_temaki_stop,
    "highway/give_way" to R.drawable.preset_temaki_yield,
    "highway/bus_stop" to R.drawable.preset_temaki_bus,
)
