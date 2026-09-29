package com.example.assignment4.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment4.ActivityIntensityClassification
import com.example.assignment4.Entities.Workout
import com.example.assignment4.ExerciseDao
import com.example.assignment4.ExerciseDataCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ActiveScreen { HOME_SCREEN, HISTORY_SCREEN, ACTIVE_SCREEN, FINISHED_WORKOUT_SCREEN }

class ApplicationViewModel(private val exerciseDao: ExerciseDao) : ViewModel()
{
    private var active_workout : Workout? = null;
    private var is_workout_active : MutableStateFlow<Boolean> = MutableStateFlow(false);
    val isWorkoutActive = is_workout_active.asStateFlow();
    private var active_screen = MutableStateFlow(ActiveScreen.HOME_SCREEN)
    val activeScreen = active_screen.asStateFlow()

    var activeScreenViewModel : ScreenViewModel? = null

    var debug_thread_running = false;
    var debug_data_gen_thread : Thread? = null;

    fun StartWorkout()
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            active_workout = exerciseDao.startWorkout(System.currentTimeMillis())
            is_workout_active.value = true


            debug_thread_running = true;
            debug_data_gen_thread = Thread {
                var ecg_tick = 0
                var hr_tick = 0
                var stage = ActivityIntensityClassification.LOW
                var stage_tick = 0

                while(debug_thread_running)
                {
                    Thread.sleep(10)

                    if(hr_tick == 0)
                    {
                        var base_hr = 60;
                        var accel_mult = 0.0f;

                        if(stage == ActivityIntensityClassification.LOW)
                        {
                            base_hr = 60
                            accel_mult = 1.0f
                        }
                        else if(stage == ActivityIntensityClassification.MEDIUM)
                        {
                            base_hr = 70
                            accel_mult = 1.5f
                        }
                        else if(stage == ActivityIntensityClassification.HIGH)
                        {
                            base_hr = 90
                            accel_mult = 4.0f
                        }

                        InsertHRSample(
                            active_workout?.id ?: 0,
                            System.currentTimeMillis(),
                            (base_hr..(base_hr + 30)).random().toFloat()
                        )

                        InsertAccelSample(
                            active_workout?.id ?: 0,
                            System.currentTimeMillis(),
                            (0..100).random().toFloat() * accel_mult,
                            (0..100).random().toFloat() * accel_mult,
                            1000 + ((0..100).random().toFloat() * accel_mult)
                        )

                        stage_tick = (stage_tick + 1) % 100

                        if(stage_tick == 0)
                        {
                            stage = ActivityIntensityClassification.values().random()
                        }
                    }

                    val is_wave_stage = ecg_tick > 190

                    var ecg_value_mv = 0.1f;
                    if(is_wave_stage)
                    {
                        val tick_progress = ((ecg_tick - 190).toDouble() * Math.PI.toDouble()) / 10.0;
                        ecg_value_mv += (Math.sin(tick_progress) * 1.2).toFloat()
                    }

                    InsertECGSample(active_workout?.id ?: 0, System.currentTimeMillis(), ecg_value_mv)
                    ecg_tick = (ecg_tick + 1) % 200
                    hr_tick = (hr_tick + 1) % 100
                }
            }
            debug_data_gen_thread?.start()

        }
    }

    fun StopWorkout()
    {
        val workout = active_workout ?: return

        // Grab stop time early so we dont get an inaccurate late one from waiting for the thread scope change
        val stop_time = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO)
        {
            debug_thread_running = false;
            debug_data_gen_thread?.join()

            exerciseDao.stopWorkout(workout, stop_time)
            is_workout_active.value = false
            active_workout = null

            historic_workouts.value = exerciseDao.getCompletedWorkouts()
        }
    }

    fun SwitchToHome()
    {
        activeScreenViewModel?.ScreenExit()
        active_screen.value = ActiveScreen.HOME_SCREEN
        activeScreenViewModel = null
    }

    fun SwitchToHistory()
    {
        activeScreenViewModel?.ScreenExit()
        active_screen.value = ActiveScreen.HISTORY_SCREEN
        activeScreenViewModel = null
    }

    fun SwitchToActive()
    {
        activeScreenViewModel?.ScreenExit()
        active_screen.value = ActiveScreen.ACTIVE_SCREEN
        activeScreenViewModel = ActiveScreenViewModel(this, exerciseDao);
        activeScreenViewModel?.ScreenEnter()
    }

    fun SwitchToFinishedWorkout(workout: Workout)
    {
        activeScreenViewModel?.ScreenExit()
        if(is_workout_active.value)
        {
            StopWorkout()
        }

        active_workout = workout
        active_screen.value = ActiveScreen.FINISHED_WORKOUT_SCREEN
        activeScreenViewModel = null
    }

    fun GetActiveWorkout() : Workout?
    {
        return active_workout;
    }

    private var historic_workouts = MutableStateFlow<List<Workout>>(List(0,  { Workout(0, 0L, 0L, 0.0f, 0.0f, 0.0f, 0.0f) }));
    val historicWorkouts = historic_workouts.asStateFlow()

    init
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            historic_workouts.value = exerciseDao.getCompletedWorkouts()
        }
    }

    val ROLLING_HR_WINDOW_SIZE = 10;
    private var rolling_hr = MutableStateFlow(List(ROLLING_HR_WINDOW_SIZE, { 0.0f }));
    val rollingHR = rolling_hr.asStateFlow()
    private fun InsertHRSample(workout_id: Int, time_stamp: Long, value: Float)
    {
        exerciseDao.insertHRSample(workout_id, time_stamp, value)
        rolling_hr.value = rolling_hr.value.drop(1) + value
    }
    val ROLLING_ENMO_WINDOW_SIZE = 10;
    private var rolling_enmo = MutableStateFlow(List(ROLLING_ENMO_WINDOW_SIZE, { 0.0f }));
    val rollingENMO = rolling_enmo.asStateFlow()
    private fun InsertAccelSample(workout_id: Int, time_stamp: Long, value_x: Float, value_y: Float, value_z: Float)
    {
        val enmo = ExerciseDataCalculator.calculateENMO(value_x, value_y, value_z)
        exerciseDao.insertAccelSample(workout_id, time_stamp, value_x, value_y, value_z, enmo)
        rolling_enmo.value = rolling_enmo.value.drop(1) + enmo
    }

    val ROLLING_ECG_WINDOW_SIZE = 100;
    private var rolling_ecg = MutableStateFlow(List(ROLLING_ECG_WINDOW_SIZE, { 0.0f }));
    val rollingECG = rolling_ecg.asStateFlow()
    private fun InsertECGSample(workout_id: Int, time_stamp: Long, value_mv: Float)
    {
        exerciseDao.insertECGSample(workout_id, time_stamp, value_mv)
        rolling_ecg.value = rolling_ecg.value.drop(1) + value_mv
    }

}