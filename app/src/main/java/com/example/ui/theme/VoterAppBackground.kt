package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoterAppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Saffron.copy(alpha = 0.08f),
                        Color.White,
                        GreenIndian.copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        // Draw the soft waves, digital circuits, and faint Ashoka Chakra
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // --- 1. Soft Wave Patterns (Indian Flag inspired curves) ---
            // Saffron Top Wave
            val saffronPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, height * 0.18f)
                cubicTo(
                    width * 0.25f, height * 0.12f,
                    width * 0.7f, height * 0.24f,
                    width, height * 0.15f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = saffronPath,
                color = Saffron.copy(alpha = 0.03f)
            )

            // Dynamic offset wavy layer (Saffron)
            val saffronPath2 = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, height * 0.14f)
                cubicTo(
                    width * 0.35f, height * 0.2f,
                    width * 0.65f, height * 0.08f,
                    width, height * 0.18f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = saffronPath2,
                color = Saffron.copy(alpha = 0.02f)
            )

            // Green Bottom Wave
            val greenPath = Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.82f)
                cubicTo(
                    width * 0.3f, height * 0.88f,
                    width * 0.75f, height * 0.76f,
                    width, height * 0.85f
                )
                lineTo(width, height)
                close()
            }
            drawPath(
                path = greenPath,
                color = GreenIndian.copy(alpha = 0.03f)
            )

            // Second wave at bottom (Green)
            val greenPath2 = Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.86f)
                cubicTo(
                    width * 0.25f, height * 0.78f,
                    width * 0.7f, height * 0.9f,
                    width, height * 0.82f
                )
                lineTo(width, height)
                close()
            }
            drawPath(
                path = greenPath2,
                color = GreenIndian.copy(alpha = 0.02f)
            )

            // --- 2. Faint Digital Circuit Overlays (Blockchain secure lines) ---
            val circuitColor = NavyIndia.copy(alpha = 0.03f)
            
            // Circuit 1 - Top Left
            drawLine(circuitColor, Offset(width * 0.05f, height * 0.22f), Offset(width * 0.25f, height * 0.22f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.25f, height * 0.22f), Offset(width * 0.35f, height * 0.28f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.35f, height * 0.28f), Offset(width * 0.35f, height * 0.35f), strokeWidth = 2f)
            drawCircle(circuitColor, radius = 5f, center = Offset(width * 0.35f, height * 0.35f))
            drawCircle(circuitColor, radius = 8f, center = Offset(width * 0.05f, height * 0.22f), style = Stroke(width = 1.5f))

            // Branch top left
            drawLine(circuitColor, Offset(width * 0.2f, height * 0.22f), Offset(width * 0.25f, height * 0.17f), strokeWidth = 2f)
            drawCircle(circuitColor, radius = 4f, center = Offset(width * 0.25f, height * 0.17f))

            // Circuit 2 - Center Right
            drawLine(circuitColor, Offset(width * 0.95f, height * 0.45f), Offset(width * 0.75f, height * 0.45f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.75f, height * 0.45f), Offset(width * 0.65f, height * 0.52f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.65f, height * 0.52f), Offset(width * 0.45f, height * 0.52f), strokeWidth = 2f)
            drawCircle(circuitColor, radius = 5f, center = Offset(width * 0.45f, height * 0.52f))
            drawCircle(circuitColor, radius = 8f, center = Offset(width * 0.95f, height * 0.45f), style = Stroke(width = 1.5f))

            // Circuit 3 - Bottom Left
            drawLine(circuitColor, Offset(width * 0.05f, height * 0.75f), Offset(width * 0.15f, height * 0.75f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.15f, height * 0.75f), Offset(width * 0.3f, height * 0.64f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.3f, height * 0.64f), Offset(width * 0.5f, height * 0.64f), strokeWidth = 2f)
            drawCircle(circuitColor, radius = 5f, center = Offset(width * 0.5f, height * 0.64f))

            // Branch bottom left
            drawLine(circuitColor, Offset(width * 0.22f, height * 0.69f), Offset(width * 0.27f, height * 0.74f), strokeWidth = 2f)
            drawCircle(circuitColor, radius = 4f, center = Offset(width * 0.27f, height * 0.74f))

            // Circuit 4 - Bottom Right
            drawLine(circuitColor, Offset(width * 0.95f, height * 0.78f), Offset(width * 0.8f, height * 0.78f), strokeWidth = 2f)
            drawLine(circuitColor, Offset(width * 0.8f, height * 0.78f), Offset(width * 0.7f, height * 0.70f), strokeWidth = 2f)
            drawCircle(circuitColor, radius = 5f, center = Offset(width * 0.7f, height * 0.70f))

            // --- 3. Ashoka Chakra Outline Symbol (Central Navy blue focus) ---
            val centerCol = NavyIndia.copy(alpha = 0.012f)
            val centerX = width * 0.5f
            val centerY = height * 0.5f
            val chakraRadius = kotlin.math.min(width, height) * 0.28f

            if (chakraRadius > 0) {
                // Outer ring
                drawCircle(
                    color = centerCol,
                    radius = chakraRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3.5f)
                )
                // Inner ring
                drawCircle(
                    color = centerCol,
                    radius = chakraRadius * 0.15f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f)
                )

                // 24 Spokes of Ashoka Chakra
                val spokeCount = 24
                val angleStep = 360f / spokeCount
                for (spoke in 0 until spokeCount) {
                    val angleRad = Math.toRadians((spoke * angleStep).toDouble())
                    val endX = (centerX + chakraRadius * cos(angleRad)).toFloat()
                    val endY = (centerY + chakraRadius * sin(angleRad)).toFloat()
                    drawLine(
                        color = centerCol,
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.5f
                    )
                }
            }
        }

        // UI screens are rendered transparently above this canvas layers
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
