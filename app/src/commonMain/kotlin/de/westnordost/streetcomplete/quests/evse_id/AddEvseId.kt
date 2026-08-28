package de.westnordost.streetcomplete.quests.evse_id

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.core.net.toUri
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.MultiValueQuestQrScanForm
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept
import de.westnordost.streetcomplete.util.math.contains
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

val urlPrefixToQueryParameter = mapOf(
    "http://m.intercharge.eu/qr" to "evseid",
    "https://charge.elli.eco/" to "evseid",
    "https://e-mobility.lidl.de/qr" to "evseid",
    "https://smatrics.com/start-charging" to "evseId",
    "https://www.aral-pulse.de/webshop/details" to "evseid",
)
val urlPrefixesWithEvseIdAsPath = arrayOf(
    "https://laden.enercity.de/",
    "https://pay.chargedrive.com/",
    "https://qr.on-charge.com/",
    "www.chargepoint-services.com/",
)

class AddEvseId : OsmElementQuestType<String> {

    override val icon = Res.drawable.ic_quest_charger_ref
    override val title = Res.string.quest_evse_id_title
    override val wikiLink = "Key:ref:EU:EVSE"
    override val changesetComment = "Add EVSE ID (ref:EU:EVSE)"
    override val enabledInCountries = NoCountriesExcept(
        "AT","BE","BG","CH","CY","CZ","DE","DK","EE","ES","FI","FR","GR","HR",
        "HU","IE","IT","LT","LU","LV","MT","NL","PL","PT","RO","SE","SI","SK"
    )

    override val achievements = listOf(CITIZEN)

    private val baseFilter = """
        nodes, ways with
          (man_made = charge_point or amenity = charging_station)
          and !ref:EU:EVSE
          and (ref:signed != no or !ref:signed)
          and access !~ private|no
    """.toElementFilterExpression()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {

        val chargePoints = mapData
            .filter("nodes with man_made = charge_point")
            .toList()

        val candidates = mapData.filter(baseFilter)

        return candidates.filter { element ->

            if (element is Way && element.tags["amenity"] == "charging_station") {

                val geometry = mapData.getGeometry(element.type, element.id)
                    ?: return@filter true

                val bounds = geometry.bounds

                val hasChargePointsInside = chargePoints.any { cp ->
                    val cpGeom = mapData.getGeometry(cp.type, cp.id) ?: return@any false
                    bounds.contains(cpGeom.center)
                }

                if (hasChargePointsInside) return@filter false
            }

            true
        }.toList()
    }

    // Geometry-dependent → return null to trigger surrounding-data re-check
    override fun isApplicableTo(element: Element): Boolean? =
        if (baseFilter.matches(element)) null else false

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val composableScope = rememberCoroutineScope()
        val context = LocalContext.current

        val scanUnknownValueToastText = stringResource(Res.string.quest_evse_id_scan_unknown_value)

        MultiValueQuestQrScanForm(
            on,
            addAnotherValueText = Res.string.quest_evse_id_add_more,
            scanAnotherValueText = Res.string.quest_evse_id_scan_more,
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_generic_answer_noSign)) { on(Answer("ref:signed=no")) }) },
            isOk = { EVSE_REGEX.matches(it) },
            onQrCodeParsed = {
                value: String, addValue: (String?) -> Unit ->
                    composableScope.launch {
                        val result = parseQrCodeValue(value)

                        if (result != null) {
                            addValue(result)
                        } else {
                            Toast.makeText(context, scanUnknownValueToastText, Toast.LENGTH_LONG).show()
                        }
                    }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            hint = stringResource(Res.string.quest_evse_id_hint)
        )
    }

    override fun applyAnswerTo(
        answer: String,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        if (answer.startsWith("ref:signed=")) {
            tags["ref:signed"] = answer.substringAfter("=")
            return
        }

        val normalized = answer
            .split(";")
            .map { it.trim().uppercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }
            .joinToString(";")

        if (normalized.isNotEmpty()) {
            tags["ref:EU:EVSE"] = normalized
        }
    }
}

private val EVSE_REGEX = Regex("(?i)^[A-Z]{2}\\*?[A-Z0-9]{3}\\*?E(?!\\*)[A-Z0-9*]{1,31}$")
private val EVSE_REGEX_IN_TEXT = Regex("(?i)[A-Z]{2}\\*?[A-Z0-9]{3}\\*?E(?!\\*)[A-Z0-9*]{1,31}")

private suspend fun followRedirect(url: String): String? {
    val client = HttpClient { followRedirects = false }
    val response: HttpResponse = client.request(url) {
        method = HttpMethod.Head
    }
    return response.headers[HttpHeaders.Location]
}

// Some of the QR codes contain a URL that is missing the protocol.
private fun normalizeUrl(url: String): String = if (url.startsWith("http")) url else "https://$url"

private fun getUrlQueryParameter(url: String, queryParameter: String): String? {
    val uri = normalizeUrl(url).toUri()
    return uri.getQueryParameter(queryParameter)
}

private fun getUrlPath(url: String): String? {
    val uri = normalizeUrl(url).toUri()
    return uri.path?.replace("/", "")
}

private suspend fun parseQrCodeValue(value: String): String? {
    urlPrefixToQueryParameter.forEach { (urlPrefix, queryParameter) ->
        if (value.startsWith(urlPrefix))
            return getUrlQueryParameter(value, queryParameter)
    }

    urlPrefixesWithEvseIdAsPath.forEach { urlPrefix ->
        if (value.startsWith(urlPrefix))
            return getUrlPath(value)
    }

    if (value.startsWith("https://chrg.me")) {
        val redirectedLocation = followRedirect(value) ?: return null
        return getUrlQueryParameter(redirectedLocation, "evseId")
    }

    arrayOf("evseid", "evseId").forEach { queryParameter ->
        val maybeEvseId = getUrlQueryParameter(value, queryParameter)
        if (maybeEvseId != null && EVSE_REGEX.matches(maybeEvseId)) return maybeEvseId
    }

    val regexMatch = EVSE_REGEX_IN_TEXT.find(value)
    if (regexMatch != null)
        return regexMatch.value

    return null
}
