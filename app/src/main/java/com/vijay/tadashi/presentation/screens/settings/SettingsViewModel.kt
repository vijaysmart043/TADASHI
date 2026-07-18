package com.vijay.tadashi.presentation.screens.settings

import com.vijay.tadashi.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : BaseViewModel<Unit, SettingsEffect, SettingsAction>(Unit) {

    override fun handleActions() {}
}

sealed interface SettingsEffect

sealed interface SettingsAction