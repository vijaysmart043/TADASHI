package com.vijay.tadashi.presentation.screens.splash

import androidx.lifecycle.viewModelScope
import com.vijay.tadashi.core.constants.Constants
import com.vijay.tadashi.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel<Unit, SplashEffect, SplashAction>(Unit) {

    override fun handleActions() {
        viewModelScope.launch {
            action.collect { action ->
                when (action) {
                    SplashAction.NavigateToHome -> navigateToHome()
                }
            }
        }
    }

    private fun navigateToHome() {
        viewModelScope.launch {
            delay(Constants.SPLASH_DELAY)
            sendEffect(SplashEffect.NavigateToHome)
        }
    }
}

sealed interface SplashEffect {
    data object NavigateToHome : SplashEffect
}

sealed interface SplashAction {
    data object NavigateToHome : SplashAction
}