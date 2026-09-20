package com.example.assignment4

import android.util.Log
import com.example.assignment4.EcgSample
import com.example.assignment4.TimingAccuracyVerifier
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

class MessageReceiverService : WearableListenerService() {

    companion object {
        const val PATH_ECG = "/ecg_data"
        private const val TAG = "MessageReceiver"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != PATH_ECG) return

        try {
            val json = String(messageEvent.data)
            val obj = JSONObject(json)

            val sample = EcgSample(
                timestampMs = obj.getLong("ts"),
                value = obj.getDouble("value").toFloat(),
                sequenceId = obj.optLong("seq", 0)
            )

            val receiveTime = System.currentTimeMillis()
            val isAccurate = TimingAccuracyVerifier.verify(sample, receiveTime)

            Log.i(TAG, "Received ECG sample: $sample  accurate=$isAccurate")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message", e)
        }
    }
}