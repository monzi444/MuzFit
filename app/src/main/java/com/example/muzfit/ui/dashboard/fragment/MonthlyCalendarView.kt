package com.example.muzfit.ui.dashboard.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.muzfit.R
import com.example.muzfit.model.DashboardCalendarDay
import com.example.muzfit.model.DashboardCalendarDay.ActivityLevel
import java.util.Calendar
import java.util.Locale

// ─── Bridge ──────────────────────────────────────────────────────

fun setupMonthlyCalendar(
    composeView: ComposeView,
    calendarData: List<DashboardCalendarDay>,
    todayDay: Int, todayMonth: Int, todayYear: Int,
    selDay: Int, selMonth: Int, selYear: Int,
    displayMonth: Int, displayYear: Int,
    onDayClick: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    composeView.setContent {
        MonthlyCalendarGrid(
            calendarData = calendarData,
            todayDay = todayDay, todayMonth = todayMonth, todayYear = todayYear,
            selDay = selDay, selMonth = selMonth, selYear = selYear,
            displayMonth = displayMonth, displayYear = displayYear,
            onDayClick = onDayClick,
            onPrevMonth = onPrevMonth,
            onNextMonth = onNextMonth
        )
    }
}

// ─── Shared colours ──────────────────────────────────────────────

private data class CalCol(
    val surface: Color,
    val greenAccent: Color,
    val onSurface: Color,
    val orangeAccent: Color
)

@Composable
private fun calCols(): CalCol = CalCol(
    surface      = colorResource(R.color.muz_surface_l0),
    greenAccent  = colorResource(R.color.muz_primary_lime),
    onSurface    = colorResource(R.color.muz_on_surface),
    orangeAccent = colorResource(R.color.activity_medium)
)

// ─── Day-of-week labels ──────────────────────────────────────────

private val dayAbbrs: List<String> by lazy {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    List(7) {
        val label = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
            ?.take(2)?.uppercase() ?: ""
        cal.add(Calendar.DAY_OF_YEAR, 1)
        label
    }
}

private val monthNames: List<String> by lazy {
    listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
}

private val cellShape = RoundedCornerShape(10.dp)

// ─── Monthly calendar grid ───────────────────────────────────────

/**
 * Monthly calendar grid — month/year header, day-of-week header, 7×6 day cells.
 *
 * Visual language shared with [DietWeekCalendarView].
 */
@Composable
fun MonthlyCalendarGrid(
    calendarData: List<DashboardCalendarDay>,
    todayDay: Int, todayMonth: Int, todayYear: Int,
    selDay: Int, selMonth: Int, selYear: Int,
    displayMonth: Int, displayYear: Int,
    onDayClick: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val c = calCols()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // ── Month/year header with prev/next arrows ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PrevNextArrow(false, onClick = onPrevMonth)
            Text(
                text = "${monthNames.getOrElse(displayMonth) { "?" }} $displayYear",
                color = c.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                textAlign = TextAlign.Center
            )
            PrevNextArrow(true, onClick = onNextMonth)
        }

        Spacer(Modifier.height(8.dp))

        // ── Day-of-week header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayAbbrs.forEach { label ->
                Text(
                    text = label,
                    color = c.onSurface.copy(alpha = 0.35f),
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(34.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Day grid: 6 rows × 7 columns ──
        calendarData.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    val num = day.dayNumber
                    val isCurrent = day.isCurrentMonth
                    val isToday = isCurrent && num == todayDay && displayMonth == todayMonth && displayYear == todayYear
                    val isSel = isCurrent && num == selDay && displayMonth == selMonth && displayYear == selYear

                    val cellBg: Color
                    val cellFg: Color

                    when {
                        day.level == ActivityLevel.OVERFLOW -> {
                            cellBg = c.orangeAccent.copy(alpha = 0.15f)
                            cellFg = c.orangeAccent
                        }
                        day.level == ActivityLevel.GOAL -> {
                            cellBg = c.greenAccent.copy(alpha = 0.15f)
                            cellFg = c.greenAccent
                        }
                        !isCurrent -> {
                            cellBg = Color.Transparent
                            cellFg = c.onSurface.copy(alpha = 0.20f)
                        }
                        else -> {
                            cellBg = Color.Transparent
                            cellFg = c.onSurface.copy(alpha = 0.60f)
                        }
                    }

                    val finalFg = if (isToday) c.greenAccent else cellFg
                    val borderMod = if (isSel) Modifier.border(
                        1.dp, c.onSurface.copy(alpha = 0.50f), cellShape
                    ) else Modifier

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(cellShape)
                            .background(cellBg)
                            .then(borderMod)
                            .clickable(onClick = { onDayClick(num) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString(),
                            color = finalFg,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

// ─── Prev/Next Arrow Button ──────────────────────────────────────

@Composable
private fun PrevNextArrow(isNext: Boolean, onClick: () -> Unit) {
    val c = calCols()
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(c.onSurface.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isNext) "\u276F" else "\u276E",  // ❯ / ❮
            color = c.onSurface.copy(alpha = 0.60f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
