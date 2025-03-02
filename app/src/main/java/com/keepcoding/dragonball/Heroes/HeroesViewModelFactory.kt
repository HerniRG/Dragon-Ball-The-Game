package com.keepcoding.dragonball.Heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.keepcoding.dragonball.Repository.CharactersRepository
import com.keepcoding.dragonball.Repository.UserRepository

class HeroesViewModelFactory(
    private val userRepository: UserRepository,
    private val charactersRepository: CharactersRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeroesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeroesViewModel(userRepository, charactersRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}