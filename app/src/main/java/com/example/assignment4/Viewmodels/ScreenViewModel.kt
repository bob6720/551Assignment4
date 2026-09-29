package com.example.assignment4.Viewmodels

import androidx.lifecycle.ViewModel

abstract class ScreenViewModel : ViewModel()
{
    abstract fun ScreenEnter()
    abstract fun ScreenExit()
}