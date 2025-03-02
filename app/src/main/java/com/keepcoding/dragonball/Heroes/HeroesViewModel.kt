package com.keepcoding.dragonball.Heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Repository.CharactersRepository
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeroesViewModel(
    private val userRepository: UserRepository,
    private val charactersRepository: CharactersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> get() = _uiState.asStateFlow()

    sealed class State {
        data object Loading : State()
        data class Success(val heroes: List<Characters>) : State()
        data class Error(val errorResId: Int) : State()
        data class CharacterSelected(val characters: Characters, val heroes: List<Characters>) : State()
    }

    fun downloadCharacters() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading
            userRepository.loadTokenIfNeeded()
            val token = userRepository.getToken()

            if (token.isBlank()) {
                _uiState.value = State.Error(R.string.error_unauthorized)
                return@launch
            }

            when (val response = charactersRepository.fetchCharacters(token)) {
                is CharactersRepository.CharactersResponse.Success -> {
                    _uiState.value = State.Success(response.characters)
                }
                is CharactersRepository.CharactersResponse.Error -> {
                    _uiState.value = State.Error(response.errorResId)
                }
            }
        }
    }

    fun healHero(hero: Characters) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = charactersRepository.healCharacter(hero.id, healAmount = 20)
            if (response is CharactersRepository.CharactersResponse.Success) {
                val updatedCharacter = response.characters.find { it.id == hero.id } ?: hero
                _uiState.value = State.CharacterSelected(updatedCharacter, response.characters)
            }
        }
    }

    fun damageHero(hero: Characters) {
        viewModelScope.launch(Dispatchers.IO) {
            val randomDamage = (10..60).random()
            val response = charactersRepository.updateCharacterLife(hero.id, damage = randomDamage)
            if (response is CharactersRepository.CharactersResponse.Success) {
                val updatedCharacter = response.characters.find { it.id == hero.id } ?: hero.copy(currentLife = hero.currentLife - randomDamage)
                _uiState.value = State.CharacterSelected(updatedCharacter, response.characters)
            }
        }
    }

    fun healAllHeroes() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = charactersRepository.healAllCharacters()
            if (response is CharactersRepository.CharactersResponse.Success) {
                _uiState.value = State.Success(response.characters)
            }
        }
    }

    fun selectedCharacter(character: Characters) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = charactersRepository.incrementTimesSelected(character.id)
            if (response is CharactersRepository.CharactersResponse.Success) {
                val updatedCharacter = response.characters.find { it.id == character.id } ?: character
                _uiState.value = State.CharacterSelected(updatedCharacter, response.characters)
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.clearToken()
        }
    }
}