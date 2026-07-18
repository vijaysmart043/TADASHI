package com.vijay.tadashi.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.tadashi.core.logger.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, E, A>(initialState: S) : ViewModel() {

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect: Channel<E> = Channel()
    val effect = _effect.receiveAsFlow()

    private val _action: MutableSharedFlow<A> = MutableSharedFlow()
    val action: SharedFlow<A> = _action.asSharedFlow()

    init {
        handleActions()
    }

    protected fun setState(newState: S) {
        _state.value = newState
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    protected fun sendAction(action: A) {
        viewModelScope.launch {
            _action.emit(action)
        }
    }

    protected abstract fun handleActions()

    override fun onCleared() {
        Logger.d("ViewModel cleared: ${this::class.simpleName}")
        super.onCleared()
    }
}