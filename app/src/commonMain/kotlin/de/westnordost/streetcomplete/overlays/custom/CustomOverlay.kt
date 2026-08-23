package de.westnordost.streetcomplete.overlays.custom

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.elementfilter.ParseException
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ConfirmDeleteDialog
import de.westnordost.streetcomplete.util.getCurrentCustomOverlayPref
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.ktx.isArea
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.iterator
import kotlin.math.abs

class CustomOverlay(val prefs: ObservableSettings) : Overlay {

    override val title = Res.string.custom_overlay_title
    override val icon = Res.drawable.ic_custom_overlay
    override val changesetComment = "Edit user-defined element selection"
    override val wikiLink: String = "Tags"
    override val isCreateNodeEnabled get() = prefs.getString(Prefs.CUSTOM_OVERLAY_IDX_FILTER, "").startsWith("nodes")

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>> {
        val filter = try {
            prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, prefs), "").toElementFilterExpression()
        } catch (e: ParseException) { return emptySequence() }
        val colorKeyPref = prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, prefs), "").let {
            if (it.startsWith("!")) it.substringAfter("!")
            else it
        }
        val colorKeySelector = try { colorKeyPref.takeIf { it.isNotBlank() }?.toRegex() }
            catch (_: Exception) { null }
        val dashFilter = try {
            val string = prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_DASH_FILTER, prefs), "").takeIf { it.isNotBlank() }
            string?.let { "ways with $it".toElementFilterExpression() }
        } catch (_: Exception) { null }
        val missingColor = if (prefs.getBoolean(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA, prefs), true))
                OverlayColor.Red
            else
                OverlayColor.Invisible
        return mapData
            .filter(filter)
            .map { it to getStyle(it, colorKeySelector, dashFilter, missingColor) }
    }

    @Composable
    override fun Form(on: (OverlayAction) -> Unit, element: Element?, geometry: ElementGeometry, countryInfo: CountryInfo) {
        /* todo if element == null
        showInBottomSheet(CreatePoiFragment.createWithPrefill(prefs.getString(Prefs.CUSTOM_OVERLAY_IDX_FILTER, "")!!.substringAfter("with ")))
         */
        var confirmDeleteNode by remember { mutableStateOf<Node?>(null) }
        OverlayForm(
            on,
            false,
            false,
            { },
            otherAnswers = { listOfNotNull(
                if (element is Node) {
                    AnswerItem(stringResource(Res.string.quest_generic_answer_does_not_exist)) {
                        confirmDeleteNode = element
                    }
                } else null
            ) },
        ) {
            val colorKeyPref = prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, prefs), "")
            val colorKeySelector = try {
                val actualColorKeyPref = if (colorKeyPref.startsWith("!"))
                    colorKeyPref.substringAfter("!")
                else colorKeyPref
                actualColorKeyPref.takeIf { it.isNotEmpty() }?.toRegex()
            } catch (_: Exception) { null }
            val colorTags = if (colorKeySelector != null)
                    element?.tags?.filter { it.key.matches(colorKeySelector) }
                else null
            if (colorTags != null)
                Text(colorTags.entries.sortedBy { it.key }.joinToString("\n") { "${it.key} = ${it.value}" })
            if (element != null) {
                TextButton({
                    if (colorKeyPref.startsWith("!") && !colorKeyPref.contains(' '))
                        focusKey = colorKeyPref
                    on(Action.EditTags) // todo: this is also in "...", need to remove it
                }) {
                    Text(stringResource(Res.string.quest_generic_answer_show_edit_tags))
                }
            }
        }
        confirmDeleteNode?.let { node ->
            ConfirmDeleteDialog(
                onDismissRequest = { confirmDeleteNode = null },
                onConfirmDelete = { on(Edit(DeletePoiNodeAction(node))) },
                onLeaveNote = { on(Action.LeaveNote) }
            )
        }
    }

    companion object {
        var focusKey: String? = null
    }
}

private fun getStyle(element: Element, colorKeySelector: Regex?, dashFilter: ElementFilterExpression?, defaultMissingColor: Color): OverlayStyle {
    val color by lazy {
        if (colorKeySelector == null) OverlayColor.Lime
        else {
            val colorString = element.tags.mapNotNull {
                // derive color from all matching tags
                if (it.key.matches(colorKeySelector)) it.value + it.key
                else null
            }.sorted().joinToString() // sort because tags hashMap doesn't have a defined order
            if (colorString.isEmpty()) defaultMissingColor
            else Color(createColorFromString(colorString).toColorInt())
        }
    }

    var leftColor: Color? = null
    var rightColor: Color? = null
    var centerColor: Color? = null
    // get left/right style if there is some match
    if (colorKeySelector != null && element !is Node && !element.isArea()) { // avoid doing needless work here
        val leftColorTags = mutableListOf<String>()
        val rightColorTags = mutableListOf<String>()
        val centerColorTags = mutableListOf<String>()
        for ((k, v) in element.tags) {
            if (!k.matches(colorKeySelector)) continue
            // create color in a way that left, right and both match in color -> strip side from tags
            if (v == "both" || k.contains(":both")) {
                val t = v + k.replace(":both", "")
                leftColorTags.add(t)
                rightColorTags.add(t)
                continue
            }
            if (v == "right" || k.contains(":right")) {
                rightColorTags.add(v + k.replace(":right", ""))
                continue
            }
            if (v == "left" || k.contains(":left")) {
                leftColorTags.add(v + k.replace(":left", ""))
                continue
            }
            // only use a center color if there is a match that is not related to left/right/both
            centerColorTags.add(v + k)
        }
        // make sure to use all matching color tags
        if (leftColorTags.isNotEmpty())
            leftColor = Color(createColorFromString(leftColorTags.sorted().joinToString()).toColorInt())
        if (rightColorTags.isNotEmpty())
            rightColor = Color(createColorFromString(rightColorTags.sorted().joinToString()).toColorInt())
        if (centerColorTags.isNotEmpty())
            centerColor = Color(createColorFromString(centerColorTags.sorted().joinToString()).toColorInt())
    }


    return when {
//        element is Node -> OverlayStyle.Point(R.drawable.ic_custom_overlay_node, getNameLabel(element.tags), color)
        // MapLibre can only use colors with sdf icons, not with normal images
        element is Node -> OverlayStyle.Point(Res.drawable.preset_maki_circle, getNameLabel(element.tags), color = color)
        element.isArea() -> OverlayStyle.Polygon(color, label = getNameLabel(element.tags))
        // no labels for lines, because this often leads to duplicate labels e.g. for roads
        leftColor != null || rightColor != null -> OverlayStyle.Polyline(
            stroke = centerColor?.let { OverlayStyle.Stroke(it, dashFilter?.matches(element) == true) },
            strokeLeft = leftColor.takeIf { it != null }?.let { OverlayStyle.Stroke(it) },
            strokeRight = rightColor.takeIf { it != null }?.let { OverlayStyle.Stroke(it) }
        )
        else -> OverlayStyle.Polyline(OverlayStyle.Stroke(color, dashFilter?.matches(element) == true))
    }
}

private fun createColorFromString(string: String): String {
    val c = abs(string.hashCode()).toString(16)
    return when {
        c.length >= 6 -> "#${c.subSequence(c.length - 6, c.length)}"
        else -> createColorFromString("${c}1") // the 1 is there to avoid very similar colors for numbers
    }
}
