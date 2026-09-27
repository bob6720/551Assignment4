package com.example.assignment4.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment4.Entities.Workout
import com.example.assignment4.ExerciseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApplicationViewModel(private val exerciseDao: ExerciseDao) : ViewModel()
{
    private var active_workout : Workout? = null;
    private var is_workout_active : MutableStateFlow<Boolean> = MutableStateFlow(false);
    var isWorkoutActive = is_workout_active.asStateFlow();

    fun StartWorkout()
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            active_workout = exerciseDao.startWorkout(System.currentTimeMillis())
            is_workout_active.value = true
        }
    }

    fun StopWorkout()
    {
        val workout = active_workout ?: return

        viewModelScope.launch(Dispatchers.IO)
        {
            exerciseDao.stopWorkout(workout.id, System.currentTimeMillis())
        }

        active_workout = null
        is_workout_active.value = false
    }

}