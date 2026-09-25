package de.westnordost.streetcomplete.overlays.restriction

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateRelationAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeleteRelationAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.createChanges
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.osm.AddConditionalDialog
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSignForm
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType
import de.westnordost.streetcomplete.quests.max_weight.Weight
import de.westnordost.streetcomplete.quests.max_weight.getIcon
import de.westnordost.streetcomplete.quests.max_weight.osmKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.access_manager_button_add_conditional
import de.westnordost.streetcomplete.resources.add
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.delete_confirmation
import de.westnordost.streetcomplete.resources.ic_overlay_restriction
import de.westnordost.streetcomplete.resources.leave_note
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.osm_element_gone_confirmation
import de.westnordost.streetcomplete.resources.overlay_none
import de.westnordost.streetcomplete.resources.quest_generic_confirmation_title
import de.westnordost.streetcomplete.resources.quest_generic_confirmation_yes
import de.westnordost.streetcomplete.resources.quest_max_weight
import de.westnordost.streetcomplete.resources.restriction_overlay_exceptions
import de.westnordost.streetcomplete.resources.restriction_overlay_only_for
import de.westnordost.streetcomplete.resources.restriction_overlay_other_restrictions
import de.westnordost.streetcomplete.resources.restriction_overlay_relation_incomplete
import de.westnordost.streetcomplete.resources.restriction_overlay_relation_unsupported
import de.westnordost.streetcomplete.resources.restriction_overlay_remove_conditional_restrictions
import de.westnordost.streetcomplete.resources.restriction_overlay_select_way
import de.westnordost.streetcomplete.resources.restriction_overlay_show_details
import de.westnordost.streetcomplete.resources.restriction_overlay_signed_switch
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalSetOverlayVisibleCallback
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private enum class RestrictionType { TURN, WEIGHT }

