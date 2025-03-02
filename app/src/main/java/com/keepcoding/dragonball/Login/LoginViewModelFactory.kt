package com.keepcoding.dragonball.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.Repository.UserRepository

class LoginViewModelFactory(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}