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
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
    onStartListening: () -> Unit = {},
    connectionStatus: String = "",
    device: String = ""
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay so text is readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ECG / Smartwatch Receiver",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Waiting for data from watch…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Button(onClick = {}) {
                    if (connectionStatus == "connected") {
                        Text(text = "Connected to $device")
                    } else {
                        Text(text = connectionStatus)
                    }
                }
                Button(onClick = onStartListening)
                {
                    Text("Start Listening")
                }

                val isWorkoutActive = viewModel.isWorkoutActive.collectAsState()

                if(!isWorkoutActive.value){
                    Button(onClick = { viewModel.StartWorkout() })
                    {
                        Text("Start Workout")
                    }
                } else {
                    Button(onClick = { viewModel.StopWorkout() })
                    {
                        Text("Stop Workout")
                    }
                }

                Button(onClick = { viewModel.SwitchToActive() })
                {
                    Text("Current Workout")
                }

                Button(onClick = { viewModel.SwitchToHistory() })
                {
                    Text("History")
                }
            }
        }
    }
}