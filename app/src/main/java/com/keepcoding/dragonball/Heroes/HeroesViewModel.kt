package com.keepcoding.dragonball.Heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepcoding.dragonball.Heroes.Data.PreferencesMagager
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Repository.CharactersRepository
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeroesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> get() = _uiState.asStateFlow()

    sealed class State {
        object Loading : State()
        data class Success(val heroes: List<Characters>) : State()
        data class Error(val message: String, val errorCode: Int) : State()
        data class CharacterSelected(val characters: Characters) : State()
    }

    private var charactersList = listOf<Characters>()

    fun downloadCharacters(preferencesManager: PreferencesMagager) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading

            val userRepository = UserRepository(preferencesManager)
            val charactersRepository = CharactersRepository(preferencesManager)

            userRepository.loadTokenIfNeeded()
            val token = userRepository.getToken()
            if (token.isBlank()) {
                _uiState.value = State.Error("El token está vacío", 401)
                return@launch
            }

            when (val response = charactersRepository.fetchCharacters(token)) {
                is CharactersRepository.CharactersResponse.Success -> {
                    charactersList = response.characters
                    _uiState.value = State.Success(charactersList)
                }
                is CharactersRepository.CharactersResponse.Error -> {
                    _uiState.value = State.Error(response.message, 500)
                }
            }
        }
    }

    fun healAllHeroes() {
        viewModelScope.launch(Dispatchers.IO) {
            if (charactersList.isNotEmpty()) {
                // Se restaura la vida de todos los héroes
                charactersList = charactersList.map {
                    it.copy(currentLife = it.totalLife)
                }
                _uiState.value = State.Success(charactersList)
            }
        }
    }

    fun logout(preferencesManager: PreferencesMagager) {
        viewModelScope.launch(Dispatchers.IO) {
            val userRepository = UserRepository(preferencesManager)
            userRepository.clearToken()
        }
    }

    fun selectedCharacter(characters: Characters) {
        _uiState.value = State.CharacterSelected(characters)
    }
}