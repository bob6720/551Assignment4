package com.example.assignment4

import android.content.res.Resources
import android.util.Log
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Transaction
import com.example.assignment4.Entities.AccelSample
import com.example.assignment4.Entities.AccelSampleTuple
import com.example.assignment4.Entities.ECG_Sample
import com.example.assignment4.Entities.ECG_SampleTuple
import com.example.assignment4.Entities.HR_Sample
import com.example.assignment4.Entities.HR_SampleTuple
import com.example.assignment4.Entities.Workout
import com.example.assignment4.ExerciseDataCalculator.calculateWorkoutStats

@Database(entities = [HR_Sample::class, AccelSample::class, ECG_Sample::class, Workout::class], version = 9)
abstract class ExerciseDataStore : RoomDatabase()
{
    abstract fun exerciseDao(): ExerciseDao
}

@Dao
interface ExerciseDao
{
    @Query("SELECT time_stamp, value, percent_hrr FROM HR_Sample where workout_id = :workout_id")
    fun getHRSamples(workout_id: Int): List<HR_SampleTuple>

    @Query("INSERT INTO HR_Sample (workout_id, time_stamp, value, percent_hrr) VALUES (:workout_id, :time_stamp, :value, :percent_hrr)")
    fun insertHRSample(workout_id: Int, time_stamp: Long, value: Float, percent_hrr: Float)

    @Query("SELECT time_stamp, value, percent_hrr FROM HR_Sample where workout_id = :workout_id AND time_stamp BETWEEN :time_stamp_start AND :time_stamp_end")
    fun getHRSamplesBetween(workout_id: Int, time_stamp_start: Long, time_stamp_end: Long): List<HR_SampleTuple>

    @Query("SELECT time_stamp, value_x, value_y, value_z, enmo FROM AccelSample where workout_id = :workout_id")
    fun getAccelSamples(workout_id: Int): List<AccelSampleTuple>

    @Query("INSERT INTO AccelSample (workout_id, time_stamp, value_x, value_y, value_z, enmo) VALUES (:workout_id, :time_stamp, :value_x, :value_y, :value_z, :enmo)")
    fun insertAccelSample(workout_id: Int, time_stamp: Long, value_x: Float, value_y: Float, value_z: Float, enmo: Float)

    @Query("SELECT time_stamp, value_x, value_y, value_z, enmo FROM AccelSample where workout_id = :workout_id AND time_stamp BETWEEN :time_stamp_start AND :time_stamp_end")
    fun getAccelSamplesBetween(workout_id: Int, time_stamp_start: Long, time_stamp_end: Long): List<AccelSampleTuple>

    @Query("INSERT INTO ECG_Sample (workout_id, time_stamp, value_mv) VALUES (:workout_id, :time_stamp, :value_mv)")
    fun insertECGSample(workout_id: Int, time_stamp: Long, value_mv: Float)

    @Query("SELECT time_stamp, value_mv FROM ECG_Sample where workout_id = :workout_id")
    fun getECGSamples(workout_id: Int): List<ECG_SampleTuple>

    @Query("INSERT INTO Workout (start_time_stamp, end_time_stamp, hr_average, hr_max, hr_min, hr_std_dev, activity_level) VALUES (:time_stamp, :time_stamp, 0, 0, 0, 0, 0)")
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
    fun insertWorkoutStopTime(workout_id: Int, time_stamp: Long)

    @Query("UPDATE Workout SET hr_average = :average_hr, hr_max = :max_hr, hr_min = :min_hr, hr_std_dev = :hr_std_dev, activity_level = :activity_level WHERE id = :workout_id")
    fun insertWorkoutStats(workout_id: Int, average_hr: Float, max_hr: Float, min_hr: Float, hr_std_dev: Float, activity_level: Int)

    @Transaction
    fun stopWorkout(resources: Resources, workout: Workout, time_stamp: Long)
    {
        insertWorkoutStopTime(workout.id, time_stamp)
        val hr_samples = getHRSamples(workout.id)
        val accel_samples = getAccelSamples(workout.id)

        calculateWorkoutStats(resources, workout, hr_samples, accel_samples)

        insertWorkoutStats(workout.id, workout.hrAverage, workout.hrMax, workout.hrMin, workout.hrStdDev, workout.activityLevel)
    }

