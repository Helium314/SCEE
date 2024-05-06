package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.google.gson.JsonObject
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(private val map: MapLibreMap) {
    private val pinsSource = GeoJsonSource(SOURCE)

    val layers: List<Layer> = listOf(
        CircleLayer("pin-dot-layer", SOURCE)
            .withFilter(gte(zoom(), 14f))
            .withProperties(
                circleColor("white"),
                circleStrokeColor("#aaaaaa"),
                circleRadius(4f),
                circleStrokeWidth(1f),
                circleTranslate(arrayOf(0f, -6f)) // so that it hides behind the pin
            ),
        SymbolLayer("pins-layer", SOURCE)
            .withFilter(gte(zoom(), 16f))
            .withProperties(
                iconImage(get("icon-image")),
                iconSize(0.5f),
                // better would be arrayOf(-5f, 0f, -14f, 5f) or something like that, but setting
                // different paddings per side is not supported by MapLibre Native yet
                iconPadding(-4f),
                iconOffset(listOf(-9f, -69f).toTypedArray()),
                symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE), // = order in which they were added
            )
    )

    /** Shows/hides the pins */
    @UiThread fun setVisible(value: Boolean) {
        val visibility = if (value) Property.VISIBLE else Property.NONE
        layers.forEach { it.setProperties(visibility(visibility)) }
    }

    fun getProperties(properties: JsonObject): Map<String, String> =
        properties.getProperties()


    init {
        pinsSource.isVolatile = true
        map.style?.addSource(pinsSource)
    }

    /** Show given pins. Previously shown pins are replaced with these.  */
    suspend fun set(pins: Collection<Pin>) {
        val features = pins.sortedBy { it.order }.map { it.toFeature() }
        val mapLibreFeatures = FeatureCollection.fromFeatures(features)
        withContext(Dispatchers.Main) { pinsSource.setGeoJson(mapLibreFeatures) }
    }

    /** Clear pins */
    @UiThread fun clear() {
        pinsSource.clear()
    }

    companion object {
        private const val SOURCE = "pins-source"
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0
)

private fun Pin.toFeature(): Feature {
    val p = JsonObject()
    p.addProperty("icon-image", iconName)
    properties.forEach { p.addProperty(it.first, it.second) }
    return Feature.fromGeometry(position.toPoint(), p)
}

private fun JsonObject.getProperties(): Map<String, String> =
    entrySet().associate { it.key to it.value.asString }
