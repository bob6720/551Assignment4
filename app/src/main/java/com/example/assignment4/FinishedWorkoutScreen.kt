package com.example.assignment4
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.assignment4.Entities.Workout
import com.example.assignment4.Viewmodels.ApplicationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun FinishedWorkoutScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
    workout: Workout?
)
{
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight().background(color = Color.White))
    {
        if(workout == null)
        {
            viewModel.SwitchToHistory()
        }

        val timestamp_converter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())

        Button(onClick = { viewModel.SwitchToHistory() })
        {
            Text("Back")
        }

        val start_time_label = timestamp_converter.format(Instant.ofEpochMilli(workout!!.startTimeStamp))
        val end_time_label = timestamp_converter.format(Instant.ofEpochMilli(workout.endTimeStamp))

        Text(text = String.format("Workout %d (%s-%s)", workout.id, start_time_label, end_time_label))

        Text(text = String.format("Average HR: %f", workout.hrAverage))
        Text(text = String.format("Max HR: %f", workout.hrMax))
        Text(text = String.format("Min HR: %f", workout.hrMin))
    }
}