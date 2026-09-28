package com.example.assignment4.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment4.Entities.Workout
import com.example.assignment4.ExerciseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ActiveScreen { HOME_SCREEN, HISTORY_SCREEN }

class ApplicationViewModel(private val exerciseDao: ExerciseDao) : ViewModel()
{
    private var active_workout : Workout? = null;
    private var is_workout_active : MutableStateFlow<Boolean> = MutableStateFlow(false);
    val isWorkoutActive = is_workout_active.asStateFlow();
    private var active_screen = MutableStateFlow(ActiveScreen.HOME_SCREEN)
    val activeScreen = active_screen.asStateFlow()

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
                while(debug_thread_running)
                {
                    Thread.sleep(100)

                    exerciseDao.insertHRSample(active_workout?.id ?: 0, System.currentTimeMillis(), (60..100).random().toFloat())
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
        }

        // Todo(Leo): Once this is done and stats have been calculated refresh historic_workouts.
    }

    fun SwitchToHome()
    {
        active_screen.value = ActiveScreen.HOME_SCREEN
    }

    fun SwitchToHistory()
    {
        active_screen.value = ActiveScreen.HISTORY_SCREEN
    }

    private var historic_workouts = MutableStateFlow<List<Workout>>(List(0,  { Workout(0, 0L, 0L, 0.0f, 0.0f, 0.0f) }));
    val historicWorkouts = historic_workouts.asStateFlow()

    init
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            historic_workouts.value = exerciseDao.getCompletedWorkouts()
        }
    }

}