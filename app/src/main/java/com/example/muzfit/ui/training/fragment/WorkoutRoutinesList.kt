package com.example.muzfit.ui.training.fragment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.muzfit.R
import com.example.muzfit.model.WorkoutRoutine

/* ------------------------------------------------------------------ *
 *  Syne font family (same as MealSections).                          *
 * ------------------------------------------------------------------ */

private val SyneFamily = FontFamily(
    Font(R.font.syne_regular, FontWeight.Normal,   FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.Medium,   FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.Bold,     FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.ExtraBold, FontStyle.Normal)
)

/* ------------------------------------------------------------------ *
 *  Design tokens (mirror MealSections for visual consistency).       *
 * ------------------------------------------------------------------ */

private val SurfaceColor = Color(0xFF0F0E0F)
private val OnSurfaceColor = Color(0xFFFEFEFF)
private val OnSurfaceDim = OnSurfaceColor.copy(alpha = 0.40f)
private val OnSurfaceBorder = OnSurfaceColor.copy(alpha = 0.07f)
private val LimeAccent = Color(0xFFC5F701)
private val LimeAccentBorder = LimeAccent.copy(alpha = 0.20f)
private val LimeAccentBg = LimeAccent.copy(alpha = 0.08f)
private val DangerRed = Color(0xFFFF4B4B)
private val DangerRedBg = DangerRed.copy(alpha = 0.10f)
private val PressedBg = OnSurfaceColor.copy(alpha = 0.05f)

private val CardCorner = 10.dp
private val CardBorderWidth = 1.dp

/* ------------------------------------------------------------------ *
 *  Section header row.                                                *
 *  Label "ROUTINE" + total count of routines on the right.           *
 *  Hidden count when no routines.                                     *
 * ------------------------------------------------------------------ */

