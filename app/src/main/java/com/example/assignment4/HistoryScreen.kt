package com.example.assignment4

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable // Can delete this
import androidx.compose.runtime.collectAsState // Can delete this
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
import android.content.res.Configuration
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import androidx.compose.runtime.*


// To acknowledge and suppress compiler warnings with M3API
@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
)
{
    val historicWorkouts = viewModel.historicWorkouts.collectAsState()

    // Get orientation of phone
    val configuration = LocalConfiguration.current
    // If it's landscape orientation
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Sorting and filtering state (the date)
    var sortNewestFirst by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Get time (from import)
    val timestampConverter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    // Format so it can be read
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    // Apply filter and then sort data by filter
    val displayedWorkouts = remember(historicWorkouts.value, sortNewestFirst, selectedDate) {
        var list = historicWorkouts.value

        // Filter by selected date (if any)
        selectedDate?.let { date ->
            list = list.filter { workout ->
                val workoutDate = Instant.ofEpochMilli(workout.startTimeStamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                workoutDate == date
            }
        }

        // Sort by start time
        if (sortNewestFirst) {
            list.sortedByDescending { it.startTimeStamp }
        } else {
            list.sortedBy { it.startTimeStamp }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Grey overlay for each entry
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top line --------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                    text = "Previous Workouts",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                // Balance the back button otherwise it's off the screen
                Spacer(modifier = Modifier.width(72.dp))
            }

            // Second Line -------------------- (Sort and Date filter controls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort button
                OutlinedButton(
                    onClick = { sortNewestFirst = !sortNewestFirst },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (sortNewestFirst) "Newest first" else "Oldest first",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Date picker button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),   // ← fixed
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = selectedDate?.format(dateFormatter) ?: "Filter by date",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Clear date filter
                if (selectedDate != null) {
                    TextButton(
                        onClick = { selectedDate = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.85f))
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // The rest -------------------- (data entry rows)
            if (displayedWorkouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (selectedDate != null) "No workouts on this date" else "No workouts yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedDate != null)
                                    "Try selecting a different date"
                                else
                                    "Completed workouts will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            // If it's not empty then show the data entries
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(displayedWorkouts) { workout ->
                        val startLabel = timestampConverter.format(
                            Instant.ofEpochMilli(workout.startTimeStamp)
                        )
                        val endLabel = timestampConverter.format(
                            Instant.ofEpochMilli(workout.endTimeStamp)
                        )

                        WorkoutHistoryCard(
                            workoutId = workout.id,
                            startLabel = startLabel,
                            endLabel = endLabel,
                            onClick = { viewModel.SwitchToFinishedWorkout(workout) },
                            isLandscape = isLandscape
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialog (Just uses the phones date picker)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// For each data entry
@Composable
private fun WorkoutHistoryCard(
    workoutId: Int,
    startLabel: String,
    endLabel: String,
    onClick: () -> Unit,
    isLandscape: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.13f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Workout #$workoutId",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$startLabel  →  $endLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Text(
                    text = "View →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Workout #$workoutId",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Started:  $startLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = "Ended:    $endLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}