@Composable
fun RestrictionOverlayWayForm(
    on: (OverlayAction) -> Unit,
    element: Element, // Way
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
) {
    // Selected way geometry; used as map highlight fallback when no relation geometry exists.
    val wayGeometry = geometry
    val way = element as Way
    val locale = countryInfo.languageTag?.let { Locale(it) } ?: Locale.current
    val units = countryInfo.weightLimitUnits

    val originalRestrictions = remember(way.id) {
        getOriginalRestrictions(way, mapDataWithEditsSource)
    }

    var selectedRestriction by rememberSerializable {
        mutableStateOf(
            getInitialRestrictionPreferComplete(originalRestrictions) {
                isRelationComplete(it, mapDataWithEditsSource)
            }
        )
    }
    var currentRestriction by rememberSerializable {
        mutableStateOf(selectedRestriction)
    }

    var turnRestrictionSelectionMode by rememberSaveable { mutableStateOf(false) }
    var draftSigned by rememberSaveable {
        mutableStateOf(
            (selectedRestriction as? TurnRestriction)?.relation?.tags?.get("implicit") != "yes"
        )
    }
    var draftTurnType by rememberSaveable {
        mutableStateOf(
            (selectedRestriction as? TurnRestriction)?.relation?.tags?.getShortRestrictionValue()
                ?.takeIf { it in turnRestrictionTypeList }
                ?: turnRestrictionTypeList.first()
        )
    }

    // Weight input including unit (weight string on WeightRestriction stays numeric for isComplete)
    var editedWeight by rememberSerializable {
        mutableStateOf(
            (selectedRestriction as? WeightRestriction)?.let { parseWeight(it.weight, units) }
        )
    }

    var showAddRestrictionDialog by remember { mutableStateOf(false) }
    var showWeightTypeDialog by remember { mutableStateOf(false) }
    var showExceptionsDialog by remember { mutableStateOf(false) }
    var showOnlyForDialog by remember { mutableStateOf(false) }
    var showAddConditionalDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRelationDetails by remember { mutableStateOf(false) }

    val setOverlayVisible = LocalSetOverlayVisibleCallback.current
    val mapClick = LocalLastMapClick.current
    val mapMarkersCallback = LocalMapMarkersCallback.current

    val hasChanges = currentRestriction != null && currentRestriction != selectedRestriction
    val isComplete = when (val restriction = currentRestriction) {
        is WeightRestriction -> hasChanges && editedWeight != null
        is TurnRestriction -> hasChanges
        null -> false
    }

    val showAddButton =
        !turnRestrictionSelectionMode && currentRestriction == selectedRestriction

    fun selectRestriction(restriction: RestrictionOverlayRestriction) {
        selectedRestriction = restriction
        currentRestriction = restriction
        turnRestrictionSelectionMode = false
        if (restriction is WeightRestriction) {
            editedWeight = parseWeight(restriction.weight, units)
        } else {
            editedWeight = null
        }
        if (restriction is TurnRestriction) {
            restriction.relation.tags.getShortRestrictionValue()
                ?.takeIf { it in turnRestrictionTypeList }
                ?.let { draftTurnType = it }
            draftSigned = restriction.relation.tags["implicit"] != "yes"
        }
    }

    LaunchedEffect(turnRestrictionSelectionMode) {
        setOverlayVisible?.invoke(!turnRestrictionSelectionMode)
    }

    DisposableEffect(Unit) {
        onDispose {
            setOverlayVisible?.invoke(true)
            mapMarkersCallback?.invoke(emptyList())
        }
    }

    LaunchedEffect(mapClick?.timestamp) {
        val click = mapClick ?: return@LaunchedEffect
        if (!turnRestrictionSelectionMode) return@LaunchedEffect
        val (toWay, viaNode) = findEligibleToWay(
            position = click.position,
            clickAreaSizeInMeters = click.clickAreaSizeInMeters,
            fromWay = way,
            mapDataSource = mapDataWithEditsSource,
        ) ?: return@LaunchedEffect

        val baseTags = (currentRestriction as? TurnRestriction)?.relation?.tags.orEmpty()
        val relation = createTurnRestrictionRelation(
            fromWay = way,
            toWay = toWay,
            viaNode = viaNode,
            restrictionType = draftTurnType,
            signed = draftSigned,
            baseTags = baseTags,
        )
        currentRestriction = TurnRestriction(relation)
        turnRestrictionSelectionMode = false
    }

    // Include member roles so from<->to swap refreshes via bearing / highlights.
    val turnMemberRoles = (currentRestriction as? TurnRestriction)
        ?.relation?.members?.joinToString { "${it.role}:${it.type}:${it.ref}" }
    LaunchedEffect(currentRestriction, turnMemberRoles, draftTurnType, wayGeometry, turnRestrictionSelectionMode) {
        if (turnRestrictionSelectionMode) {
            mapMarkersCallback?.invoke(listOf(Marker(wayGeometry)))
            return@LaunchedEffect
        }
        when (val restriction = currentRestriction) {
            is TurnRestriction -> {
                val markers = mutableListOf<Marker>()
                (relationGeometry(restriction.relation, mapDataWithEditsSource) ?: wayGeometry)
                    .let { markers += Marker(it) }
                viaBearingForTurnRestriction(restriction.relation, mapDataWithEditsSource)
                    ?.let { (position, bearing) ->
                        val type = restriction.relation.tags.getShortRestrictionValue() ?: draftTurnType
                        markers += Marker(
                            geometry = pointGeometry(position),
                            icon = getIconForTurnRestriction(type),
                            rotation = bearing,
                        )
                    }
                mapMarkersCallback?.invoke(markers)
            }
            is WeightRestriction -> mapMarkersCallback?.invoke(listOf(Marker(wayGeometry)))
            null -> mapMarkersCallback?.invoke(emptyList())
        }
    }

    OverlayForm(
        on = on,
        isComplete = isComplete,
        hasChanges = hasChanges,
        onClickOk = {
            when (val restriction = currentRestriction) {
                is WeightRestriction -> {
                    val weight = editedWeight
                        ?: parseWeight(restriction.weight, units)
                        ?: return@OverlayForm
                    val changes = restriction.way.tags.createChanges(way.tags)
                    changes[restriction.type.osmKey] = weight.toOsmString()
                    on(Edit(UpdateElementTagsAction(restriction.way, changes.create())))
                }
                is TurnRestriction -> {
                    val rel = restriction.relation
                    if (rel.id == 0L) {
                        on(Edit(CreateRelationAction(rel.tags, rel.members)))
                    } else {
                        val oldRelation = (selectedRestriction as? TurnRestriction)?.relation
                            ?: return@OverlayForm
                        on(
                            Edit(
                                UpdateElementTagsAction(
                                    oldRelation,
                                    rel.tags.createChanges(oldRelation.tags).create()
                                )
                            )
                        )
                    }
                }
                null -> Unit
            }
        },
        otherAnswers = {
            listOfNotNull(
                (currentRestriction as? TurnRestriction)
                    ?.takeIf { it.relation.id != 0L }
                    ?.let {
                        AnswerItem(stringResource(Res.string.restriction_overlay_show_details)) {
                            showRelationDetails = true
                        }
                    }
            )
        },
    ) {
        // BottomSheetFormScaffold (via OverlayForm) already owns verticalScroll;
        // nested verticalScroll here receives infinite max height and crashes.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OtherRestrictionsList(
                originalRestrictions = originalRestrictions,
                selectedRestriction = selectedRestriction,
                wayId = way.id,
                countryCode = countryInfo.countryCode.orEmpty(),
                onSelect = ::selectRestriction,
            )

            when {
                turnRestrictionSelectionMode -> {
                    TurnRestrictionEditor(
                        relation = (currentRestriction as? TurnRestriction)?.relation,
                        wayId = way.id,
                        draftTurnType = draftTurnType,
                        draftSigned = draftSigned,
                        selectionMode = true,
                        onTurnTypeChange = { draftTurnType = it },
                        onSignedChange = { draftSigned = it },
                        onSwap = {},
                        onExceptionsClick = {},
                        onOnlyForClick = {},
                        onConditionalClick = {},
                        conditionalLabel = stringResource(Res.string.access_manager_button_add_conditional),
                        infoText = stringResource(Res.string.restriction_overlay_select_way),
                        showOnlyFor = false,
                    )
                }
                currentRestriction is TurnRestriction -> {
                    val turn = currentRestriction as TurnRestriction
                    if (turn.relation.isSupportedTurnRestriction()) {
                        val infoText = turn.relation.tags
                            .filterKeys { key ->
                                key.startsWith("restriction:") &&
                                    onlyTurnRestriction.none { key.endsWith(it) }
                            }
                            .map { "${it.key} = ${it.value}" }
                            .joinToString("\n")
                        TurnRestrictionEditor(
                            relation = turn.relation,
                            wayId = way.id,
                            draftTurnType = draftTurnType,
                            draftSigned = draftSigned,
                            selectionMode = false,
                            onTurnTypeChange = { type ->
                                draftTurnType = type
                                val latest = (currentRestriction as? TurnRestriction)?.relation
                                    ?: turn.relation
                                currentRestriction = TurnRestriction(
                                    withTurnRestrictionType(latest, type)
                                )
                            },
                            onSignedChange = { signed ->
                                draftSigned = signed
                                val latest = (currentRestriction as? TurnRestriction)?.relation
                                    ?: turn.relation
                                currentRestriction = TurnRestriction(
                                    withImplicitSigned(latest, signed)
                                )
                            },
                            onSwap = {
                                val latest = (currentRestriction as? TurnRestriction)?.relation
                                    ?: return@TurnRestrictionEditor
                                if (!isTurnRestrictionFromToSwapAllowed(latest)) return@TurnRestrictionEditor
                                currentRestriction = TurnRestriction(withSwappedFromTo(latest))
                            },
                            onExceptionsClick = { showExceptionsDialog = true },
                            onOnlyForClick = { showOnlyForDialog = true },
                            onConditionalClick = {
                                val latestTurn = currentRestriction as? TurnRestriction ?: turn
                                if (infoText.isNotBlank()) {
                                    currentRestriction = removeConditional(latestTurn)
                                } else {
                                    showAddConditionalDialog = true
                                }
                            },
                            conditionalLabel = if (infoText.isNotBlank()) {
                                stringResource(Res.string.restriction_overlay_remove_conditional_restrictions)
                            } else {
                                stringResource(Res.string.access_manager_button_add_conditional)
                            },
                            infoText = infoText.ifBlank { null },
                            showOnlyFor = true,
                        )
                    } else {
                        UnsupportedTurnRestrictionInfo(
                            relation = turn.relation,
                            mapDataWithEditsSource = mapDataWithEditsSource,
                        )
                    }
                }
                currentRestriction is WeightRestriction -> {
                    val weightRestriction = currentRestriction as WeightRestriction
                    val restrictionInfo = weightRestriction.way.tags
                        .filterKeys { it.startsWith("${weightRestriction.type.osmKey}:") }
                        .map { "${it.key} = ${it.value}" }
                        .joinToString("\n")

                    MaxWeightSignForm(
                        type = weightRestriction.type,
                        weight = editedWeight,
                        onWeightChange = { newWeight ->
                            editedWeight = newWeight
                            currentRestriction = WeightRestriction(
                                way = weightRestriction.way,
                                type = weightRestriction.type,
                                weight = newWeight?.toOsmString().orEmpty(),
                            )
                        },
                        locale = locale,
                        selectableUnits = units,
                    )

                    Button2(
                        onClick = {
                            if (restrictionInfo.isNotBlank()) {
                                currentRestriction = removeConditional(weightRestriction)
                            } else {
                                showAddConditionalDialog = true
                            }
                        },
                        style = ButtonStyle.Text,
                    ) {
                        Text(
                            if (restrictionInfo.isNotBlank()) {
                                stringResource(Res.string.restriction_overlay_remove_conditional_restrictions)
                            } else {
                                stringResource(Res.string.access_manager_button_add_conditional)
                            }
                        )
                    }
                    if (restrictionInfo.isNotBlank()) {
                        Text(restrictionInfo, style = MaterialTheme.typography.body2)
                    }
                }
            }

            if (showAddButton) {
                Button2(onClick = { showAddRestrictionDialog = true }) {
                    Text(stringResource(Res.string.add))
                }
            }

            val showRemove = when (val restriction = currentRestriction) {
                is TurnRestriction -> restriction.relation.id != 0L
                is WeightRestriction -> true
                null -> false
            }
            if (showRemove && !turnRestrictionSelectionMode) {
                Button2(
                    onClick = { showDeleteDialog = true },
                    style = ButtonStyle.Outlined,
                ) {
                    Text(stringResource(Res.string.delete_confirmation))
                }
            }
        }
    }

    if (showAddRestrictionDialog) {
        SimpleItemSelectDialog(
            onDismissRequest = { showAddRestrictionDialog = false },
            columns = SimpleGridCells.Fixed(2),
            items = RestrictionType.entries,
            onSelected = { type ->
                when (type) {
                    RestrictionType.TURN -> {
                        turnRestrictionSelectionMode = true
                        draftSigned = true
                        draftTurnType = turnRestrictionTypeList.first()
                    }
                    RestrictionType.WEIGHT -> showWeightTypeDialog = true
                }
            },
            itemContent = { type ->
                val icon = when (type) {
                    RestrictionType.TURN -> Res.drawable.ic_overlay_restriction
                    RestrictionType.WEIGHT -> Res.drawable.quest_max_weight
                }
                Image(painterResource(icon), contentDescription = type.name)
            },
        )
    }

    if (showWeightTypeDialog) {
        val availableTypes = MaxWeightType.entries.filter { type ->
            originalRestrictions.none { it is WeightRestriction && it.type == type } &&
                type.getIcon(countryInfo.countryCode.orEmpty()) != null
        }
        SimpleItemSelectDialog(
            onDismissRequest = { showWeightTypeDialog = false },
            columns = SimpleGridCells.Fixed(2),
            items = availableTypes,
            onSelected = { type ->
                editedWeight = null
                currentRestriction = WeightRestriction(way, type, "")
            },
            itemContent = { type ->
                val icon = type.getIcon(countryInfo.countryCode.orEmpty())
                if (icon != null) Image(painterResource(icon), contentDescription = type.name)
            },
        )
    }

    if (showExceptionsDialog) {
        val turn = currentRestriction as? TurnRestriction
        if (turn != null) {
            ExceptionsMultiChoiceDialog(
                selected = turn.relation.tags["except"]
                    ?.split(";")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    .orEmpty()
                    .toSet(),
                onDismissRequest = { showExceptionsDialog = false },
                onConfirm = { exceptions ->
                    currentRestriction = TurnRestriction(
                        withExceptions(
                            turn.relation,
                            turnRestrictionExceptions.filter { it in exceptions }
                        )
                    )
                    showExceptionsDialog = false
                },
            )
        } else {
            showExceptionsDialog = false
        }
    }

    if (showOnlyForDialog) {
        val turn = currentRestriction as? TurnRestriction
        if (turn != null) {
            OnlyForDialog(
                currentOnly = currentOnlyFor(turn.relation),
                onDismissRequest = { showOnlyForDialog = false },
                onSelected = { onlyFor ->
                    currentRestriction = TurnRestriction(withOnlyFor(turn.relation, onlyFor))
                    showOnlyForDialog = false
                },
                onClear = {
                    currentRestriction = TurnRestriction(withOnlyFor(turn.relation, null))
                    showOnlyForDialog = false
                },
            )
        } else {
            showOnlyForDialog = false
        }
    }

    if (showAddConditionalDialog) {
        when (val restriction = currentRestriction) {
            is TurnRestriction -> {
                val tags = restriction.relation.tags
                val restrictionKey = when {
                    tags.containsKey("restriction") -> "restriction"
                    else -> tags.keys.firstOrNull { key ->
                        onlyTurnRestriction.any { key == "restriction:$it" }
                    } ?: "restriction"
                }
                val values = tags[restrictionKey]?.let { listOf(it, "none") }
                    ?: listOf(draftTurnType)
                AddConditionalDialog(
                    onDismissRequest = { showAddConditionalDialog = false },
                    keys = listOf("$restrictionKey:conditional"),
                    values = values,
                    numberOnly = false,
                    countryInfo = countryInfo,
                ) { key, value ->
                    val newTags = tags.toMutableMap()
                    newTags[key] = value
                    if (!value.startsWith("none")) newTags.remove(restrictionKey)
                    currentRestriction = TurnRestriction(restriction.relation.copy(tags = newTags))
                    showAddConditionalDialog = false
                }
            }
            is WeightRestriction -> {
                val weightKey = restriction.type.osmKey
                AddConditionalDialog(
                    onDismissRequest = { showAddConditionalDialog = false },
                    keys = listOf("$weightKey:conditional"),
                    values = null,
                    numberOnly = false,
                    countryInfo = countryInfo,
                ) { key, value ->
                    val newTags = restriction.way.tags.toMutableMap()
                    newTags[key] = value
                    if (!value.startsWith("none")) newTags.remove(weightKey)
                    currentRestriction = WeightRestriction(
                        way = restriction.way.copy(tags = newTags),
                        type = restriction.type,
                        weight = restriction.weight,
                    )
                    showAddConditionalDialog = false
                }
            }
            null -> showAddConditionalDialog = false
        }
    }

    if (showDeleteDialog) {
        when (val restriction = currentRestriction) {
            is TurnRestriction -> {
                if (restriction.relation.id == 0L) {
                    showDeleteDialog = false
                } else {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        text = { Text(stringResource(Res.string.quest_generic_confirmation_title)) },
                        buttonRow = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(Res.string.cancel))
                            }
                            TextButton(onClick = {
                                showDeleteDialog = false
                                on(Action.LeaveNote)
                            }) {
                                Text(stringResource(Res.string.leave_note))
                            }
                            TextButton(onClick = {
                                showDeleteDialog = false
                                on(Edit(DeleteRelationAction(restriction.relation)))
                            }) {
                                Text(stringResource(Res.string.osm_element_gone_confirmation))
                            }
                        },
                    )
                }
            }
            is WeightRestriction -> {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    text = { Text(stringResource(Res.string.quest_generic_confirmation_title)) },
                    buttonRow = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(Res.string.cancel))
                        }
                        TextButton(onClick = {
                            showDeleteDialog = false
                            on(Action.LeaveNote)
                        }) {
                            Text(stringResource(Res.string.leave_note))
                        }
                        TextButton(onClick = {
                            val changes = restriction.way.tags.createChanges(way.tags)
                            changes.remove(restriction.type.osmKey)
                            changes.keys
                                .filter { it.startsWith("${restriction.type.osmKey}:") }
                                .toList()
                                .forEach { changes.remove(it) }
                            showDeleteDialog = false
                            if (changes.hasChanges) {
                                on(Edit(UpdateElementTagsAction(restriction.way, changes.create())))
                            }
                        }) {
                            Text(stringResource(Res.string.quest_generic_confirmation_yes))
                        }
                    },
                )
            }
            null -> showDeleteDialog = false
        }
    }

    if (showRelationDetails) {
        val turn = currentRestriction as? TurnRestriction
        if (turn != null && turn.relation.id != 0L) {
            InfoDialog(
                onDismissRequest = { showRelationDetails = false },
                title = { Text(stringResource(Res.string.restriction_overlay_show_details)) },
                text = {
                    Text(relationDetailsText(turn.relation, mapDataWithEditsSource))
                },
            )
        } else {
            showRelationDetails = false
        }
    }
}

