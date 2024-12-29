package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.content.res.Resources
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.main.map.Marker
import de.westnordost.streetcomplete.screens.main.map.components.FocusGeometryMapComponent.Companion
import de.westnordost.streetcomplete.screens.main.map.createIconBitmap
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.toHexColor
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
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

/** Allows setting any (User-Provided) Geo-Json. Reads text from "name" property */
class CustomGeometryMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
) {
    private val geometrySource = GeoJsonSource(SOURCE)

    val layers: List<Layer> = listOf(
        FillLayer("custom-geo-fill", SOURCE)
            .withFilter(isArea())
            .withProperties(
                fillColor(COLOR),
                fillOpacity(OPACITY * 0.5f)
            ),
        LineLayer("custom-geo-lines", SOURCE)
            // both polygon and line
            .withProperties(
                lineWidth(10f),
                lineColor(COLOR),
                lineOpacity(OPACITY),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        SymbolLayer("custom-geo-text", SOURCE)
            .withFilter(has("name"))
            .withProperties(
                textColor(COLOR),
                textOpacity(OPACITY),
                textFont(arrayOf("Roboto Regular")),
                textField(get("name")),
                textAllowOverlap(true),
                textIgnorePlacement(true),
                textAnchor(Property.TEXT_ANCHOR_TOP),
                textOffset(arrayOf(0f, 1f)),
                textSize(14 * context.resources.configuration.fontScale),
            ),
    )

    init {
        geometrySource.isVolatile = true
        map.style?.addSource(geometrySource)
    }

    @UiThread fun set(geoJson: String) {
        try {
            geometrySource.setGeoJson(geoJson)
        } catch (e: Exception) {
            Log.e("CustomGeometrySource", "error setting geoJson: $e")
            clear()
        }
    }

    @UiThread fun clear() {
        geometrySource.clear()
    }

    companion object {
        private const val SOURCE = "custom-geo-source"
        private const val COLOR = "#53FE70"
        private const val OPACITY = 0.4f
    }
}