    @Query("SELECT * FROM Workout WHERE start_time_stamp < end_time_stamp")
    fun getCompletedWorkouts(): List<Workout>

}

enum class ActivityIntensityClassification { NONE, LOW, MEDIUM, HIGH }

object ExerciseDataCalculator
{
    fun calculateWorkoutStats(resources: Resources, workout: Workout, samples: List<HR_SampleTuple>, accel_samples: List<AccelSampleTuple>)
    {
        var average = 0.0;
        var max = 0.0;
        var min = Double.MAX_VALUE;

        for (sample in samples)
        {
            average += sample.value / samples.size
            max = Math.max(max, sample.value.toDouble())
            min = Math.min(min, sample.value.toDouble())
        }

        workout.hrStdDev = calculateStandardDevHR(samples, average.toFloat())

        workout.hrAverage = average.toFloat()
        workout.hrMax = max.toFloat()
        workout.hrMin = min.toFloat()
        workout.activityLevel = classifyActivitySliceIntensity(resources, samples, accel_samples).ordinal
    }

    fun calculateENMO(x: Float, y: Float, z: Float): Float
    {
        return Math.max(Math.sqrt(Math.pow(x.toDouble(), 2.0) + Math.pow(y.toDouble(), 2.0) + Math.pow(z.toDouble(), 2.0)).toFloat() - 1000.0f, 0.0f)
    }

    fun calculatePercentHRR(sample_hr: Float, resting_hr: Float, max_hr: Float): Float
    {
        return Math.max((sample_hr - resting_hr) / (max_hr - resting_hr), 0.0f)
    }

    fun calculateAverageHR(samples: List<HR_SampleTuple>): Float
    {
        var average = 0.0

        for(sample in samples)
        {
            average += sample.value / samples.size
        }

        return average.toFloat();
    }

    fun calculateAveragePercentHRR(samples: List<HR_SampleTuple>): Float
    {
        var average = 0.0

        for(sample in samples)
        {
            average += sample.percent_hrr / samples.size
        }

        return average.toFloat();
    }

    fun calculateStandardDevHR(samples: List<HR_SampleTuple>, hr_avg: Float): Float
    {
        var std_dev = 0.0

        for(sample in samples)
        {
            val internal_std_dev = sample.value - hr_avg
            std_dev += (internal_std_dev * internal_std_dev) / samples.size
        }

        return Math.sqrt(std_dev).toFloat()
    }

    fun calculateAverageENMO(samples: List<AccelSampleTuple>): Float
    {
        var average = 0.0

        for(sample in samples)
        {
            average += sample.enmo / samples.size
        }

        return average.toFloat();
    }

    fun classifyActivitySliceIntensity(resources: Resources, hr_samples: List<HR_SampleTuple>, accel_samples: List<AccelSampleTuple>): ActivityIntensityClassification
    {
        if(hr_samples.size == 0 || accel_samples.size == 0)
        {
            return ActivityIntensityClassification.NONE
        }

        val avg_percent_hrr = calculateAveragePercentHRR(hr_samples) * 100.0f
        val avg_enmo = calculateAverageENMO(accel_samples)

        //Log.d("A4", String.format("HR: %f, ENMO: %f", avg_hr, avg_enmo))

        if(avg_percent_hrr > resources.getInteger(R.integer.HR_HIGH_INT_HRR) && avg_enmo > resources.getInteger(R.integer.ENMO_HIGH_INT_GRAV))
        {
            return ActivityIntensityClassification.HIGH
        }
        else if(avg_percent_hrr > resources.getInteger(R.integer.HR_MED_INT_HRR) && avg_enmo > resources.getInteger(R.integer.ENMO_MED_INT_GRAV))
        {
            return ActivityIntensityClassification.MEDIUM
        }

        return ActivityIntensityClassification.LOW
    }

}