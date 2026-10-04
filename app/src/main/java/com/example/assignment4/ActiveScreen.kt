package com.example.assignment4

import androidx.annotation.RestrictTo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment4.Viewmodels.ActiveScreenViewModel
import com.example.assignment4.Viewmodels.ApplicationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
// UI update imports
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight


@Composable
fun ActiveScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
) {
    val localViewModel = viewModel.activeScreenViewModel as ActiveScreenViewModel
    val textMeasurer = rememberTextMeasurer()
    //val timestamp_converter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())
    val minute_intensity = localViewModel.minuteIntensity.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val minuteHr = localViewModel.minuteAvgHR.collectAsState()
    val minuteIntensity = localViewModel.minuteIntensity.collectAsState()
    val rollingHr = viewModel.rollingHR.collectAsState()
    val rollingEcg = viewModel.rollingECG.collectAsState()
    val rollingEnmo = viewModel.rollingENMO.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.placeholder2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.80f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.SwitchToHome() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("← Back", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Live Workout",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(72.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Stats cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Average HR card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.13f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Avg HR (1 min)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${minuteHr.value.toInt()} bpm",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Intensity card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.13f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Intensity",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (minute_intensity.value) {
                                        ActivityIntensityClassification.NONE -> "No Activity Detected"
                                        ActivityIntensityClassification.LOW -> "Low"
                                        ActivityIntensityClassification.MEDIUM -> "Medium"
                                        ActivityIntensityClassification.HIGH -> "High"
                                    },
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (minuteIntensity.value) {
                                        ActivityIntensityClassification.NONE -> Color.White.copy(
                                            alpha = 0.6f
                                        )

                                        ActivityIntensityClassification.LOW -> Color(0xFF81C784)
                                        ActivityIntensityClassification.MEDIUM -> Color(0xFFFFB74D)
                                        ActivityIntensityClassification.HIGH -> Color(0xFFE57373)
                                    }
                                )
                            }
                        }
                    }
                }


                // Heart Rate Graph ----------
                item {
                    GraphCard(title = "Heart Rate History") {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isLandscape) 220.dp else 260.dp)
                        ) {
                            val maxValue = (rollingHr.value.maxOrNull() ?: 170f) + 10f
                            val inner_height = size.height * 0.8f
                            val label_top = textMeasurer.measure("${maxValue.toInt()} bpm")
                            val label_bottom = textMeasurer.measure(text = "0bpm")
                            val left_offset =
                                maxOf(label_top.size.width, label_bottom.size.width).toFloat() + 12f
                            val innerWidth = size.width - left_offset

                            // Grid lines
                            for (i in 1..3) {
                                val y = inner_height * (i / 4f)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(left_offset, y),
                                    end = Offset(left_offset + innerWidth, y),
                                    strokeWidth = 1f
                                )
                            }

                            // Threshold lines
                            val restingY = inner_height - (viewModel.GetUserRestingHeartRate()
                                .toFloat() / maxValue) * inner_height
                            val maxY = inner_height - (viewModel.GetUserMaxHeartRate()
                                .toFloat() / maxValue) * inner_height

                            if(viewModel.GetUserRestingHeartRate() < maxValue)
                            {
                                drawLine(
                                    Color(0xFF81C784).copy(alpha = 0.7f),
                                    Offset(left_offset, restingY),
                                    Offset(left_offset + innerWidth, restingY),
                                    2f
                                )
                            }
                            if(viewModel.GetUserMaxHeartRate() < maxValue)
                            {
                                drawLine(
                                    Color(0xFFE57373).copy(alpha = 0.7f),
                                    Offset(left_offset, maxY),
                                    Offset(left_offset + innerWidth, maxY),
                                    2f
                                )
                            }

                            // HR path
                            if (rollingHr.value.size > 1) {
                                val path = Path()
                                rollingHr.value.forEachIndexed { index, value ->
                                    val x =
                                        (index * (innerWidth / (rollingHr.value.size - 1))) + left_offset
                                    val y = inner_height - (value / maxValue) * inner_height
                                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path, Color(0xFF64B5F6), style = Stroke(width = 3f))
                            }

                            // Axes
                            drawLine(
                                Color.White.copy(alpha = 0.4f),
                                Offset(left_offset, inner_height),
                                Offset(left_offset + innerWidth, inner_height),
                                2f
                            )
                            drawLine(
                                Color.White.copy(alpha = 0.4f),
                                Offset(left_offset, 0f),
                                Offset(left_offset, inner_height),
                                2f
                            )

                            // Labels
                            drawText(
                                label_top,
                                color = Color.White,
                                topLeft = Offset(left_offset - label_top.size.width - 8f, 0f)
                            )
                            drawText(
                                label_bottom,
                                color = Color.White,
                                topLeft = Offset(
                                    left_offset - label_bottom.size.width - 8f,
                                    inner_height - label_bottom.size.height
                                )
                            )
                        }
                    }
                }

                // ECG Graph -------------
                item {
                    GraphCard(title = "ECG Visualiser") {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isLandscape) 220.dp else 260.dp)
                        ) {
                            val inner_height = size.height * 0.8f
                            val maxValue = (rollingEcg.value.maxOrNull() ?: 2.0f) * 1.1f
                            val label_top = textMeasurer.measure("%.2fuV".format(maxValue))
                            val label_bottom = textMeasurer.measure("0.00uV")
                            val left_offset =
                                Math.max(label_top.size.width, label_bottom.size.width)
                                    .toFloat() + 12f
                            val inner_width = size.width - left_offset

                            // Grid
                            for (i in 1..3) {
                                val y = inner_height * (i / 4f)
                                drawLine(
                                    Color.White.copy(alpha = 0.15f),
                                    Offset(left_offset, y),
                                    Offset(left_offset + inner_width, y),
                                    1f
                                )
                            }

                            if (rollingEcg.value.size > 1) {
                                val path = Path()
                                rollingEcg.value.forEachIndexed { index, value ->
                                    val x =
                                        (index * (inner_width / (rollingEcg.value.size - 1))) + left_offset
                                    val y = inner_height - (value / maxValue) * inner_height
                                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path, Color(0xFFEF5350), style = Stroke(width = 2.5f))
                            }

                            drawLine(
                                Color.White.copy(alpha = 0.4f),
                                Offset(left_offset, inner_height),
                                Offset(left_offset + inner_width, inner_height),
                                2f
                            )
                            drawLine(
                                Color.White.copy(alpha = 0.4f),
                                Offset(left_offset, 0f),
                                Offset(left_offset, inner_height),
                                2f
                            )

                            drawText(
                                label_top,
                                color = Color.White,
                                topLeft = Offset(left_offset - label_top.size.width - 8f, 0f)
                            )

                            drawText(
                                label_bottom,
                                color = Color.White,
                                topLeft = Offset(
                                    left_offset - label_bottom.size.width - 8f,
                                    inner_height - label_bottom.size.height
                                )
                            )
                        }
                    }
                }

                // Movement Level Graph
                item {
                    GraphCard(title = "Movement Level") {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isLandscape) 220.dp else 260.dp)
                        ) {
                            val inner_height = size.height * 0.82f
                            val maxValue = (rollingEnmo.value.maxOrNull() ?: 380f) + 20f
                            val label_top = textMeasurer.measure("%.2fmG".format(maxValue))
                            val label_bottom = textMeasurer.measure("0.00mG")
                            val left_offset =
                                Math.max(label_top.size.width, label_bottom.size.width)
                                    .toFloat() + 12f
                            val inner_width = size.width - left_offset

                            for (i in 1..3) {
                                val y = inner_height * (i / 4f)
                                drawLine(
                                    Color.White.copy(alpha = 0.15f),
                                    Offset(left_offset, y),
                                    Offset(left_offset + inner_width, y),
                                    1f
                                )
                            }

                            if (rollingEnmo.value.size > 1) {
                                val path = Path()
                                rollingEnmo.value.forEachIndexed { index, value ->
                                    val x =
                                        (index * (inner_width / (rollingEnmo.value.size - 1))) + left_offset
                                    val y = inner_height - (value / maxValue) * inner_height
                                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path, Color(0xFFBA68C8), style = Stroke(width = 3f))

                                drawLine(
                                    Color.White.copy(alpha = 0.4f),
                                    Offset(left_offset, inner_height),
                                    Offset(left_offset + inner_width, inner_height),
                                    2f
                                )
                                drawLine(
                                    Color.White.copy(alpha = 0.4f),
                                    Offset(left_offset, 0f),
                                    Offset(left_offset, inner_height),
                                    2f
                                )

                                drawText(
                                    label_top,
                                    color = Color.White,
                                    topLeft = Offset(left_offset - label_top.size.width - 8f, 0f)
                                )

                                drawText(
                                    label_bottom,
                                    color = Color.White,
                                    topLeft = Offset(
                                        left_offset - label_bottom.size.width - 8f,
                                        inner_height - label_bottom.size.height
                                    )
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun GraphCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}