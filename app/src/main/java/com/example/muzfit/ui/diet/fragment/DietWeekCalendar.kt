package com.example.muzfit.ui.diet.fragment

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
import java.util.Calendar
import java.util.Locale

/**
 * Key = start-of-day millis, value = progress toward calorie goal.
 * 0.0 = no meals, 0.63 = 63 % of goal, 1.0 = at goal, > 1.05 = over target (orange).
 */
typealias DayProgressMap = Map<Long, Float>

// ─── Bridge ──────────────────────────────────────────────────────

fun setupDietWeekCalendar(
    composeView: ComposeView,
    weekStartMillis: Long,
    selectedDateMillis: Long?,
    dayProgressMap: DayProgressMap,
    onDayClick: (Long) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    composeView.setContent {
        DietWeekCalendarView(
            weekStartMillis = weekStartMillis,
            selectedDateMillis = selectedDateMillis,
            dayProgressMap = dayProgressMap,
            onDayClick = onDayClick,
            onPrevWeek = onPrevWeek,
            onNextWeek = onNextWeek
        )
    }
}

private fun startOfDay(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun buildWeekDays(
    weekStartMillis: Long,
    dayProgressMap: DayProgressMap
): List<WeekDay> {
    val todayCal = Calendar.getInstance()
    val todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR)
    val todayYear = todayCal.get(Calendar.YEAR)

    val cal = Calendar.getInstance().apply { timeInMillis = weekStartMillis }
    val days = mutableListOf<WeekDay>()

    for (i in 0 until 7) {
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        val isToday = year == todayYear && dayOfYear == todayDayOfYear
        val dayStart = startOfDay(cal.timeInMillis)

        days.add(
            WeekDay(
                dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                dateMillis = dayStart,
                isToday = isToday,
                progress = dayProgressMap[dayStart] ?: 0f
            )
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return days
}

data class WeekDay(
    val dayNumber: Int,
    val dateMillis: Long,
    val isToday: Boolean,
    /** 0 = no meals, 0.63 = 63 % toward goal, 1.0 = at goal, > 1.05 = over target */
    val progress: Float
)

/** Abbreviated day-of-week labels (Mon, Tue … Sun). */
private val dayLabels: List<String> by lazy {
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

// ─── Shared colours ─────────────────────────────────────────────

@Composable
private fun colors() = CalendarColors(
    surface      = colorResource(R.color.muz_surface_l0),
    greenAccent  = colorResource(R.color.muz_primary_lime),
    onSurface    = colorResource(R.color.muz_on_surface),
    orangeAccent = colorResource(R.color.activity_medium)
)

data class CalendarColors(
    val surface: Color,
    val greenAccent: Color,
    val onSurface: Color,
    val orangeAccent: Color
)

private fun containerBorder(colors: CalendarColors): Color =
    colors.onSurface.copy(alpha = 0.07f)

private val cellShape = RoundedCornerShape(10.dp)

// ─── Weekly strip ────────────────────────────────────────────────

@Composable
fun DietWeekCalendarView(
    weekStartMillis: Long,
    selectedDateMillis: Long?,
    dayProgressMap: DayProgressMap,
    onDayClick: (Long) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val days = buildWeekDays(weekStartMillis, dayProgressMap)
    val selectedDayStart = selectedDateMillis?.let { startOfDay(it) }
    val c = colors()

    val cal = Calendar.getInstance().apply { timeInMillis = weekStartMillis }
    val headerMonth = monthNames.getOrElse(cal.get(Calendar.MONTH)) { "?" }
    val headerYear = cal.get(Calendar.YEAR)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(2.dp, containerBorder(c), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // ── Month/year header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PrevNextArrow(false, onClick = onPrevWeek)
            Text(
                text = "$headerMonth $headerYear",
                color = c.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                textAlign = TextAlign.Center
            )
            PrevNextArrow(true, onClick = onNextWeek)
        }

        Spacer(Modifier.height(8.dp))

        // ── Day-of-week header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { label ->
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

        // ── Day number row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEach { day ->
                val isSelected = day.dateMillis == selectedDayStart
                val dayNumColor = if (day.progress > 0f) {
                        val pa = (day.progress * 0.75f).coerceIn(0.10f, 0.75f)
                        val t = (pa - 0.10f) / 0.65f
                        Color(1f - t, 1f - t, 1f - t)
                    } else if (day.isToday) c.greenAccent
                    else c.onSurface.copy(alpha = 0.60f)

                val borderMod = if (isSelected) Modifier.border(
                    1.dp, c.onSurface.copy(alpha = 0.50f), cellShape
                ) else Modifier

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(34.dp)
                ) {
                    // Day number box with progress background
                    val cellBg = if (day.progress > 1.05f) {
                        c.orangeAccent.copy(alpha = 0.50f)
                    } else if (day.progress > 0f) {
                        val alpha = (day.progress * 0.75f).coerceIn(0.10f, 0.75f)
                        c.greenAccent.copy(alpha = alpha)
                    } else {
                        Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(cellShape)
                            .background(cellBg)
                            .then(borderMod)
                            .clickable(onClick = { onDayClick(day.dateMillis) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayNumber.toString(),
                            color = dayNumColor,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── Prev/Next Arrow Button ──────────────────────────────────────

@Composable
private fun PrevNextArrow(isNext: Boolean, onClick: () -> Unit) {
    val c = colors()
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(c.onSurface.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isNext) "\u276F" else "\u276E",
            color = c.onSurface.copy(alpha = 0.60f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
