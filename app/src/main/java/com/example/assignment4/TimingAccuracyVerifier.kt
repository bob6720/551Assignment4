package com.example.assignment4

import android.util.Log
import com.example.assignment4.EcgSample
import kotlin.math.abs

object TimingAccuracyVerifier {

    private const val TAG = "TimingAccuracy"
    private const val MAX_ALLOWED_LATENCY_MS = 500L
    private const val MAX_CLOCK_SKEW_MS = 2000L

    fun verify(sample: EcgSample, receiveTimeMs: Long = System.currentTimeMillis()): Boolean {
        val latency = receiveTimeMs - sample.timestampMs

        if (latency < 0) {
            Log.w(TAG, "Negative Latency ($latency ms) - possible clock skew")
            return abs(latency) <= MAX_CLOCK_SKEW_MS

        }

        if (latency > MAX_ALLOWED_LATENCY_MS) {
            Log.w(TAG, "High latency: $latency ms (sample ts=${sample.timestampMs})")
            return false
        }

        Log.d(TAG, "Ok - latency=${latency} ms, value=${sample.timestampMs}")
        return true
    }





}