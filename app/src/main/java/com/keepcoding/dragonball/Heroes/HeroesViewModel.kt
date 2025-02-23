/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.keepcoding.dragonball.Heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.Repository.DragonBallRepository
import com.keepcoding.dragonball.Model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeroesViewModel: ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    sealed class State {
        data object Loading : State()
        data class Success(val heroes: List<Character>) : State()
        data class Error(val message: String, val errorCode: Int) : State()
    }

    fun downloadCharacters() {
        viewModelScope.launch {
            _uiState.value = State.Loading
            val response = DragonBallRepository.getCharacters()
            if (response.isSuccessful) {
                _uiState.value = State.Success(response.body)
            }
            else {
                _uiState.value = State.Error(response.message, response.code)
            }
        }
    }
}