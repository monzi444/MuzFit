package com.example.muzfit.ui.quick

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.muzfit.R

@Composable
fun QuickOverlay(
    show: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.70f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDismiss() }
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = show,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable(enabled = false) {}
                        .padding(bottom = 110.dp, start = 32.dp, end = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickActionButton("Add Quick Meal") {}
                        QuickActionButton("Start Workout") {}
                        QuickActionButton("Log Body Weight") {}
                        QuickActionButton("Update Goal") {}
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowDark = colorResource(id = R.color.muz_skeuo_shadow_dark)
    val shadowLight = colorResource(id = R.color.muz_skeuo_shadow_light)
    val containerColor = colorResource(id = R.color.muz_container_l2)
    val textColor = colorResource(id = R.color.muz_primary_lime)

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(50.dp),
        elevation = null, // Custom skeuomorphic elevation handled via drawBehind
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        style = PaintingStyle.Fill
                    }
                    val frameworkPaint = paint.asFrameworkPaint()
                    
                    if (isPressed) {
                        // Concave (pressed) state
                        // Top inner shadow
                        frameworkPaint.color = shadowDark.copy(alpha = 0.5f).toArgb()
                        frameworkPaint.setShadowLayer(4.dp.toPx(), 0f, 2.dp.toPx(), shadowDark.toArgb())
                        canvas.drawPath(
                            Path().apply {
                                addRoundRect(
                                    androidx.compose.ui.geometry.RoundRect(
                                        0f, 0f, size.width, size.height,
                                        androidx.compose.ui.geometry.CornerRadius(50.dp.toPx())
                                    )
                                )
                            },
                            paint
                        )
                    } else {
                        // Convex (normal) state
                        // Bottom shadow
                        frameworkPaint.color = containerColor.toArgb()
                        frameworkPaint.setShadowLayer(8.dp.toPx(), 0f, 4.dp.toPx(), shadowDark.toArgb())
                        canvas.drawPath(
                            Path().apply {
                                addRoundRect(
                                    androidx.compose.ui.geometry.RoundRect(
                                        0f, 0f, size.width, size.height - 2.dp.toPx(),
                                        androidx.compose.ui.geometry.CornerRadius(50.dp.toPx())
                                    )
                                )
                            },
                            paint
                        )
                        // Top light edge highlight
                        frameworkPaint.setShadowLayer(1.dp.toPx(), 0f, (-1).dp.toPx(), shadowLight.toArgb())
                        canvas.drawPath(
                            Path().apply {
                                addRoundRect(
                                    androidx.compose.ui.geometry.RoundRect(
                                        0f, 1.dp.toPx(), size.width, size.height - 2.dp.toPx(),
                                        androidx.compose.ui.geometry.CornerRadius(50.dp.toPx())
                                    )
                                )
                            },
                            paint
                        )
                    }
                }
            }
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        )
    }
}
