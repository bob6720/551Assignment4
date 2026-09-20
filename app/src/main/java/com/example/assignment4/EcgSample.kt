package com.example.assignment4

// Use later?
import java.sql.Timestamp

data class EcgSample(
    val timestampMs: Long,
    val value: Float,
    val sequenceId: Long = 0
)