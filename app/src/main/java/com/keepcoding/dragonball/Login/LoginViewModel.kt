package com.keepcoding.dragonball

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.Heroes.Data.PreferencesMagager
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Idle)
    val uiState: StateFlow<State> get() = _uiState

    sealed class State {
        object Idle : State()
        object Loading : State()
        object Success : State()
        data class Error(val message: String, val errorCode: Int) : State()
    }

    fun login(user: String, password: String, preferences: SharedPreferences) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading

            val localDataSource = PreferencesMagager(preferences)
            val userRepository = UserRepository(localDataSource)

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

    fun checkIfLoggedIn(preferences: SharedPreferences) {
        viewModelScope.launch(Dispatchers.IO) {
            val localDataSource = PreferencesMagager(preferences)
            val userRepository = UserRepository(localDataSource)

            userRepository.loadTokenIfNeeded()
            if (userRepository.getToken().isNotEmpty()) {
                _uiState.value = State.Success
            }
        }
    }

    fun saveUserAndPass(preferences: SharedPreferences, user: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val localDataSource = PreferencesMagager(preferences)
            localDataSource.saveUserAndPass(user, password)
        }
    }

    fun getStoredCredentials(preferences: SharedPreferences): Pair<String, String>? {
        val localDataSource = PreferencesMagager(preferences)
        return localDataSource.getUserAndPass()
    }

    fun clearUserAndPass(preferences: SharedPreferences) {
        viewModelScope.launch(Dispatchers.IO) {
            val localDataSource = PreferencesMagager(preferences)
            localDataSource.clearUserAndPass()
        }
    }
}