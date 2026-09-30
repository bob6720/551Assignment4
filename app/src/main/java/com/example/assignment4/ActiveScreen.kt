package com.example.assignment4

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

    LazyColumn(modifier = modifier.fillMaxWidth().fillMaxHeight().background(color = Color.White))
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
            Text(String.format("1min AVG HR: %d", minute_hr.value.toInt()))
        }

        item {
            val minute_intensity = localViewModel.minuteIntensity.collectAsState()
            when(minute_intensity.value)
            {
                ActivityIntensityClassification.NONE -> Text("Workout: No Workout Detected")
                ActivityIntensityClassification.LOW -> Text("Workout: Low Intensity")
                ActivityIntensityClassification.MEDIUM -> Text("Workout: Medium Intensity")
                ActivityIntensityClassification.HIGH -> Text("Workout: High Intensity")
            }
        }

        item {
            val rolling_hr = viewModel.rollingHR.collectAsState()

            val textMeasurer = rememberTextMeasurer()

            Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val inner_height = size.height * 0.8f

                val left_offset = 100f
                val inner_width = size.width - left_offset

                val path = Path()
                val maxValue = 180
                rolling_hr.value.forEachIndexed { index, value ->
                    val x = (index * (inner_width / (rolling_hr.value.size - 1))) + left_offset
                    val y = inner_height - (value / maxValue) * inner_height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(path, Color.Blue, style = Stroke(width = 3f))

                drawLine(Color.Black, start = Offset(left_offset, inner_height), end = Offset(inner_width + left_offset, inner_height), strokeWidth = 10f)
                drawLine(Color.Black, start = Offset(left_offset, 0f), end = Offset(left_offset, inner_height), strokeWidth = 10f)

                val label_top = textMeasurer.measure(maxValue.toString())
                drawText(label_top, topLeft = Offset(left_offset - (label_top.size.width + 10f), 0f))

                val label_bottom = textMeasurer.measure(text="0")
                drawText(label_bottom, topLeft = Offset(left_offset - (label_bottom.size.width + 10f), inner_height - label_bottom.size.height))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val rolling_ecg = viewModel.rollingECG.collectAsState()
            Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val path = Path()
                val maxValue = 2.0f
                rolling_ecg.value.forEachIndexed { index, value ->
                    val x = index * (size.width / (rolling_ecg.value.size - 1))
                    val y = (size.height - (value / maxValue) * size.height) + 0.1f;
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color.Red, style = Stroke(width = 3f))

                drawLine(Color.Black, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 10f)
                drawLine(Color.Black, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 10f)
            }
        }
    }
}