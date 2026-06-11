package com.example.muzfit.ui.diet.fragment

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.muzfit.R
import com.example.muzfit.model.Meal
import com.example.muzfit.model.MealCategory
import com.example.muzfit.model.UserMeal

/* ------------------------------------------------------------------ *
 *  Syne font family (loaded once from the variable font resource).  *
 * ------------------------------------------------------------------ */

private val SyneFamily = FontFamily(
    Font(R.font.syne_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.syne_regular, FontWeight.ExtraBold, FontStyle.Normal)
)

/* ------------------------------------------------------------------ *
 *  Design tokens (kept inline — these mirror the visual constants   *
 *  from the design spec. No logic touched.)                          *
 * ------------------------------------------------------------------ */

private val SurfaceColor = Color(0xFF0F0E0F)            // muz_surface_l0
private val OnSurfaceColor = Color(0xFFFEFEFF)           // muz_on_surface
private val OnSurfaceDim = OnSurfaceColor.copy(alpha = 0.40f)
private val OnSurfaceBorder = OnSurfaceColor.copy(alpha = 0.07f)
private val LimeAccent = Color(0xFFC5F701)               // muz_primary_lime
private val DangerRed = Color(0xFFFF4B4B)
private val DangerRedBg = DangerRed.copy(alpha = 0.10f)
private val LimeAccentBorder = LimeAccent.copy(alpha = 0.20f)
private val LimeAccentBg = LimeAccent.copy(alpha = 0.08f)

private val CardCorner = 10.dp
private val CardBorderWidth = 1.dp

/* ------------------------------------------------------------------ *
 *  Data class for a single meal card.                                *
 *  We keep this minimal — the data is fed by the host Java fragment. *
 * ------------------------------------------------------------------ */

data class MealCardData(
    val userMeal: UserMeal,
    val name: String,
    val kcal: Int
)

/* ------------------------------------------------------------------ *
 *  Section header row.                                               *
 * ------------------------------------------------------------------ */

