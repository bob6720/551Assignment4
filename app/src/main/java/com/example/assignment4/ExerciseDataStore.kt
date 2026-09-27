package com.example.assignment4

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Transaction
import com.example.assignment4.Entities.HR_Sample
import com.example.assignment4.Entities.HR_SampleTuple
import com.example.assignment4.Entities.Workout

@Database(entities = [HR_Sample::class, Workout::class], version = 1)
abstract class ExerciseDataStore : RoomDatabase()
{
    abstract fun exerciseDao(): ExerciseDao
}

@Dao
interface ExerciseDao
{
    @Query("SELECT time_stamp, value FROM HR_Sample where workout_id = :workout_id")
    fun getHRSamples(workout_id: Int): List<HR_SampleTuple>

    @Query("INSERT INTO HR_Sample (workout_id, time_stamp, value) VALUES (:workout_id, :time_stamp, :value)")
    fun insertHRSample(workout_id: Int, time_stamp: Long, value: Float)

    @Query("INSERT INTO Workout (start_time_stamp, end_time_stamp, hr_average, hr_max, hr_min) VALUES (:time_stamp, :time_stamp, 0, 0, 0)")
    fun insertWorkout(time_stamp: Long)

    @Query("SELECT * FROM Workout WHERE id = (SELECT last_insert_rowid())")
    fun getNewestWorkout(): Workout

    @Transaction
    fun startWorkout(time_stamp: Long): Workout
    {
        insertWorkout(time_stamp)
        return getNewestWorkout()
    }

    @Query("UPDATE Workout SET end_time_stamp = :time_stamp WHERE id = :workout_id")
    fun stopWorkout(workout_id: Int, time_stamp: Long)

}