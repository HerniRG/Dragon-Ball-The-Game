package com.keepcoding.dragonball.Heroes

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val charactersRepository = CharactersRepository()
    private val userRepository = UserRepository()

    sealed class State {
        object Loading : State()
        data class Success(val heroes: List<Characters>) : State()
        data class Error(val message: String, val errorCode: Int) : State()
        data class CharacterSelected(val characters: Characters) : State()
    }

    fun selectedCharacter(characters: Characters) {
        _uiState.value = State.CharacterSelected(characters)
    }

    /**
     * Descargar personajes usando el token (desde UserRepository) y
     * además cachear la lista en SharedPreferences (pasada desde HeroesActivity).
     */
    fun downloadCharacters(preferences: SharedPreferences) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading

            // Recuperamos el token del companion object
            val token = userRepository.getToken()
            if (token.isBlank()) {
                _uiState.value = State.Error("El token está vacío", 401)
                return@launch
            }

            val response = charactersRepository.fetchCharacters(
                token = token,
                sharedPreferences = preferences  // <-- Lo pasamos para caché
            )

            when (response) {
                is CharactersRepository.CharactersResponse.Success ->
                    _uiState.value = State.Success(response.characters)

                is CharactersRepository.CharactersResponse.Error ->
                    _uiState.value = State.Error(response.message, 500)
            }
        }
    }
}