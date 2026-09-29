package com.example.assignment4

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.assignment4.Viewmodels.ApplicationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationViewModel,
)
{
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight().background(color = Color.White))
    {
        Button(onClick = { viewModel.SwitchToHome() })
        {
            Text("Back")
        }

        var resting_heart_rate by remember { mutableStateOf(viewModel.GetUserRestingHeartRate().toString()) }
        var max_heart_rate by remember { mutableStateOf(viewModel.GetUserMaxHeartRate().toString()) }

        Text("% HRR Settings", style = MaterialTheme.typography.headlineMedium)

        TextField(
            value = resting_heart_rate,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() })
                {
                    resting_heart_rate = it
                }
            },
            label =  { Text("Resting Heart Rate") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        TextField(
            value = max_heart_rate,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() })
                {
                    max_heart_rate = it
                }
            },
            label =  { Text("Maximum Heart Rate") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Button(onClick = { viewModel.SetUserRestingHeartRate(resting_heart_rate.toInt()); viewModel.SetUserMaxHeartRate(max_heart_rate.toInt()) })
        {
            Text("Save")
        }

    }
}