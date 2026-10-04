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
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
    connectionStatus: String = "",
    device: String = ""
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWorkoutActive = viewModel.isWorkoutActive.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.placeholder2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent bg so text is readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )


        // If on side, move to grid format
        // If upright, move to column format
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            val isCompact = maxWidth < 600.dp
            // Side layout
            if (isLandscape && !isCompact) {

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left – title + status
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        HeaderSection()
                        Spacer(modifier = Modifier.height(24.dp))
                        StatusCard(connectionStatus, device)
                    }

                    // Right – buttons
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ActionButtons(
                            isWorkoutActive = isWorkoutActive.value,
                            onStart = { viewModel.StartWorkout() },
                            onStop = { viewModel.StopWorkout() },
                            onCurrent = { viewModel.SwitchToActive() },
                            onHistory = { viewModel.SwitchToHistory() },
                            onSettings = { viewModel.SwitchToSettings() }
                        )
                    }
                }
            } else {
                // Upright layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HeaderSection()
                        Spacer(modifier = Modifier.height(20.dp))
                        StatusCard(connectionStatus, device)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ActionButtons(
                        isWorkoutActive = isWorkoutActive.value,
                        onStart = { viewModel.StartWorkout() },
                        onStop = { viewModel.StopWorkout() },
                        onCurrent = { viewModel.SwitchToActive() },
                        onHistory = { viewModel.SwitchToHistory() },
                        onSettings = { viewModel.SwitchToSettings() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            // Smartwatch?
            text = "ECG / Smartwatch",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Receiver",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Waiting for data from watch…",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

// Removed the button outline as it didn't make sense for connection
@Composable
private fun StatusCard(connectionStatus: String, device: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (connectionStatus == "connected") "Connected" else "Status",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = if (connectionStatus == "connected") device else connectionStatus,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isWorkoutActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCurrent: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main workout button
        Button(
            onClick = { if (isWorkoutActive) onStop() else onStart() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isWorkoutActive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isWorkoutActive) "Stop Workout" else "Start Workout",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Other buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCurrent,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Current")
            }

            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("History")
            }
        }

        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Settings")
        }
    }
}