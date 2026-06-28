package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.RevertMoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometryUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/** Source for map data. It combines the original data downloaded with the edits made.
 *
 *  This class is threadsafe.
 */
interface MapDataWithEditsSource : MapDataRepository {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        /** Called when a number of elements have been updated or deleted */
        fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>)

        /** Called when all elements in the given bounding box should be replaced with the elements
         *  in the mapDataWithGeometry */
        fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)

        /** Called when all map data has been cleared */
        fun onCleared()
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    fun getGeometry(type: ElementType, id: Long): ElementGeometry?

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry>

    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry
}
