package com.example.assignment4

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.assignment4.Viewmodels.ActiveScreenViewModel
import com.example.assignment4.Viewmodels.ApplicationViewModel
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
                ActivityIntensityClassification.NONE -> {}
                ActivityIntensityClassification.LOW -> Text("Workout: Low Intensity")
                ActivityIntensityClassification.MEDIUM -> Text("Workout: Medium Intensity")
                ActivityIntensityClassification.HIGH -> Text("Workout: High Intensity")
            }
        }

        item {
            val rolling_hr = viewModel.rollingHR.collectAsState()

            Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val path = Path()
                val maxValue = 180
                rolling_hr.value.forEachIndexed { index, value ->
                    val x = index * (size.width / (rolling_hr.value.size - 1))
                    val y = size.height - (value / maxValue) * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color.Blue, style = Stroke(width = 3f))
            }

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
            }
        }
    }
}