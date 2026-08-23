package de.westnordost.streetcomplete.screens.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.isWayComplete
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.osm.getDirection
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.osm.places.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.quests.custom.CustomQuestList
import de.westnordost.streetcomplete.quests.custom.FILENAME_CUSTOM_QUEST
import de.westnordost.streetcomplete.quests.custom.readFromUriToExternalFile
import de.westnordost.streetcomplete.quests.tree.FILENAME_TREES
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.icon
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.MapFragment
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.Padding
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPadding
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.settings.custom_geometry_changed
import de.westnordost.streetcomplete.screens.settings.gpx_track_changed
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.ui.common.feature.FeatureSearchDialog
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.toDpOffset
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.util.getSystemLocales
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toList
import de.westnordost.streetcomplete.util.ktx.toOffset
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.view.toAndroidResourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import kotlin.jvm.java
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/** Controls the main view.
 *
 *  The logical sub components of this main view are all outsourced into individual child fragments
 *  with which this fragment communicates with.
 *
 *  The child fragments do not communicate with each other but only with their parent (this class)
 *  and the parent then controls its children. Hence, all the logic when interacting with the
 *  map / bottom sheets / sidebars / buttons etc. passes through this class and this is why this
 *  class implements all the listeners of its child fragments.
 *
 *  This class does not contain so much logic itself, it delegates most of it to its children.
 *  Think of it as the wiring that binds all the components together.
 *
 *  Still, as this is by far the largest in terms of lines of code. For easier reading, in
 *  IntelliJ, you can collapse sections of this class that start with "//region" using the little
 *  [-] icon next to it.
 *
 */
