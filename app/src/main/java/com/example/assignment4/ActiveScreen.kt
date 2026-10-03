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
@Composable
fun ActiveScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
)
{
    val localViewModel = viewModel.activeScreenViewModel as ActiveScreenViewModel
    val textMeasurer = rememberTextMeasurer()

    LazyColumn(modifier = modifier.fillMaxWidth().fillMaxHeight().padding(5.dp).background(color = Color.White))
    {
        val timestamp_converter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())

        item {
            Button(onClick = { viewModel.SwitchToHome() })
            {
                Text("Back")
            }
        }

        item {
            val minute_hr = localViewModel.minuteAvgHR.collectAsState()
            Text(String.format("Average Heart Rate (1min): %dbpm", minute_hr.value.toInt()))
        }

        item {
            val minute_intensity = localViewModel.minuteIntensity.collectAsState()
            when(minute_intensity.value)
            {
                ActivityIntensityClassification.NONE -> Text("Activity Intensity: No Activity Detected")
                ActivityIntensityClassification.LOW -> Text("Activity Intensity: Low Intensity")
                ActivityIntensityClassification.MEDIUM -> Text("Activity Intensity: Medium Intensity")
                ActivityIntensityClassification.HIGH -> Text("Activity Intensity: High Intensity")
            }
        }

        item {
            Text(modifier = Modifier.fillMaxWidth(), text = "Heart Rate History", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)

            val rolling_hr = viewModel.rollingHR.collectAsState()
            Canvas(modifier = Modifier.fillMaxWidth().padding(2.dp).height(300.dp)) {
                val maxValue = 180
                val inner_height = size.height * 0.8f

                val label_top = textMeasurer.measure(String.format("%dbpm", maxValue))
                val label_bottom = textMeasurer.measure(text = "0bpm")

                val left_offset = Math.max(label_top.size.width, label_bottom.size.width).toFloat() + 10.0f
                val inner_width = size.width - left_offset

                val path = Path()
                rolling_hr.value.forEachIndexed { index, value ->
                    val x = (index * (inner_width / (rolling_hr.value.size - 1))) + left_offset
                    val y = inner_height - (value / maxValue) * inner_height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(path, Color.Blue, style = Stroke(width = 3f))

                // Draw axis lines
                drawLine(Color.Black, start = Offset(left_offset, inner_height), end = Offset(inner_width + left_offset, inner_height), strokeWidth = 10f)
                drawLine(Color.Black, start = Offset(left_offset, 0f), end = Offset(left_offset, inner_height), strokeWidth = 10f)

                // Draw threshold lines

                val lower_threshold_y = inner_height - ((viewModel.GetUserRestingHeartRate().toFloat() / maxValue) * inner_height)
                val upper_threshold_y = inner_height - ((viewModel.GetUserMaxHeartRate().toFloat() / maxValue) * inner_height)
                drawLine(Color.Green, start = Offset(left_offset, lower_threshold_y), end = Offset(inner_width + left_offset, lower_threshold_y), strokeWidth = 5f)
                drawLine(Color.Red, start = Offset(left_offset, upper_threshold_y), end = Offset(inner_width + left_offset, upper_threshold_y), strokeWidth = 5f)

                drawText(label_top, topLeft = Offset(left_offset - (label_top.size.width + 10f), 0f))

                drawText(label_bottom, topLeft = Offset(left_offset - (label_bottom.size.width + 10f), inner_height - label_bottom.size.height))
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(modifier = Modifier.fillMaxWidth(), text = "ECG Visualiser", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)

            val rolling_ecg = viewModel.rollingECG.collectAsState()
            Canvas(modifier = Modifier.fillMaxWidth().padding(2.dp).height(300.dp)) {
                val inner_height = size.height * 0.8f

                val maxValue = 2.0f
                val label_top = textMeasurer.measure(String.format("%.2fuV", maxValue))
                val label_bottom = textMeasurer.measure(text="0.00uV")

                val left_offset = Math.max(label_top.size.width, label_bottom.size.width).toFloat() + 10.0f
                val inner_width = size.width - left_offset

                val path = Path()
                rolling_ecg.value.forEachIndexed { index, value ->
                    val x = (index * (inner_width / (rolling_ecg.value.size - 1))) + left_offset
                    val y = (inner_height - (value / maxValue) * inner_height);
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color.Red, style = Stroke(width = 3f))

                drawLine(Color.Black, start = Offset(left_offset, inner_height), end = Offset(inner_width + left_offset, inner_height), strokeWidth = 10f)
                drawLine(Color.Black, start = Offset(left_offset, 0f), end = Offset(left_offset, inner_height), strokeWidth = 10f)

                drawText(label_top, topLeft = Offset(left_offset - (label_top.size.width + 10f), 0f))

                drawText(label_bottom, topLeft = Offset(left_offset - (label_bottom.size.width + 10f), inner_height - label_bottom.size.height))
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(modifier = Modifier.fillMaxWidth(), text = "Movement Level", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)

            val rolling_enmo = viewModel.rollingENMO.collectAsState()
            Canvas(modifier = Modifier.fillMaxWidth().padding(2.dp).height(300.dp)) {
                val inner_height = size.height * 0.8f

                val maxValue = 400.0f
                val label_top = textMeasurer.measure(String.format("%.2fmG", maxValue))
                val label_bottom = textMeasurer.measure(text="0.00mG")

                val left_offset = Math.max(label_top.size.width, label_bottom.size.width).toFloat() + 10.0f
                val inner_width = size.width - left_offset

                val path = Path()
                rolling_enmo.value.forEachIndexed { index, value ->
                    val x = (index * (inner_width / (rolling_enmo.value.size - 1))) + left_offset
                    val y = (inner_height - (value / maxValue) * inner_height);
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color.Red, style = Stroke(width = 3f))

                drawLine(Color.Black, start = Offset(left_offset, inner_height), end = Offset(inner_width + left_offset, inner_height), strokeWidth = 10f)
                drawLine(Color.Black, start = Offset(left_offset, 0f), end = Offset(left_offset, inner_height), strokeWidth = 10f)

                drawText(label_top, topLeft = Offset(left_offset - (label_top.size.width + 10f), 0f))

                drawText(label_bottom, topLeft = Offset(left_offset - (label_bottom.size.width + 10f), inner_height - label_bottom.size.height))
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}