@Composable
private fun OtherRestrictionsList(
    originalRestrictions: List<RestrictionOverlayRestriction>,
    selectedRestriction: RestrictionOverlayRestriction?,
    wayId: Long,
    countryCode: String,
    onSelect: (RestrictionOverlayRestriction) -> Unit,
) {
    val others = originalRestrictions.filterNot { it == selectedRestriction }
    if (others.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(Res.string.restriction_overlay_other_restrictions))
        for (restriction in others) {
            val label = when (restriction) {
                is TurnRestriction -> restriction.relation.members
                    .filter { it.type == ElementType.WAY && it.ref == wayId }
                    .joinToString(", ") { it.role }
                is WeightRestriction -> restriction.weight
            }
            val icon = when (restriction) {
                is TurnRestriction ->
                    getIconForTurnRestriction(restriction.relation.tags.getShortRestrictionValue())
                is WeightRestriction ->
                    restriction.type.getIcon(countryCode) ?: Res.drawable.quest_max_weight
            }
            Button2(onClick = { onSelect(restriction) }, style = ButtonStyle.Outlined) {
                ImageWithLabel(
                    painter = painterResource(icon),
                    label = label,
                )
            }
        }
    }
}

@Composable
private fun TurnRestrictionEditor(
    relation: Relation?,
    wayId: Long,
    draftTurnType: String,
    draftSigned: Boolean,
    selectionMode: Boolean,
    onTurnTypeChange: (String) -> Unit,
    onSignedChange: (Boolean) -> Unit,
    onSwap: () -> Unit,
    onExceptionsClick: () -> Unit,
    onOnlyForClick: () -> Unit,
    onConditionalClick: () -> Unit,
    conditionalLabel: String,
    infoText: String?,
    showOnlyFor: Boolean,
) {
    val selectedType = relation?.tags?.getShortRestrictionValue()
        ?.takeIf { it in turnRestrictionTypeList }
        ?: draftTurnType
    val canSwap = relation != null && isTurnRestrictionFromToSwapAllowed(relation)
    val wayRole = relation?.let { wayRoleInTurnRestriction(it, wayId) }

    DropdownButton(
        items = turnRestrictionTypeList,
        selectedItem = selectedType,
        onSelectedItem = onTurnTypeChange,
        itemContent = { type ->
            ImageWithLabel(
                painter = painterResource(getIconForTurnRestriction(type)),
                label = type,
            )
        },
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = draftSigned,
                onValueChange = onSignedChange,
                role = Role.Switch,
            )
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.restriction_overlay_signed_switch),
            modifier = Modifier.weight(1f),
        )
        Switch(checked = draftSigned, onCheckedChange = onSignedChange)
    }

    if (!selectionMode && relation != null) {
        if (wayRole != null) {
            Text(
                text = wayRole,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
            )
        }

        val exceptionsArgs = relation.tags["except"]?.replace(";", ", ")
            ?: stringResource(Res.string.overlay_none)
        Button2(onClick = onExceptionsClick, style = ButtonStyle.Outlined) {
            Text(stringResource(Res.string.restriction_overlay_exceptions, exceptionsArgs))
        }

        if (showOnlyFor) {
            val onlyForText = currentOnlyFor(relation) ?: "-"
            Button2(onClick = onOnlyForClick, style = ButtonStyle.Outlined) {
                Text(stringResource(Res.string.restriction_overlay_only_for, onlyForText))
            }
        }

        // Historical: swap control only for new draft relations (id == 0), not existing ones
        if (canSwap) {
            Button2(onClick = onSwap, style = ButtonStyle.Outlined) {
                Text("from <-> to")
            }
        }

        Button2(onClick = onConditionalClick, style = ButtonStyle.Text) {
            Text(conditionalLabel)
        }
    }

    if (!infoText.isNullOrBlank()) {
        Text(
            text = infoText,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun UnsupportedTurnRestrictionInfo(
    relation: Relation,
    mapDataWithEditsSource: MapDataWithEditsSource,
) {
    val text = if (isRelationComplete(relation, mapDataWithEditsSource)) {
        stringResource(
            Res.string.restriction_overlay_relation_unsupported,
            relationDetailsText(relation, mapDataWithEditsSource),
        )
    } else {
        stringResource(Res.string.restriction_overlay_relation_incomplete)
    }
    Text(text)
}

@Composable
private fun ExceptionsMultiChoiceDialog(
    selected: Set<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    val selectedStates = remember(selected) {
        mutableStateMapOf<String, Boolean>().apply {
            turnRestrictionExceptions.forEach { put(it, it in selected) }
        }
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(Res.string.restriction_overlay_exceptions, ""),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
            )
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                turnRestrictionExceptions.forEach { value ->
                    val checked = selectedStates[value] == true
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checked,
                                onValueChange = { selectedStates[value] = it },
                            ),
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { selectedStates[value] = it },
                        )
                        Text(value, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        },
        buttonRow = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(
                onClick = { onConfirm(selectedStates.filterValues { it }.keys) }
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
    )
}

@Composable
private fun OnlyForDialog(
    currentOnly: String?,
    onDismissRequest: () -> Unit,
    onSelected: (String) -> Unit,
    onClear: () -> Unit,
) {
    var selected by remember(currentOnly) { mutableStateOf(currentOnly) }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(Res.string.restriction_overlay_only_for, currentOnly ?: "-"),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
            )
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                onlyTurnRestriction.forEach { value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = selected == value,
                                onValueChange = {
                                    selected = value
                                    onSelected(value)
                                },
                            ),
                    ) {
                        RadioButton(
                            selected = selected == value,
                            onClick = {
                                selected = value
                                onSelected(value)
                            },
                        )
                        Text(value, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        },
        buttonRow = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            if (currentOnly != null) {
                TextButton(onClick = onClear) {
                    Text(stringResource(Res.string.delete_confirmation))
                }
            }
        },
    )
}