class MainActivity :
    BaseActivity(),
    // listeners to child fragments:
    MapFragment.Listener,
    MainMapFragment.Listener,
    // listeners to changes to data:
    VisibleQuestsSource.Listener,
    MapDataWithEditsSource.Listener,
    // rest
    AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val questAutoSyncer: QuestAutoSyncer by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val prefs: Preferences by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val notesSource: NotesWithEditsSource by inject()
    private val questsHiddenSource: QuestsHiddenSource by inject()
    private val feedsUpdater: FeedsUpdater by inject()
    private val featureDictionary: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val mapAppLauncher: MapAppLauncher by inject()
    private val levelFilter: LevelFilter by inject()
    private val countryBoundaries: Lazy<CountryBoundaries> by inject(named("CountryBoundariesLazy"))
    private val osmQuestController: OsmQuestController by inject()
    private val customQuestList: CustomQuestList by inject()

    private lateinit var locationManager: FineLocationManager

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()
    private val mainBottomSheetViewModel by viewModel<MainBottomSheetViewModel>()

    private val showMapContextMenu = mutableStateOf(false)
    private val lastMapLongPress = mutableStateOf<Pair<Offset, LatLon>?>(null)

    private val lastMapClick = mutableStateOf<MapClick?>(null)

    private var windowInfo: WindowInfo? = null

    // for freezing the map while sidebar is open
    private var wasFollowingPosition: Boolean? = null
    private var wasNavigationMode: Boolean? = null

    private val mapFragment: MainMapFragment? get() =
        supportFragmentManager.findFragmentByTag(TAG_MAP) as MainMapFragment?

    private var questMonitorJob: Job? = null

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    private val requestLocationPermissionResultReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.getBooleanExtra(LocationRequestFragment.GRANTED, false)) {
                toast(R.string.no_gps_no_quests, Toast.LENGTH_LONG)
            }
        }
    }

    //region Lifecycle - Android Lifecycle Callbacks

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        questMonitorJob?.cancel()
        try { applicationContext.unbindService(questMonitorConnection) }
        catch (_: IllegalArgumentException) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        val root = RelativeLayout(this)
        val compose = ComposeView(this)
        val mapContainer = FragmentContainerView(this).also { it.id = 1 }
        root.addView(mapContainer, ViewGroup.LayoutParams(-1, -1))
        root.addView(compose, ViewGroup.LayoutParams(-1, -1))

        setContentView(root)

        if (savedInstanceState == null) {
            handleIntent(intent)

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(LocationRequestFragment(), TAG_LOCATION_REQUEST)
                add(mapContainer, MainMapFragment(), TAG_MAP)
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            requestLocationPermissionResultReceiver,
            IntentFilter(LocationRequestFragment.REQUEST_LOCATION_PERMISSION_RESULT)
        )

        lifecycle.addObserver(questAutoSyncer)
        feedsUpdater.updateAtMostDaily()

        locationManager = FineLocationManager(this, this::onLocationChanged)

        compose.setContent { AppTheme {
            val isMapAppLaunchAvailable = remember { mapAppLauncher.isAvailable() }
            var lastQuestSolved by remember { mutableStateOf<QuestSolvedEvent?>(null) }

            windowInfo = LocalWindowInfo.current
            val context = LocalContext.current

            MainScreen(
                viewModel = viewModel,
                editHistoryViewModel = editHistoryViewModel,
                mainBottomSheetViewModel = mainBottomSheetViewModel,
                onClickZoomIn = ::onClickZoomIn,
                onClickZoomOut = ::onClickZoomOut,
                onZoomDrag = ::onZoomDrag,
                onClickCompass = ::onClickCompassButton,
                onClickLocation = ::onClickLocationButton,
                onClickLocationPointer = ::onClickLocationPointer,
                onClickCreate = ::onClickCreateButton,
                onClickStopTrackRecording = ::onClickTracksStop,
                onDownload = ::onClickDownload,
                onClickSettings = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                },
                onClickQuestSettings = {
                    context.startActivity(SettingsActivity.createLaunchQuestSettingsIntent(context))
                },
                onClickAbout = {
                    context.startActivity(Intent(context, AboutActivity::class.java))
                },
                onClickProfile = {
                    context.startActivity(Intent(context, UserActivity::class.java))
                },
                onClickLogin = {
                    val intent = Intent(context, UserActivity::class.java)
                    intent.putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true)
                    context.startActivity(intent)
                },
                onExplainedNeedForLocationPermission = ::requestLocation,
                onSetMapMarkers = { markers ->
                    mapFragment?.setMarkersForCurrentHighlighting(markers)
                },
                onSolvedQuest = { icon, position ->
                    val offset = root.getLocationInWindow()
                    val startPos = mapFragment?.getPointOf(position)!!

                    startPos.x += offset.x
                    startPos.y += offset.y

                    lastQuestSolved = QuestSolvedEvent(icon, startPos.toOffset())
                },
                getOffset = { position ->
                    val offset = root.getLocationInWindow()
                    val position = mapFragment?.getPointOf(position)!!
                    position.x += offset.x
                    position.y += offset.y
                    position.toOffset()
                },
            )

            if (prefs.getBoolean(Prefs.SHOW_SOLVED_ANIMATION, true))
                lastQuestSolved?.let { LastQuestSolvedEffect(it) }
            var showAddPoiDialog by rememberSaveable { mutableStateOf(false) }

            val lastLongPressOffset = lastMapLongPress.value?.first ?: Offset.Zero
            val lastLongPressPosition = lastMapLongPress.value?.second
            MapContextMenu(
                expanded = showMapContextMenu.value,
                onDismissRequest = { showMapContextMenu.value = false },
                onClickCreateNote = { lastLongPressPosition?.let { onClickCreateNote(it) } },
                onClickCreateTrack = { onClickCreateTrack() },
                isOpenLocationAvailable = isMapAppLaunchAvailable,
                onClickOpenLocation = {
                    if (lastLongPressPosition != null) {
                        mapAppLauncher.openAt(
                            position = lastLongPressPosition,
                            zoom = mapFragment?.cameraPosition?.zoom ?: 18.0
                        )
                    }
                },
                isExpertMode = prefs.getBoolean(Prefs.EXPERT_MODE, false) && lastLongPressPosition != null,
                onClickAddNode = { showAddPoiDialog = true },
                onClickInsertNode = {
                    (mainBottomSheetViewModel as MainBottomSheetViewModelImpl).shownBottomSheet.value =
                        ShownBottomSheet.InsertNode(lastLongPressPosition!!)
                },
                offset = lastLongPressOffset.toDpOffset()
            )

            if (showAddPoiDialog) {
                val country = countryBoundaries.value.getIds(lastLongPressPosition!!).firstOrNull()
                val defaultFeatureIds: List<String> = prefs.getString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, "")
                    .split("§").filter { it.isNotBlank() && it != "shop" }
                    .ifEmpty { POPULAR_PLACE_FEATURE_IDS }
                FeatureSearchDialog(
                    onDismissRequest = { showAddPoiDialog = false },
                    onSelectedFeature = {
                        val recentFeatureIds = prefs.getString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, "").split("§").toMutableList()
                        if (recentFeatureIds.lastOrNull() != it.id) {
                            recentFeatureIds.remove(it.id)
                            recentFeatureIds.add(it.id)
                            prefs.putString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, recentFeatureIds.takeLast(35).joinToString("§"))
                        }
                        onAddPoi(lastLongPressPosition, it)
                    },
                    featureDictionary = featureDictionary.value,
                    geometryType = GeometryType.POINT,
                    countryCode = country,
                    filterFn = { true },
                    codesOfDefaultFeatures = defaultFeatureIds.reversed()
                )
            }
        } }

        observe(editHistoryViewModel.selectedEdit) { edit ->
            if (edit != null) {
                val geometry = editHistoryViewModel.getEditGeometry(edit)
                mapFragment?.startFocus(geometry, null)
                mapFragment?.highlightGeometry(geometry)
                mapFragment?.highlightPins(edit.icon!!.toAndroidResourceId()!!, listOf(edit.position))
                mapFragment?.hideOverlay()
            } else if (editHistoryViewModel.isShowingSidebar.value) {
                mapFragment?.endFocus()
                mapFragment?.clearHighlighting()
                mapFragment?.hideOverlay() // because clearHighlighting shows overlay again :-/
            }
        }
        observe(editHistoryViewModel.isShowingSidebar) { isShowingSidebar ->
            if (!isShowingSidebar) {
                unfreezeMap()
                mapFragment?.clearFocus()
                mapFragment?.clearHighlighting()
                mapFragment?.pinMode = MainMapFragment.PinMode.QUESTS
            } else {
                freezeMap()
                mapFragment?.hideOverlay()
                mapFragment?.pinMode = MainMapFragment.PinMode.EDITS
            }
        }
        observe(viewModel.geoUri) { geoUri ->
            if (geoUri != null) {
                viewModel.consumeGeoUri()
                mapFragment?.setInitialCameraPosition(geoUri)
                viewModel.isFollowingPosition.value = mapFragment?.isFollowingPosition ?: false
                viewModel.isNavigationMode.value = mapFragment?.isNavigationMode ?: false
            }
        }
        observe(mainBottomSheetViewModel.shownBottomSheet) { shownBottomSheet ->
            updateBottomSheetElementPosition()
            if (shownBottomSheet != null) {
                freezeMap()
                when (shownBottomSheet) {
                    is ShownBottomSheet.CreateOsmNote -> {
                        /* nothing more */
                    }
                    is ShownBottomSheet.OsmNoteQuest -> {
                        showQuestDetailsOnMap(shownBottomSheet.quest, null)
                    }
                    is ShownBottomSheet.OsmQuest -> {
                        showQuestDetailsOnMap(shownBottomSheet.quest, shownBottomSheet.element)
                    }
                    is ShownBottomSheet.Overlay -> {
                        if (shownBottomSheet.element != null) {
                            showOverlayElementDetailsOnMap(
                                overlay = shownBottomSheet.overlay,
                                element = shownBottomSheet.element,
                                geometry = shownBottomSheet.geometry!!
                            )
                        } else {
                            showOverlayForNewElementOnMap(shownBottomSheet.overlay)
                        }
                    }
                    is ShownBottomSheet.ExternalSourceQuest -> {
                        val element = shownBottomSheet.quest.elementKey?.let { mapDataWithEditsSource.get(it.type, it.id) }
                        showQuestDetailsOnMap(shownBottomSheet.quest, element)
                    }
                    is ShownBottomSheet.AddPoi -> {} // nothing to show on map
                    is ShownBottomSheet.InsertNode -> {
                        mapFragment?.updateCameraPosition {
                            position = shownBottomSheet.position
                            padding = getOpenQuestFormMapPadding()
                        }
                        shownBottomSheet.highlightGeometries = { mapFragment?.highlightGeometries(it) }
                    }
                }
            } else {
                clearHighlighting()
                unfreezeMap()
                mapFragment?.endFocus()
            }
        }
        observe(viewModel.selectedOverlay) { selectedOverlay ->
            if (mainBottomSheetViewModel.shownBottomSheet.value is ShownBottomSheet.Overlay) {
                mainBottomSheetViewModel.closeBottomSheet()
            }
        }
        observe(viewModel.reverseQuestOrder) {
            mapFragment?.setQuestOrder(it)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        if (gpx_track_changed) {
            mapFragment?.loadGpxTrack()
            gpx_track_changed = false
        }
        if (custom_geometry_changed) {
            mapFragment?.loadCustomGeometry()
            custom_geometry_changed = false
        }
    }

    override fun onStart() {
        super.onStart()

        updateScreenOn()

        Log.i(TAG, "onStart (add listeners)")
        wasFollowingPosition = mapFragment?.isFollowingPosition // use value from mapFragment if already loaded
        visibleQuestsSource.addListener(this)
        mapDataWithEditsSource.addListener(this)
        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(isLocationAvailable)
        stopQuestMonitor()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        if (intent.type == "text/*") {
            AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.pref_custom_title) { _, _ ->
                    readFromUriToExternalFile(uri, FILENAME_CUSTOM_QUEST, this)
                    customQuestList.reload()
                    visibleQuestsSource.clearCache()
                }
                .setNeutralButton(R.string.pref_trees_title) { _, _ ->
                    readFromUriToExternalFile(uri, FILENAME_TREES, this)
                }
                .show()
        }
        viewModel.setUri(uri.toString())
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop (remove listeners)")

        visibleQuestsSource.removeListener(this)
        mapDataWithEditsSource.removeListener(this)
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)

        locationManager.removeUpdates()
        startQuestMonitor()
    }

    //endregion

    /* ------------------------------- Preferences listeners ------------------------------------ */

    private fun updateScreenOn() {
        if (prefs.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    //region QuestsMapFragment - Callbacks from the map with its quest pins

    /* ---------------------------------- MapFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        viewModel.geoUri.value?.let { geoUri ->
            viewModel.consumeGeoUri()
            mapFragment?.setInitialCameraPosition(geoUri)
        }
        viewModel.isFollowingPosition.value = mapFragment?.isFollowingPosition ?: false
        viewModel.isNavigationMode.value = mapFragment?.isNavigationMode ?: false
        viewModel.isRecordingTracks.value = mapFragment?.isRecordingTracks ?: false
        viewModel.mapCamera.value = mapFragment?.cameraPosition
        viewModel.metersPerDp.value = mapFragment?.getMetersPerPixel() ?: 0.0
        updateBottomSheetElementPosition()
        updateDisplayedPosition()
    }

    override fun onMapIsChanging(camera: CameraPosition) {
        viewModel.mapCamera.value = camera
        viewModel.metersPerDp.value = mapFragment?.getMetersPerPixel() ?: 0.0
        updateBottomSheetElementPosition()
        updateDisplayedPosition()
    }

    override fun onPanBegin() {
        /* panning only results in not following location anymore if a location is already known
           and displayed
         */
        if (mapFragment?.displayedLocation != null) {
            setIsFollowingPosition(false)
        }
    }

    override fun onUserCameraMoveStarted() {
        viewModel.userHasMovedCamera.value = true
    }

    override fun onLongPress(point: PointF, position: LatLon) {
        if (mainBottomSheetViewModel.shownBottomSheet.value != null || editHistoryViewModel.isShowingSidebar.value) return

        lastMapLongPress.value = Pair(Offset(point.x, point.y), position)
        showMapContextMenu.value = true
    }

    /* ---------------------------- MainMapFragment.Listener --------------------------- */

    override fun onClickedQuest(questKey: QuestKey) {
        if (mainBottomSheetViewModel.shownBottomSheet.value != null) return
        mainBottomSheetViewModel.showQuest(questKey)
    }

    override fun onClickedEdit(editKey: EditKey) {
        editHistoryViewModel.select(editKey)
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        if (mainBottomSheetViewModel.shownBottomSheet.value != null) {
            lastMapClick.value = MapClick(position, clickAreaSizeInMeters)
        } else if (editHistoryViewModel.isShowingSidebar.value) {
            editHistoryViewModel.hideSidebar()
        }
    }

    override fun onClickedElement(elementKey: ElementKey) {
        val overlay = viewModel.selectedOverlay.value ?: return
        if (mainBottomSheetViewModel.shownBottomSheet.value != null) return
        mainBottomSheetViewModel.showElementInOverlay(overlay, elementKey)
    }

    override fun onDisplayedLocationDidChange() {
        updateDisplayedPosition()
    }

    private fun updateDisplayedPosition() {
        viewModel.displayedPosition.value = getDisplayedPoint()?.toOffset()
    }

    private fun updateBottomSheetElementPosition() {
        val bottomSheetElementPosition = mainBottomSheetViewModel.shownBottomSheet.value?.position
        mainBottomSheetViewModel.geometryOffsetInWindow.value =
            if (bottomSheetElementPosition != null) mapFragment?.getPointOf(bottomSheetElementPosition)?.toOffset()
            else null
    }

    private fun getDisplayedPoint(): PointF? {
        val mapFragment = mapFragment ?: return null
        val displayedPosition = mapFragment.displayedLocation?.toLatLon() ?: return null
        return mapFragment.getPointOf(displayedPosition)
    }

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread
    override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val questKey =
            when (val shown = mainBottomSheetViewModel.shownBottomSheet.value) {
                is ShownBottomSheet.OsmNoteQuest -> shown.quest.key
                is ShownBottomSheet.OsmQuest -> shown.quest.key
                else -> return
        }
        // open quest has been deleted
        if (questKey in removed) {
            mainBottomSheetViewModel.closeBottomSheet()
        }
    }

    @AnyThread
    override fun onInvalidated() {
        val questKey =
            when (val shown = mainBottomSheetViewModel.shownBottomSheet.value) {
                is ShownBottomSheet.OsmNoteQuest -> shown.quest.key
                is ShownBottomSheet.OsmQuest -> shown.quest.key
                else -> return
            }

        lifecycleScope.launch {
            val openQuest = withContext(Dispatchers.IO) { visibleQuestsSource.get(questKey) }
            // open quest does not exist anymore after visible quest invalidation
            if (openQuest == null) {
                mainBottomSheetViewModel.closeBottomSheet()
            }
        }
    }

    /* ---------------------------- MapDataWithEditsSource.Listener ----------------------------- */

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val elementKey = (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)?.element?.key ?: return
        if (elementKey in deleted) {
            mainBottomSheetViewModel.closeBottomSheet()
        }
    }

    @AnyThread
    override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        val elementKey = (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)?.element?.key ?: return
        lifecycleScope.launch {
            val openElement = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) }
            // open element does not exist anymore after download
            if (openElement == null) {
                mainBottomSheetViewModel.closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onCleared() {
        val elementKey = (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)?.element?.key ?: return
        mainBottomSheetViewModel.closeBottomSheet()
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++ VIEW CONTROL ++++++++++++++++++++++++++++++++++++++ */

    //region Location - Request location and update location status

    private fun updateLocationAvailability(isAvailable: Boolean) {
        if (isAvailable) {
            onLocationIsEnabled()
        } else {
            onLocationIsDisabled()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationIsEnabled() {
        viewModel.locationState.value = LocationState.SEARCHING
        mapFragment?.startPositionTracking()
        questAutoSyncer.startPositionTracking()

        mapFragment?.centerCurrentPositionIfFollowing()
        locationManager.getCurrentLocation()
    }

    private fun onLocationIsDisabled() {
        viewModel.locationState.value = when {
            hasLocationPermission -> LocationState.ALLOWED
            else -> LocationState.DENIED
        }
        viewModel.isNavigationMode.value = false
        viewModel.displayedPosition.value = null
        mapFragment?.clearPositionTracking()
        questAutoSyncer.stopPositionTracking()
        locationManager.removeUpdates()
    }

    private fun onLocationChanged(location: Location) {
        viewModel.locationState.value = LocationState.UPDATING
    }

    //endregion

    //region Buttons - Functionality for the buttons in the main view

    private fun onClickDownload(enqueue: Boolean) {
        val downloadBbox = getDownloadArea() ?: return
        viewModel.download(downloadBbox, enqueue)
    }

    fun onClickZoomOut() {
        mapFragment?.updateCameraPosition(300) { zoomBy = -1.0 }
    }

    fun onClickZoomIn() {
        mapFragment?.updateCameraPosition(300) { zoomBy = +1.0 }
    }

    private fun onZoomDrag(dp: Float) {
        mapFragment?.updateCameraPosition(300) { zoomBy = dp / 20.0 }
    }

    private fun onClickTracksStop() {
        // hide the track information
        viewModel.isRecordingTracks.value = false
        val mapFragment = mapFragment ?: return
        mapFragment.stopPositionTrackRecording()
        val pos = mapFragment.displayedLocation?.toLatLon() ?: return
        composeNote(pos, mapFragment.recordedTracks.takeIf { it.isNotEmpty() })
    }

    private fun onClickCompassButton() {
        // Clicking the compass button will always rotate the map back to north and remove tilt
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition ?: return

        // if the user wants to rotate back north, it means he also doesn't want to use nav mode anymore
        if (mapFragment.isNavigationMode) {
            mapFragment.updateCameraPosition(300) { rotation = 0.0 }
            setIsNavigationMode(false)
        } else {
            mapFragment.updateCameraPosition(300) {
                rotation = 0.0
                tilt = 0.0
            }
        }
    }

    private fun onClickLocationButton() {
        val mapFragment = mapFragment ?: return

        when {
            !viewModel.locationState.value.isEnabled -> {
                requestLocation()
            }
            !mapFragment.isFollowingPosition -> {
                setIsFollowingPosition(true)
            }
            else -> {
                if (!prefs.getBoolean(Prefs.DISABLE_NAVIGATION_MODE, false) || mapFragment.isNavigationMode)
                    setIsNavigationMode(!mapFragment.isNavigationMode)
            }
        }
    }

    private fun onClickLocationPointer() {
        setIsFollowingPosition(true)
    }

    private fun requestLocation() {
        (supportFragmentManager.findFragmentByTag(TAG_LOCATION_REQUEST) as? LocationRequestFragment)?.startRequest()
    }

    private fun onClickCreateButton() {
        val overlay = viewModel.selectedOverlay.value ?: return
        mainBottomSheetViewModel.showCreateElementInOverlay(overlay)
    }

    private fun setIsNavigationMode(navigation: Boolean) {
        mapFragment?.isNavigationMode = navigation
        viewModel.isNavigationMode.value = navigation
    }

    private fun setIsFollowingPosition(follow: Boolean) {
        mapFragment?.isFollowingPosition = follow
        viewModel.isFollowingPosition.value = follow
        if (follow) mapFragment?.centerCurrentPositionIfFollowing()
    }

    private fun getDownloadArea(): BoundingBox? {
        val displayArea = mapFragment?.getDisplayedArea()
        if (displayArea == null) {
            toast(R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG)
            return null
        }

        val enclosingBBox = displayArea.asBoundingBoxOfEnclosingTiles(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        val areaInSqKm = enclosingBBox.area() / 1000000
        if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
            toast(R.string.download_area_too_big, Toast.LENGTH_LONG)
            return null
        }

        // below a certain threshold, it does not make sense to download, so let's enlarge it
        if (areaInSqKm < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM) {
            val cameraPosition = mapFragment?.cameraPosition
            if (cameraPosition != null) {
                val radius = sqrt(1000000 * ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM / PI)
                return cameraPosition.position.enclosingBoundingBox(radius)
            }
        }

        return enclosingBBox
    }

    /* -------------------------------------- Context Menu -------------------------------------- */

    private fun onClickCreateNote(pos: LatLon) {
        if ((mapFragment?.cameraPosition?.zoom ?: 0.0) < ApplicationConstants.NOTE_MIN_ZOOM) {
            toast(R.string.create_new_note_unprecise)
            return
        }

        composeNote(pos)
    }

    private fun composeNote(pos: LatLon, trackpoints: List<Trackpoint>? = null) {
        mainBottomSheetViewModel.showCreateNote(trackpoints)

        mapFragment?.updateCameraPosition(300) {
            position = pos
            padding = getOpenQuestFormMapPadding()
        }
    }

    private fun onAddPoi(pos: LatLon, feature: Feature) {
        (mainBottomSheetViewModel as MainBottomSheetViewModelImpl).shownBottomSheet.value =
            ShownBottomSheet.AddPoi(pos, feature)
        mapFragment?.updateCameraPosition(300) {
            position = pos
            padding = getOpenQuestFormMapPadding()
        }
    }

    private fun onClickCreateTrack() {
        mapFragment?.startPositionTrackRecording()
        viewModel.isRecordingTracks.value = true
    }

    //endregion

    //region Bottom Sheet - Controlling the bottom sheet and its interaction with the map

    /** Open or replace the bottom sheet. */
    private fun showBottomSheet(content: ShownBottomSheet) {
        freezeMap()
    }

    /** Make the map not follow the user's location anymore temporarily */
    private fun freezeMap() {
        val mapFragment = mapFragment ?: return
        if (wasFollowingPosition == null) wasFollowingPosition = mapFragment.isFollowingPosition
        if (wasNavigationMode == null) wasNavigationMode = mapFragment.isNavigationMode
        mapFragment.isFollowingPosition = false
        mapFragment.isNavigationMode = false
    }

    /** Make the map follow the user's location again (if it was following before) */
    private fun unfreezeMap() {
        wasFollowingPosition?.let { mapFragment?.isFollowingPosition = it }
        wasNavigationMode?.let { mapFragment?.isNavigationMode = it }
        wasFollowingPosition = null
        wasNavigationMode = null
    }

    private fun clearHighlighting() {
        mapFragment?.clearHighlighting()
    }

    //endregion

    //region Bottom sheets

    @UiThread
    private fun showOverlayForNewElementOnMap(overlay: Overlay) {
        val mapFragment = mapFragment ?: return

        mapFragment.updateCameraPosition {
            position = getCrosshairOffset()?.toPointF()?.let { mapFragment.getPositionAt(it) }
            padding = getOpenQuestFormMapPadding()
        }
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private suspend fun showOverlayElementDetailsOnMap(overlay: Overlay, element: Element, geometry: ElementGeometry) {
        val mapFragment = mapFragment ?: return

        mapFragment.updateCameraPosition {
            padding = getOpenQuestFormMapPadding()
        }

        mapFragment.highlightGeometry(geometry)
        mapFragment.highlightPins(overlay.icon.toAndroidResourceId()!!, listOf(geometry.center))
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private fun showQuestDetailsOnMap(quest: Quest, element: Element?) {
        Log.i(TAG, "showQuestDetails for ${quest.key}")
        val mapFragment = mapFragment ?: return
        val highlightedElementMarkers = lifecycleScope.async(Dispatchers.IO) { getHighlightedElements(quest, element) }
        val otherQuestMarkers = lifecycleScope.async(Dispatchers.IO) { showOtherQuests(quest) }

        mapFragment.startFocus(quest.geometry, getOpenQuestFormMapPadding())
        mapFragment.highlightGeometry(quest.geometry)
        mapFragment.highlightPins(quest.type.icon.toAndroidResourceId()!!, quest.markerLocations)
        mapFragment.hideNonHighlightedPins(quest.key)
        mapFragment.hideOverlay()

        lifecycleScope.launch(Dispatchers.IO) {
            val markers = mergeMarkersAtSamePosition(highlightedElementMarkers.await(), otherQuestMarkers.await())
            // on main thread because quest form clears markers on launch, and is running in parallel
            // -> this ensures markers are set only after the form is created
            withContext(Dispatchers.Main) { mapFragment.setMarkersForCurrentHighlighting(markers) }
        }
    }

    // if quest and highlight marker at same position, set color of highlight marker to quest color
    private fun mergeMarkersAtSamePosition(highlightMarkers: List<Marker>, questMarkers: List<Marker>): List<Marker> {
        // creating a map of possibly many markers may not be the fastest thing... but still ok i guess
        val m = hashMapOf<LatLon, Marker>()
        highlightMarkers.associateByTo(m) { it.geometry.center }
        questMarkers.forEach { questMarker ->
            val highlightMarker = m[questMarker.geometry.center]
            if (highlightMarker == null) {
                m[questMarker.geometry.center] = questMarker
                return@forEach
            }
            m[questMarker.geometry.center] = highlightMarker.copy(color = questMarker.color)
        }
        return m.values.toList()
    }

    private fun getHighlightedElements(quest: Quest, element: Element? = null): List<Marker> {
        val bbox = when (quest) {
            is OsmQuest -> quest.geometry.bounds.enlargedBy(quest.type.highlightedElementsRadius)
            is ExternalSourceQuest -> quest.geometry.bounds.enlargedBy(quest.type.highlightedElementsRadius)
            else -> return emptyList()
        }
        var mapData: MapDataWithGeometry? = null

        fun getMapData(): MapDataWithGeometry {
            val data = mapDataWithEditsSource.getMapDataWithGeometry(bbox)
            if (data is MutableMapDataWithGeometry && element is Way && !data.isWayComplete(element.id)) {
                // complete way to show stuff along it
                mapDataWithEditsSource.getWayComplete(element.id)?.nodes?.forEach {
                    data.put(it, ElementPointGeometry(it.position))
                }
            }
            mapData = data
            return data
        }

        val elements =
            when (quest) {
                is OsmQuest -> element?.let { quest.type.getHighlightedElements(it, mapData ?: getMapData()) } ?: emptySequence()
                is ExternalSourceQuest -> quest.type.getHighlightedElements(::getMapData)
                else -> emptySequence()
            }
        if (elements == emptySequence<Element>()) return emptyList()
        val levels = element?.let { parseLevelsOrNull(it.tags) }
        val localLanguages = getSystemLocales().toList().map { it.language }
        return elements.mapNotNull { e ->
            // don't highlight "this" element
            if (element == e) return@mapNotNull null
            // include only elements with the same (=intersecting) level, if any
            val eLevels = parseLevelsOrNull(e.tags)
            if (!levels.levelsIntersect(eLevels)) return@mapNotNull null
            // include only elements with the same layer, if any (except for bridges)
            if (element?.tags?.get("layer") != e.tags["layer"] && e.tags["bridge"] == null) return@mapNotNull null

            val geometry = mapData?.getGeometry(e.type, e.id) ?: return@mapNotNull null
            val icon = getIcon(featureDictionary.value, e)
            val title = getTitle(e.tags, localLanguages)
            val direction = (e as? Node)?.getDirection(mapDataWithEditsSource)
            Marker(geometry, icon, title, direction = direction)
        }.toList()
    }

    private fun showOtherQuests(quest: Quest): List<Marker> {
        if (prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0) == 0) {
            viewModel.nearbyQuests.value = emptyList()
            return emptyList()
        }

        // Quests should be grouped by element key, so non-OsmQuests need some kind of fake key
        fun Quest.thatKey() = if (this is OsmQuest) ElementKey(elementType, elementId)
            else ElementKey(ElementType.entries[abs(key.hashCode() % 3)], -abs(7 * key.hashCode()).toLong())

        val markers = mutableListOf<Marker>()

        val quests = visibleQuestsSource.getNearbyQuests(quest, prefs.getFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, 0.0f).toDouble() + 0.01)
            .filterNot { it == quest || it.type.dotColor != null } // ignore current quest and poi dots
            .sortedBy { it.thatKey() != quest.thatKey() }
        if (quests.isEmpty()) {
            viewModel.nearbyQuests.value = emptyList()
            return emptyList()
        }

        val questsAndColorByElement = mutableMapOf<ElementKey, Pair<Int, MutableList<Quest>>>()
        val colors = arrayOf(Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.BLUE, ColorUtils.blendARGB(Color.RED, Color.YELLOW, 0.5f))
        var colorIterator = colors.iterator()
        quests.forEach {
            questsAndColorByElement.getOrPut(it.thatKey()) {
                val color = if (it.thatKey() == quest.thatKey()) Color.WHITE // no color for other quests of the selected element
                    else colorIterator.next()
                if (!colorIterator.hasNext()) colorIterator = colors.iterator() // cycle through color list if there are many elements
                if (color != Color.WHITE)
                    markers.add(Marker(it.geometry, color = color))
                Pair(color, mutableListOf())
            }.second.add(it)
        }
        viewModel.nearbyQuests.value = questsAndColorByElement.values
        return markers
    }

    private fun getCrosshairOffset(): Offset? {
        val windowInfo = windowInfo ?: return null
        val padding = getOpenQuestFormMapPadding() ?: return null
        val size = windowInfo.containerSize
        return Offset(
            (padding.left + (size.width - padding.left - padding.right) / 2).toFloat(),
            (padding.top + (size.height - padding.top - padding.bottom) / 2).toFloat()
        )
    }

    private fun Offset.toPointF() = PointF(x, y)

    private fun getOpenQuestFormMapPadding(): Padding? {
        val windowInfo = windowInfo ?: return null
        val layoutDirection = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        val density = Density(this)
        return Dimensions.getOpenQuestFormMapPadding(windowInfo).toPadding(layoutDirection, density)
    }

    //endregion
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.action == KeyEvent.ACTION_UP) {
                viewModel.showMainMenuDialog.value = true
            }
            return true
        }
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP && prefs.getBoolean(Prefs.VOLUME_ZOOM, false)) {
            if (event.action == KeyEvent.ACTION_UP) {
                onClickZoomIn()
            }
            return true
        }
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && prefs.getBoolean(Prefs.VOLUME_ZOOM, false)) {
            if (event.action == KeyEvent.ACTION_UP) {
                onClickZoomOut()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun startQuestMonitor() {
        if (prefs.getBoolean(Prefs.QUEST_MONITOR, false) && !NearbyQuestMonitor.running) {
            questMonitorJob?.cancel()
            questMonitorJob = lifecycleScope.launch {
                delay(1000) // wait, as we don't want do start the monitor if onDestroy follows
                applicationContext.bindService(Intent(this@MainActivity, NearbyQuestMonitor::class.java), questMonitorConnection, BIND_AUTO_CREATE)
            }
        }
    }

    private fun stopQuestMonitor() {
        // try to stop quest monitor more often than it seems necessary, because sometime android
        // is slow to react, e.g. when quickly switching between SC and other app
        if (prefs.getBoolean(Prefs.QUEST_MONITOR, false) || NearbyQuestMonitor.running) {
            try { applicationContext.unbindService(questMonitorConnection) }
            catch (_: IllegalArgumentException) { } // happens on first start, and maybe if there is some issue
            questMonitorJob?.cancel()
            questMonitorJob = lifecycleScope.launch {
                delay(5000)
                // sometimes it just doesn't stop, or is started with considerable delay for some reason
                // try to catch this here
                try { applicationContext.unbindService(questMonitorConnection) }
                catch (_: IllegalArgumentException) { }
            }
        }
    }

    companion object {
        private const val TAG_LOCATION_REQUEST = "LocationRequestFragment"
        private const val TAG_MAP = "MainMapFragment"

        // quest monitor connection needs to work with multiple main activities
        private val questMonitorConnection: ServiceConnection by lazy { object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {}
            override fun onServiceDisconnected(p0: ComponentName?) {}
        } }
    }
}

private const val TAG = "MainActivity"
