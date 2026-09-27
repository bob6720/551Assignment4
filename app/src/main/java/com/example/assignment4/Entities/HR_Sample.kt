package com.example.assignment4.Entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Workout::class, parentColumns = ["id"], childColumns = ["workout_id"]) ])
data class HR_Sample(
    @PrimaryKey(autoGenerate = true) val workout_id: Int,
    @ColumnInfo(name = "time_stamp") val timeStamp: Long,
    @ColumnInfo(name = "value") val value: Float,
)

data class HR_SampleTuple(
    @ColumnInfo(name = "time_stamp") val timeStamp: Long,
    @ColumnInfo(name = "value") val value: Float
)