package com.example.assignment4.Viewmodels

import androidx.lifecycle.ViewModel
import com.example.assignment4.ExerciseDao
import com.example.assignment4.ExerciseDataCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActiveScreenViewModel(private val viewModel: ApplicationViewModel, private val exerciseDao: ExerciseDao) : ScreenViewModel()
{
    private val FAST_REFRESH_TIME_MS = 1000L
    private val SLOW_REFRESH_TIME_MS = 60L*1000L

    // The average HR for the sample period (1 min)
    private val minute_avg_hr = MutableStateFlow<Float>(0.0f);
    val minuteAvgHR = minute_avg_hr.asStateFlow()

    private var threads_running = false;

    private var fast_refresh_thread : Thread? = null;
    private var slow_refresh_thread : Thread? = null;


    // Kick off the threaded jobs to re-calculate stats every so often
    private fun StartStatRecalculationJobs()
    {
        if(threads_running)
        {
            return
        }

        threads_running = true

        fast_refresh_thread = Thread {
            try
            {
                while(true)
                {
                    Thread.sleep(FAST_REFRESH_TIME_MS)
                }
            }
            catch(e: InterruptedException)
            {
            }
        }

        fast_refresh_thread?.start();

        slow_refresh_thread = Thread {
            try
            {
                while(true)
                {
                    Thread.sleep(SLOW_REFRESH_TIME_MS)

                    // Calculate minute HR stats
                    val curr_time = System.currentTimeMillis();
                    val start_time = curr_time - SLOW_REFRESH_TIME_MS;

                    val hr_samples = exerciseDao.getHRSamplesBetween(viewModel.GetActiveWorkout()?.id
                        ?: 0, start_time, curr_time)

                    minute_avg_hr.value = ExerciseDataCalculator.calculateAverageHR(hr_samples);
                }
            }
            catch(e: InterruptedException)
            {
            }
        }

        slow_refresh_thread?.start();
    }

    private fun KillStatRecalculationJobs()
    {
        if(!threads_running)
        {
            return;
        }

        threads_running = false

        fast_refresh_thread?.interrupt()
        slow_refresh_thread?.interrupt()

        threads_running = false
    }

    override fun ScreenEnter()
    {
        StartStatRecalculationJobs()
    }

    override fun ScreenExit()
    {
        KillStatRecalculationJobs()
    }

}