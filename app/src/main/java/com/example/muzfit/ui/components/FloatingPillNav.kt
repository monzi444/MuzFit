package com.example.muzfit.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.muzfit.R

/**
 * Data class representing a Navigation Tab.
 */
data class NavTab(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun FloatingPillNav(
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit
) {
    val tabs = listOf(
        NavTab("home", "Home", LinearIcons.Home),
        NavTab("diet", "Diet", LinearIcons.Diet),
        // Central FAB placeholder
        NavTab("workout", "Workout", LinearIcons.Workout),
        NavTab("profile", "Profile", LinearIcons.Profile)
    )

    val glassBgColor = Color(0x851C1E1E) // rgba(28, 30, 30, 0.52)
    val limeColor = colorResource(id = R.color.muz_primary_lime)
    val onSurfaceVariant = colorResource(id = R.color.muz_on_surface_variant)
    val glassBorderColor = colorResource(id = R.color.muz_glass_border)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Blur Background Layer (Separate from content to avoid blurring icons/text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .width(380.dp)
                    .height(72.dp)
                    .blur(20.dp)
                    .clip(CircleShape)
                    .background(glassBgColor.copy(alpha = 0.4f))
            )
        }

        // Main Pill Container
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .height(72.dp)
                .border(1.dp, glassBorderColor, CircleShape),
            color = glassBgColor,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Tabs
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    NavItem(tabs[0], selectedTabId == tabs[0].id) { onTabSelected(tabs[0].id) }
                    NavItem(tabs[1], selectedTabId == tabs[1].id) { onTabSelected(tabs[1].id) }
                }

                // Central FAB Space
                Spacer(modifier = Modifier.width(64.dp))

                // Right Tabs
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    NavItem(tabs[2], selectedTabId == tabs[2].id) { onTabSelected(tabs[2].id) }
                    NavItem(tabs[3], selectedTabId == tabs[3].id) { onTabSelected(tabs[3].id) }
                }
            }
        }

        // Central FAB (Floating outside the main Row for layering)
        Box(
            modifier = Modifier
                .padding(bottom = 11.dp) // Adjusted to center relative to 72dp height
                .size(50.dp)
                .drawBehind {
                    // Glow effect
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(limeColor.copy(alpha = 0.4f), Color.Transparent),
                            center = center,
                            radius = size.width * 0.8f
                        )
                    )
                }
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(limeColor, limeColor.copy(alpha = 0.8f))
                    )
                )
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LinearIcons.Plus,
                contentDescription = "Quick Action",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NavItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = colorResource(id = R.color.muz_primary_lime)
    val inactiveColor = colorResource(id = R.color.muz_on_surface_variant)

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = tab.label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) activeColor else inactiveColor
        )
        
        // Active Indicator Dot
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .background(
                    color = if (isSelected) activeColor else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}

/**
 * Custom Linear Icons inspired by Lucide/Feather, forced to 1.6.dp stroke.
 */
object LinearIcons {
    private const val StrokeWidth = 1.6f

    val Home = ImageVector.Builder(
        name = "Home",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(3f, 9f)
        lineTo(12f, 2f)
        lineTo(21f, 9f)
        lineTo(21f, 20f)
        curveTo(21f, 20.53f, 20.79f, 21.04f, 20.41f, 21.41f)
        curveTo(20.04f, 21.79f, 19.53f, 22f, 19f, 22f)
        lineTo(5f, 22f)
        curveTo(4.47f, 22f, 3.96f, 21.79f, 3.59f, 21.41f)
        curveTo(3.21f, 21.04f, 3f, 20.53f, 3f, 20f)
        lineTo(3f, 9f)
        close()
        moveTo(9f, 22f)
        lineTo(9f, 12f)
        lineTo(15f, 12f)
        lineTo(15f, 22f)
    }.build()

    val Diet = ImageVector.Builder(
        name = "Diet",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(6f, 2f)
        lineTo(6f, 10f)
        curveTo(6f, 11.1f, 6.9f, 12f, 8f, 12f)
        lineTo(9f, 12f)
        lineTo(9f, 22f)
        moveTo(18f, 2f)
        lineTo(18f, 22f)
        moveTo(6f, 2f)
        lineTo(10f, 2f)
        moveTo(8f, 2f)
        lineTo(8f, 5f)
    }.build()

    val Workout = ImageVector.Builder(
        name = "Workout",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(18f, 8f)
        lineTo(22f, 8f)
        moveTo(2f, 8f)
        lineTo(6f, 8f)
        moveTo(18f, 16f)
        lineTo(22f, 16f)
        moveTo(2f, 16f)
        lineTo(6f, 16f)
        moveTo(6f, 12f)
        lineTo(18f, 12f)
        moveTo(6f, 4f)
        lineTo(6f, 20f)
        moveTo(18f, 4f)
        lineTo(18f, 20f)
    }.build()

    val Profile = ImageVector.Builder(
        name = "Profile",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(20f, 21f)
        lineTo(20f, 19f)
        curveTo(20f, 17.94f, 19.58f, 16.92f, 18.83f, 16.17f)
        curveTo(18.08f, 15.42f, 17.06f, 15f, 16f, 15f)
        lineTo(8f, 15f)
        curveTo(6.94f, 15f, 5.92f, 15.42f, 5.17f, 16.17f)
        curveTo(4.42f, 16.92f, 4f, 17.94f, 4f, 19f)
        lineTo(4f, 21f)
        moveTo(16f, 7f)
        curveTo(16f, 9.21f, 14.21f, 11f, 12f, 11f)
        curveTo(9.79f, 11f, 8f, 9.21f, 8f, 7f)
        curveTo(8f, 4.79f, 9.79f, 3f, 12f, 3f)
        curveTo(14.21f, 3f, 16f, 4.79f, 16f, 7f)
        close()
    }.build()

    val Plus = ImageVector.Builder(
        name = "Plus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = StrokeWidth, // Strictly 1.6.dp as per requirements
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 5f)
        lineTo(12f, 19f)
        moveTo(5f, 12f)
        lineTo(19f, 12f)
    }.build()
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E0F)
@Composable
fun FloatingPillNavPreview() {
    var selectedTab by remember { mutableStateOf("home") }
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        FloatingPillNav(
            selectedTabId = selectedTab,
            onTabSelected = { selectedTab = it },
            onFabClick = {}
        )
    }
}
