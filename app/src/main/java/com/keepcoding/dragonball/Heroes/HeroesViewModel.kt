package com.keepcoding.dragonball.Heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Repository.CharactersRepository
import com.keepcoding.dragonball.Repository.ErrorMessages
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HeroesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> get() = _uiState.asStateFlow()

    private lateinit var prefs: PreferencesManager

    sealed class State {
        data object Loading : State()
        data class Success(val heroes: List<Characters>) : State()
        data class Error(val errorResId: Int) : State()
        data class CharacterSelected(val characters: Characters, val uniqueId: Long = System.nanoTime()) : State()
    }

    private var charactersList = listOf<Characters>()

    private fun getUserId(): String = prefs.getUserAndPass()?.first ?: "default"

    private fun persistCharacters() {
        val json = Gson().toJson(charactersList)
        prefs.saveCharactersList(userId = getUserId(), charactersJson = json)
    }

    fun downloadCharacters(preferencesManager: PreferencesManager) {
        prefs = preferencesManager

        val savedJson = prefs.getCharactersList(getUserId())
        if (savedJson.isNotEmpty()) {
            try {
                val savedCharacters = Gson().fromJson(savedJson, Array<Characters>::class.java).toList()
                if (savedCharacters.isNotEmpty()) {
                    charactersList = savedCharacters
                    _uiState.value = State.Success(charactersList)
                    return
                }
            } catch (_: Exception) { }
        }

        if (charactersList.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading

            val userRepository = UserRepository(prefs)
            val charactersRepository = CharactersRepository(prefs)

            userRepository.loadTokenIfNeeded()
            val token = userRepository.getToken()
            if (token.isBlank()) {
                _uiState.value = State.Error(R.string.error_unauthorized)
                return@launch
            }

            when (val response = charactersRepository.fetchCharacters(token)) {
                is CharactersRepository.CharactersResponse.Success -> {
                    charactersList = response.characters
                    persistCharacters()
                    _uiState.value = State.Success(charactersList)
                }
                is CharactersRepository.CharactersResponse.Error -> {
                    _uiState.value = State.Error(ErrorMessages.getErrorMessage(500))
                }
            }
        }
    }

    fun healAllHeroes() {
        viewModelScope.launch(Dispatchers.IO) {
            if (charactersList.isNotEmpty()) {
                charactersList = charactersList.map {
                    it.copy(currentLife = it.totalLife, isDead = false)
                }
                persistCharacters()
                _uiState.value = State.Success(charactersList)
            }
        }
    }

    fun healHero(hero: Characters) {
        viewModelScope.launch(Dispatchers.IO) {
            if (hero.isDead) return@launch
            val healAmount = 20
            charactersList = charactersList.map {
                if (it.id == hero.id) {
                    it.copy(currentLife = (it.currentLife + healAmount).coerceAtMost(it.totalLife))
                } else {
                    it
                }
            }
            persistCharacters()
            val updatedHero = hero.copy(currentLife = (hero.currentLife + healAmount).coerceAtMost(hero.totalLife))
            withContext(Dispatchers.Main) {
                _uiState.value = State.Success(charactersList)
                _uiState.value = State.CharacterSelected(updatedHero)
            }
        }
    }

    fun damageHero(hero: Characters) {
        viewModelScope.launch(Dispatchers.IO) {
            if (hero.isDead) return@launch
            val randomDamage = (10..60).random()
            charactersList = charactersList.map {
                if (it.id == hero.id) {
                    val newLife = (it.currentLife - randomDamage).coerceAtLeast(0)
                    it.copy(currentLife = newLife, isDead = (newLife == 0))
                } else {
                    it
                }
            }
            persistCharacters()
            val updatedHero = charactersList.find { it.id == hero.id } ?: hero
            withContext(Dispatchers.Main) {
                _uiState.value = State.Success(charactersList)
                _uiState.value = State.CharacterSelected(updatedHero)
            }
        }
    }

    fun logout(preferencesManager: PreferencesManager) {
        viewModelScope.launch(Dispatchers.IO) {
            val userRepository = UserRepository(preferencesManager)
            userRepository.clearToken()
        }
    }

    fun selectedCharacter(character: Characters) {
        if (!character.isDead) {
            charactersList = charactersList.map {
                if (it.id == character.id) {
                    it.copy(timesSelected = it.timesSelected + 1)
                } else {
                    it
                }
            }
            persistCharacters()

            val updatedCharacter = charactersList.find { it.id == character.id } ?: character

            _uiState.value = State.CharacterSelected(updatedCharacter)
        }
    }
}