@Composable
private fun MealSectionHeader(label: String, totalKcal: Int, isEmpty: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label.uppercase(),
            color = OnSurfaceDim,
            fontSize = 11.sp,
            fontWeight = FontWeight(700),
            fontFamily = SyneFamily,
            letterSpacing = 1.5.sp
        )
        if (!isEmpty) {
            Text(
                text = "$totalKcal kcal",
                color = OnSurfaceDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Meal item card.                                                   *
 * ------------------------------------------------------------------ */

@Composable
private fun MealItemCard(
    data: MealCardData,
    onDelete: () -> Unit
) {
    var visible by remember(data.userMeal.id) { mutableStateOf(true) }
    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically(animationSpec = tween(200, easing = LinearEasing)) + fadeOut()
    ) {
        MealItemCardContent(
            data = data,
            onDeletePressed = {
                visible = false
                // Delay the actual deletion so the animation can play first.
                // Compose side doesn't own the data; the host will observe the change.
            },
            onDeleteConfirmed = onDelete
        )
    }
}

@Composable
private fun MealItemCardContent(
    data: MealCardData,
    onDeletePressed: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val iconColor = if (isPressed) DangerRed else OnSurfaceDim
    val iconBg = if (isPressed) DangerRedBg else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor, RoundedCornerShape(CardCorner))
            .border(CardBorderWidth, OnSurfaceBorder, RoundedCornerShape(CardCorner))
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Meal name — fills the left side, pushes kcal + delete to the right.
        Text(
            text = data.name,
            color = OnSurfaceColor,
            fontSize = 14.sp,
            fontWeight = FontWeight(500),
            fontFamily = SyneFamily,
            modifier = Modifier.weight(1f)
        )

        // Kcal value (monospace, dim) — right-aligned with the delete button.
        Text(
            text = "${data.kcal} kcal",
            color = OnSurfaceDim,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Delete button
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(if (isPressed) 0.95f else 1f)
                .background(iconBg, RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        onDeletePressed()
                        onDeleteConfirmed()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Delete",
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Empty-state row (dashed border, "add meal" affordance).            *
 * ------------------------------------------------------------------ */

@Composable
private fun MealEmptyState(label: String, onTap: () -> Unit) {
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
                onClick = onTap
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
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            fontFamily = SyneFamily
        )
    }
}

/* ------------------------------------------------------------------ *
 *  Whole section: header + (cards | empty state).                    *
 * ------------------------------------------------------------------ */

@Composable
private fun MealSection(
    title: String,
    items: List<MealCardData>,
    emptyStateLabel: String,
    onDelete: (UserMeal) -> Unit,
    onAdd: () -> Unit
) {
    val totalKcal = items.sumOf { it.kcal }
    val isEmpty = items.isEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        MealSectionHeader(title, totalKcal, isEmpty)
        Spacer(modifier = Modifier.height(10.dp))
        if (isEmpty) {
            MealEmptyState(emptyStateLabel, onTap = onAdd)
        } else {
            items.forEachIndexed { index, item ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                MealItemCard(data = item, onDelete = { onDelete(item.userMeal) })
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Public composable: stacked Colazione / Pranzo / Cena.             *
 * ------------------------------------------------------------------ */

@Composable
fun MealSectionsContent(
    colazione: List<MealCardData>,
    pranzo: List<MealCardData>,
    cena: List<MealCardData>,
    onDelete: (UserMeal) -> Unit,
    onAddColazione: () -> Unit,
    onAddPranzo: () -> Unit,
    onAddCena: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MealSection(
            title = "Colazione",
            items = colazione,
            emptyStateLabel = "Add breakfast",
            onDelete = onDelete,
            onAdd = onAddColazione
        )
        Spacer(modifier = Modifier.height(20.dp))
        MealSection(
            title = "Pranzo",
            items = pranzo,
            emptyStateLabel = "Add lunch",
            onDelete = onDelete,
            onAdd = onAddPranzo
        )
        Spacer(modifier = Modifier.height(20.dp))
        MealSection(
            title = "Cena",
            items = cena,
            emptyStateLabel = "Add dinner",
            onDelete = onDelete,
            onAdd = onAddCena
        )
    }
}

/* ------------------------------------------------------------------ *
 *  Static bridge: lets the existing Java fragment render this        *
 *  composable in a ComposeView without owning any ViewModel logic.   *
 * ------------------------------------------------------------------ */

object MealSectionsBridge {

    /**
     * Renders the redesigned meal sections in the given ComposeView.
     *
     * @param composeView         the ComposeView host
     * @param colazioneList       list of UserMeal objects logged for breakfast
     * @param pranzoList          list of UserMeal objects logged for lunch
     * @param cenaList            list of UserMeal objects logged for dinner
     * @param resolveMeal         callback that turns a UserMeal into the matching Meal
     *                            (returns null if the meal cannot be resolved — the card
     *                            is then silently skipped)
     * @param onDelete            called with the UserMeal to remove
     * @param onAddColazione      called when the user taps the empty/add state
     * @param onAddPranzo         same for lunch
     * @param onAddCena           same for dinner
     */
    @JvmStatic
    fun setContent(
        composeView: ComposeView,
        colazioneList: List<UserMeal>,
        pranzoList: List<UserMeal>,
        cenaList: List<UserMeal>,
        resolveMeal: (UserMeal) -> Meal?,
        onDelete: (UserMeal) -> Unit,
        onAddColazione: () -> Unit,
        onAddPranzo: () -> Unit,
        onAddCena: () -> Unit
    ) {
        fun toCards(list: List<UserMeal>): List<MealCardData> = list.mapNotNull { um ->
            val m = resolveMeal(um) ?: return@mapNotNull null
            MealCardData(
                userMeal = um,
                name = m.foodName ?: "",
                kcal = Math.round(m.calories).toInt()
            )
        }

        composeView.setContent {
            MealSectionsContent(
                colazione = toCards(colazioneList),
                pranzo = toCards(pranzoList),
                cena = toCards(cenaList),
                onDelete = onDelete,
                onAddColazione = onAddColazione,
                onAddPranzo = onAddPranzo,
                onAddCena = onAddCena
            )
        }
    }
}
