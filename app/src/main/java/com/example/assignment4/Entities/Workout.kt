package com.example.assignment4.Entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "start_time_stamp") val startTimeStamp: Long,
    @ColumnInfo(name = "end_time_stamp") val endTimeStamp: Long,

    // Calculated once the workout ends
    @ColumnInfo(name = "hr_average") val hrAverage: Float,
    @ColumnInfo(name = "hr_max") val hrMax: Float,
    @ColumnInfo(name = "hr_min") val hrMin: Float,
)