package com.example.assignment4

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Query
import androidx.room3.RoomDatabase
import com.example.assignment4.Entities.HR_Sample

@Database(entities = [HR_Sample::class], version = 1)
abstract class ExerciseDataStore : RoomDatabase()
{
    abstract fun exerciseDao(): ExerciseDao
}

@Dao
interface ExerciseDao
{
    @Query("SELECT (time_stamp, value) FROM HR_Sample where workout_id = :workout_id")
    fun getHRSamples(workout_id: Int): List<HR_Sample>

    @Query("INSERT INTO HR_Sample (workout_id, time_stamp, value) VALUES (:workout_id, :time_stamp, :value)")
    fun insertHRSample(workout_id: Int, time_stamp: Long, value: Float)


}