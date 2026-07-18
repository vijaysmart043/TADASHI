package com.vijay.tadashi.presentation.screens.home

import com.vijay.tadashi.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<Unit, HomeEffect, HomeAction>(Unit) {

    override fun handleActions() {}
}

sealed interface HomeEffect

sealed interface HomeAction