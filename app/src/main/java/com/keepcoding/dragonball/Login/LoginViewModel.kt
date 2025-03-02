package com.keepcoding.dragonball.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Idle)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    sealed class State {
        data object Idle : State()
        data object Loading : State()
        data object Success : State()
        data class Error(val message: String, val errorCode: Int) : State()
    }

    fun login(user: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading

            when (val loginResponse = userRepository.login(user, password)) {
                is UserRepository.LoginResponse.Success -> {
                    _uiState.value = State.Success
                }
                is UserRepository.LoginResponse.Error -> {
                    _uiState.value = State.Error("Error de login: ${loginResponse.message}", 401)
                }
            }
        }
    }

    fun checkIfLoggedIn() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.loadTokenIfNeeded()
            if (userRepository.getToken().isNotEmpty()) {
                _uiState.value = State.Success
            }
        }
    }

    fun saveUserAndPass(user: String, password: String) {
        viewModelScope.launch {
            preferencesManager.saveUserAndPass(user, password)
        }
    }

    fun getStoredCredentials(): Pair<String, String>? {
        return preferencesManager.getUserAndPass()
    }

    fun clearUserAndPass() {
        viewModelScope.launch {
            preferencesManager.clearUserAndPass()
        }
    }
}