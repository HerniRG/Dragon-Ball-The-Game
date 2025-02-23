/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.keepcoding.dragonball.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.Repository.DragonBallRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Idle)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    sealed class State {
        data object Idle : State()
        data object Loading : State()
        data class Success(val token: String) : State()
        data class Error(val message: String, val errorCode: Int) : State()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading
            val response = DragonBallRepository.login(email, password)
            if (response.isSuccessful) {
                _uiState.value = State.Success(response.body.token)
            } else {
                _uiState.value = State.Error(response.message, response.code)
            }
        }
    }
}