@Composable
private fun RoutineSectionHeader(count: Int) {
    val isEmpty = count == 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Routine".uppercase(),
            color = OnSurfaceDim,
            fontSize = 11.sp,
            fontWeight = FontWeight(700),
            fontFamily = SyneFamily,
            letterSpacing = 1.5.sp
        )
        if (!isEmpty) {
            Text(
                text = "$count",
                color = OnSurfaceDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Single routine card.                                               *
 *  - Surface bg, 1dp border white 7%, RoundedCornerShape(10.dp).     *
 *  - 14dp vertical / 16dp horizontal padding.                        *
 *  - Routine name: 14sp weight 500, white.                            *
 *  - Exercise count: 12sp monospace, white 40%.                       *
 *  - Edit + Delete icons (24×24dp) on the right, scale 0.95 on press.*
 * ------------------------------------------------------------------ */

@Composable
private fun RoutineCard(
    routine: WorkoutRoutine,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var visible by remember(routine.name) { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically(animationSpec = tween(200, easing = LinearEasing)) + fadeOut()
    ) {
        RoutineCardContent(
            routine = routine,
            selected = selected,
            onSelect = onSelect,
            onEdit = onEdit,
            onDelete = {
                visible = false
                onDelete()
            }
        )
    }
}

@Composable
private fun RoutineCardContent(
    routine: WorkoutRoutine,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rowInteraction = remember { MutableInteractionSource() }
    val rowPressed by rowInteraction.collectIsPressedAsState()

    val borderColor = if (selected) LimeAccentBorder else OnSurfaceBorder
    val bgColor = if (selected) LimeAccentBg else SurfaceColor

    val editInteraction = remember { MutableInteractionSource() }
    val editPressed by editInteraction.collectIsPressedAsState()
    val editIconColor = if (editPressed) LimeAccent else OnSurfaceDim
    val editBg = if (editPressed) LimeAccentBg else Color.Transparent

    val delInteraction = remember { MutableInteractionSource() }
    val delPressed by delInteraction.collectIsPressedAsState()
    val delIconColor = if (delPressed) DangerRed else OnSurfaceDim
    val delBg = if (delPressed) DangerRedBg else Color.Transparent

    val cardModifier = Modifier
        .fillMaxWidth()
        .background(bgColor, RoundedCornerShape(CardCorner))
        .border(
            width = if (selected) CardBorderWidth.times(2) else CardBorderWidth,
            brush = SolidColor(borderColor),
            shape = RoundedCornerShape(CardCorner)
        )
        .clickable(
            interactionSource = rowInteraction,
            indication = null,
            onClick = onSelect
        )
        .padding(vertical = 14.dp, horizontal = 16.dp)

    Row(
        modifier = cardModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name + exercise count column (weight 1)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routine.name,
                color = OnSurfaceColor,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                fontFamily = SyneFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = routine.exerciseSummary,
                color = OnSurfaceDim,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Edit button
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(if (editPressed) 0.95f else 1f)
                .background(editBg, RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = editInteraction,
                    indication = null,
                    onClick = onEdit
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit",
                tint = editIconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Delete button
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(if (delPressed) 0.95f else 1f)
                .background(delBg, RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = delInteraction,
                    indication = null,
                    onClick = onDelete
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete",
                tint = delIconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Empty-state row when no routines are saved.                        *
 *  Dashed-ish border, "+ Add routine" CTA.                            *
 * ------------------------------------------------------------------ */

@Composable
private fun EmptyRoutinesRow(onAdd: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor = if (isPressed) LimeAccentBorder else OnSurfaceBorder
    val bgColor = if (isPressed) LimeAccentBg else SurfaceColor
    val contentColor = if (isPressed) LimeAccent else OnSurfaceDim
    val iconColor = if (isPressed) LimeAccent else OnSurfaceDim

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(CardCorner))
            .border(
                width = CardBorderWidth,
                brush = SolidColor(borderColor),
                shape = RoundedCornerShape(CardCorner)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onAdd
            )
            .padding(vertical = 18.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Add routine",
            color = contentColor,
            fontSize = 13.sp,
            fontFamily = SyneFamily
        )
    }
}

/* ------------------------------------------------------------------ *
 *  Alphabet scrollbar (right-side, A-Z jumping).                     *
 *  Shows one row per unique first letter of routine names.            *
 *  Tapping a letter scrolls the list to the first routine starting   *
 *  with that letter. Hidden when there are too few routines.          *
 * ------------------------------------------------------------------ */

private fun firstLetterIndex(routines: List<WorkoutRoutine>, letter: Char): Int {
    val upper = letter.uppercaseChar()
    for (i in routines.indices) {
        val name = routines[i].name
        if (!name.isNullOrEmpty() && name[0].uppercaseChar() == upper) {
            return i
        }
    }
    return -1
}

@Composable
private fun AlphabetScrollbar(
    routines: List<WorkoutRoutine>,
    scrollState: androidx.compose.foundation.ScrollState,
    itemHeights: () -> IntArray
) {
    val letters = remember(routines) {
        routines
            .mapNotNull { it.name.firstOrNull()?.uppercaseChar() }
            .toSortedSet()
            .toList()
    }
    // Hide the bar when there are not enough routines to justify it
    if (routines.size < 6 || letters.size < 2) return

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(40.dp)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letters.forEach { letter ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val color = if (isPressed) LimeAccent else OnSurfaceDim
            Text(
                text = letter.toString(),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            val idx = firstLetterIndex(routines, letter)
                            if (idx >= 0) {
                                val heights = itemHeights()
                                val target = heights.take(idx).sum()
                                val maxScroll = scrollState.maxValue
                                val safe = target.coerceIn(0, maxScroll)
                                coroutineScope.launch {
                                    scrollState.scrollTo(safe)
                                }
                            }
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Public composable: full routines list.                            *
 * ------------------------------------------------------------------ */

@Composable
fun WorkoutRoutinesContent(
    routines: List<WorkoutRoutine>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onEdit: (WorkoutRoutine) -> Unit,
    onDelete: (WorkoutRoutine) -> Unit,
    onAdd: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Approximate per-item height for jump-to-letter math.
    // Card is 14dp+14dp+text height (about 64dp), plus 8dp spacer.
    val perItemHeightDp = 64 + 8
    val itemHeights: () -> IntArray = {
        // header (~22dp) + spacer 10dp + first item offset
        IntArray(routines.size) { i ->
            if (i == 0) (22 + 10) + perItemHeightDp
            else perItemHeightDp
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Padding-right on the scrollable column so routine cards never go under
        // the alphabet scrollbar (which is 40dp wide + 4dp end padding).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(end = 44.dp)
        ) {
            RoutineSectionHeader(count = routines.size)
            Spacer(modifier = Modifier.height(10.dp))
            if (routines.isEmpty()) {
                EmptyRoutinesRow(onAdd = onAdd)
            } else {
                routines.forEachIndexed { index, routine ->
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    RoutineCard(
                        routine = routine,
                        selected = index == selectedIndex,
                        onSelect = { onSelect(index) },
                        onEdit = { onEdit(routine) },
                        onDelete = { onDelete(routine) }
                    )
                }
            }
            // Bottom padding so the last card isn't clipped by FABs/buttons
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Alphabet scrollbar on the right edge, vertically centered
        if (routines.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            ) {
                AlphabetScrollbar(
                    routines = routines,
                    scrollState = scrollState,
                    itemHeights = itemHeights
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Java-friendly bridge.                                              *
 * ------------------------------------------------------------------ */

object WorkoutRoutinesBridge {

    @JvmStatic
    fun setContent(
        composeView: ComposeView,
        routines: List<WorkoutRoutine>,
        selectedIndex: Int,
        onSelect: (Int) -> Unit,
        onEdit: (WorkoutRoutine) -> Unit,
        onDelete: (WorkoutRoutine) -> Unit,
        onAdd: () -> Unit
    ) {
        composeView.setContent {
            WorkoutRoutinesContent(
                routines = routines,
                selectedIndex = selectedIndex,
                onSelect = onSelect,
                onEdit = onEdit,
                onDelete = onDelete,
                onAdd = onAdd
            )
        }
    }
}