private fun removeConditional(
    restriction: RestrictionOverlayRestriction,
): RestrictionOverlayRestriction = when (restriction) {
    is TurnRestriction -> {
        val newTags = restriction.relation.tags.toMutableMap()
        val oldConditionalKey = when {
            newTags.containsKey("restriction:conditional") -> "restriction:conditional"
            else -> newTags.keys.firstOrNull {
                it.startsWith("restriction:") && it.endsWith(":conditional")
            } ?: "restriction:conditional"
        }
        val baseKey = oldConditionalKey.substringBefore(":conditional")
        if (!newTags.containsKey(baseKey)) {
            newTags.getShortRestrictionValue()?.let { newTags[baseKey] = it }
        }
        newTags.remove(oldConditionalKey)
        TurnRestriction(restriction.relation.copy(tags = newTags))
    }
    is WeightRestriction -> {
        val newTags = restriction.way.tags.toMutableMap()
        newTags.remove("${restriction.type.osmKey}:conditional")
        WeightRestriction(
            way = restriction.way.copy(tags = newTags),
            type = restriction.type,
            weight = restriction.weight,
        )
    }
}

private fun parseWeight(
    weightString: String,
    units: List<WeightMeasurementUnit>,
): Weight? {
    if (weightString.isBlank()) return null
    val normalized = weightString.replace(',', '.')
    normalized.toDoubleOrNull()?.let {
        return Weight(it, units.firstOrNull() ?: WeightMeasurementUnit.METRIC_TON)
    }
    when {
        weightString.endsWith("lbs") -> {
            val w = weightString.substringBefore("lbs").trim().replace(',', '.').toDoubleOrNull()
                ?: return null
            val unit = units.firstOrNull { it == WeightMeasurementUnit.POUND }
                ?: WeightMeasurementUnit.POUND
            return Weight(w, unit)
        }
        weightString.endsWith("st") -> {
            val w = weightString.substringBefore("st").trim().replace(',', '.').toDoubleOrNull()
                ?: return null
            val unit = units.firstOrNull { it == WeightMeasurementUnit.SHORT_TON }
                ?: WeightMeasurementUnit.SHORT_TON
            return Weight(w, unit)
        }
        weightString.endsWith("t") -> {
            val w = weightString.substringBefore("t").trim().replace(',', '.').toDoubleOrNull()
                ?: return null
            return Weight(w, units.firstOrNull() ?: WeightMeasurementUnit.METRIC_TON)
        }
        else -> return null
    }
}

private fun relationDetailsText(
    relation: Relation,
    mapDataSource: MapDataWithEditsSource,
): String {
    val tagsText = relation.tags.entries
        .sortedBy { it.key }
        .joinToString("\n") { "${it.key} = ${it.value}" }
    val membersText = relation.members.joinToString("\n") { member ->
        val element = mapDataSource.get(member.type, member.ref)
        val details = element?.key?.toString() ?: "${member.type}/${member.ref}"
        "${member.role}: $details"
    }
    return "$tagsText\n\n$membersText"
}
