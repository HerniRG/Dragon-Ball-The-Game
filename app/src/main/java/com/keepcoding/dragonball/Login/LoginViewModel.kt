package com.keepcoding.dragonball

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Idle)
    val uiState: StateFlow<State> = _uiState

    private val userRepository = UserRepository()

    sealed class State {
        object Idle : State()
        object Loading : State()
        object Success : State()
        data class Error(val message: String, val errorCode: Int) : State()
    }

    // Iniciar sesión
    fun login(user: String, password: String, preferences: SharedPreferences?) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading
            val loginResponse = userRepository.login(user, password)
            when (loginResponse) {
                is UserRepository.LoginResponse.Success -> {
                    // Guardamos el token en UserRepository
                    userRepository.getToken().let { token ->
                        if (token.isNotEmpty()) {
                            // Podemos guardar el token en SharedPreferences si queremos
                            preferences?.edit()?.apply {
                                putString("userToken", token)
                                apply()
                            }
                        }
                    }
                    _uiState.value = State.Success
                }
                is UserRepository.LoginResponse.Error -> {
                    _uiState.value = State.Error(
                        "Error con la contraseña o la conexión a internet",
                        401
                    )
                }
            }
        }
    }

    // Guardar usuario y contraseña en SharedPreferences
    fun saveUserAndPass(preferences: SharedPreferences?, user: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000L)
            preferences?.edit()?.apply {
                putString("User", user)
                putString("Password", password)
                apply()
            }
        }
    }

    // Comprobar si hay un usuario logueado
    fun checkIfLoggedIn(preferences: SharedPreferences?) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = preferences?.getString("userToken", "") ?: ""
            if (token.isNotEmpty()) {
                // Ya hay un token, asumimos que está logueado
                _uiState.value = State.Success
            }
        }
    }

    // Recuperar usuario y contraseña de SharedPreferences
    fun getStoredCredentials(preferences: SharedPreferences?): Pair<String, String>? {
        val usuario = preferences?.getString("User", "") ?: ""
        val password = preferences?.getString("Password", "") ?: ""

        return if (usuario.isNotEmpty() && password.isNotEmpty()) {
            usuario to password
        } else {
            null
        }
    }
}