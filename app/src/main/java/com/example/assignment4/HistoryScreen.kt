package com.example.assignment4

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.assignment4.Viewmodels.ApplicationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
)
{
    val historicWorkouts = viewModel.historicWorkouts.collectAsState()

    LazyColumn(modifier = modifier.fillMaxWidth().background(color = Color.White))
    {
        val timestamp_converter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())

        item {
            Button(onClick = { viewModel.SwitchToHome() })
            {
                Text("Back")
            }
        }

        item {
            Text(text = "Previous Workouts")
        }

        items(historicWorkouts.value.size)
        {
            val workout = historicWorkouts.value[it]

            val start_time_label = timestamp_converter.format(Instant.ofEpochMilli(workout.startTimeStamp))
            val end_time_label = timestamp_converter.format(Instant.ofEpochMilli(workout.endTimeStamp))

            Button(onClick = { viewModel.SwitchToFinishedWorkout(workout) })
            {
                Text(text = String.format("Workout %d (%s-%s)", workout.id, start_time_label, end_time_label))
            }
        }
    }
}