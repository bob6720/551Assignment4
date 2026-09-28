package com.example.assignment4.Entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Workout::class, parentColumns = ["id"], childColumns = ["workout_id"]) ])
data class AccelSample(
    @PrimaryKey() val time_stamp: Long,
    @ColumnInfo(name = "workout_id") val workout_id: Int,
    // Milligravity Units
    @ColumnInfo(name = "value_x") val value_x: Float,
    @ColumnInfo(name = "value_y") val value_y: Float,
    @ColumnInfo(name = "value_z") val value_z: Float,
    @ColumnInfo(name = "enmo") val enmo: Float,
)

data class AccelSampleTuple(
    @ColumnInfo(name = "time_stamp") val timeStamp: Long,
    // Milligravity Units
    @ColumnInfo(name = "value_x") val value_x: Float,
    @ColumnInfo(name = "value_y") val value_y: Float,
    @ColumnInfo(name = "value_z") val value_z: Float,
    @ColumnInfo(name = "enmo") val enmo: Float,
)