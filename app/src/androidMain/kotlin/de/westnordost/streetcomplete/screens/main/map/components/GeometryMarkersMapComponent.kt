package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.content.res.Resources
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.screens.main.map.createIconBitmap
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.view.toAndroidResourceId
import de.westnordost.streetcomplete.util.ktx.toHexColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import androidx.core.graphics.toColorInt
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.toRgba

/** Manages putting some generic geometry markers with an optional drawable on the map. I.e. to
 *  show the geometry of elements surrounding the selected quest */
class GeometryMarkersMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
    private val mapImages: MapImages
) {

    init {
        GlobalScope.launch {
            mapImages.addOnce(listOf(directionIcon)) {
                val name = context.resources.getResourceEntryName(directionIcon)
                createIconBitmap(context, it, true) to true
            }
        }
    }
    private val geometrySource = GeoJsonSource(SOURCE)

    val layers: List<Layer> = listOf(
        FillLayer("geo-fill", SOURCE)
            .withFilter(isArea())
            .withProperties(
                fillColor(get("color")),
                fillOpacity(0.3f)
            ),
        LineLayer("geo-lines", SOURCE)
            // both polygon and line
            .withProperties(
                lineWidth(10f),
                lineColor(get("color")),
                lineOpacity(0.5f),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        SymbolLayer("geo-symbols", SOURCE)
            .withFilter(isPoint())
            .withProperties(
                iconColor(get("color")),
                iconImage(get("icon")),
                iconSize(interpolate(linear(), zoom(), stop(17, 0.5f), stop(19, 1f))),
                iconAllowOverlap(true),
                iconRotate(get("rotation")),
                textField(get("label")),
                textAnchor(Property.TEXT_ANCHOR_TOP),
                textOffset(arrayOf(0f, 1f)),
                textSize(16 * context.resources.configuration.fontScale),
                textColor(get("color")),
                textFont(arrayOf("Roboto Bold")),
                textOptional(true)
            ),
        SymbolLayer("geo-direction", SOURCE)
            .withFilter(all(isPoint(), has("direction")))
            .withProperties(
                iconColor(get("color")),
                iconImage(context.resources.getResourceEntryName(directionIcon)),
                iconSize(interpolate(linear(), zoom(), stop(17, 0.7f), stop(19, 1.5f))),
                iconAllowOverlap(true),
                iconRotate(get("rotation")),
                iconRotationAlignment("map"),
                // how to translate dependent on rotation? needs an array, but how to create
//                iconTranslateAnchor("map"),
//                iconTranslate(array(
//                    product(cos(get("rotation")), literal(5f)),
//                    product(sin(get("rotation")), literal(5f))
//                ))
            )
    )

    init {
        geometrySource.isVolatile = true
        map.style?.addSource(geometrySource)
    }

    suspend fun setAll(markers: Iterable<Marker>) {

        val icons = markers.map { it.icon ?: Res.drawable.preset_maki_circle }.mapNotNull { it.toAndroidResourceId() }
        mapImages.addOnce(icons) {
            val name = context.resources.getResourceEntryName(it)
            val sdf = name.startsWith("preset_")
            createIconBitmap(context, it, sdf) to sdf
        }
        val features = markers.flatMap { it.toFeatures(context.resources) }

        withContext(Dispatchers.Main) {
            geometrySource.clear()
            geometrySource.setGeoJson(FeatureCollection.fromFeatures(features))
        }
    }

    @UiThread fun clear() {
        geometrySource.clear()
    }

    private fun update() {

    }

    companion object {
        private const val SOURCE = "geometry-source"
    }
}

private fun Marker.toFeatures(resources: Resources): List<Feature> {
    val features = ArrayList<Feature>(3)
    val color = color?.toHexColor() ?: "#D140D0"
    // point marker or any marker with title or icon
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val p = JsonObject()
        val mustHaveIcon = (icon ?: Res.drawable.preset_maki_circle).toAndroidResourceId()!!
        p.addProperty("icon", resources.getResourceEntryName(mustHaveIcon))
        if (title != null) {
            p.addProperty("label", title)
        }
        p.addProperty("color", color)
        p.addProperty("rotation", rotation?.toFloat() ?: 0.0f)
        features.add(Feature.fromGeometry(geometry.center.toPoint(), p))
    }

    if (direction != null && geometry is ElementPointGeometry) {
        val p = JsonObject()
        val color = this.color ?: "#80D140D0".toColorInt()
        p.addProperty("direction", true)
        p.addProperty("color", color.toRgba())
        p.addProperty("rotation", direction.toFloat())
        features.add(Feature.fromGeometry(geometry.center.toPoint(), p))
    }

    // polygon / polylines marker(s)
    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        features.add(Feature.fromGeometry(geometry.toMapLibreGeometry(), JsonObject().apply { addProperty("color", color) }))
    }
    return features
}
private val directionIcon = R.drawable.view_direction
