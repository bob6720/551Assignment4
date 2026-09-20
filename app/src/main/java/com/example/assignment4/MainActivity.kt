package com.example.assignment4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.assignment4.ui.theme.Assignment4Theme
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Optional: get the MessageClient early (the Service still receives messages in the background)
        val messageClient = Wearable.getMessageClient(this)

        setContent {
            Assignment4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStartListening = {
                            // For now the WearableListenerService handles everything
                        }
                    )
                }
            }
        }
    }
}