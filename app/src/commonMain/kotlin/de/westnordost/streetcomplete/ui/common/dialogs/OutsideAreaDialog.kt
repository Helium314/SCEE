package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutsideAreaDialog(
    position: LatLon,
    downloadedTilesSource: DownloadedTilesSource,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!downloadedTilesSource.contains(position.enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM).toTilesRect(), 0L))
        ConfirmationDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.general_warning)) },
            text = { Text(stringResource(Res.string.outside_downloaded_area_warning)) },
            onConfirmed = onConfirm
        )
    else onConfirm()
}
