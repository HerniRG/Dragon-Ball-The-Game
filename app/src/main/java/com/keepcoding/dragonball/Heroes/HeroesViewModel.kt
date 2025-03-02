package com.keepcoding.dragonball.Heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Repository.CharactersRepository
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

    // Guardaremos la instancia de PreferencesManager para persistir cambios
    private lateinit var prefs: PreferencesManager

    sealed class State {
        object Loading : State()
        data class Success(val heroes: List<Characters>) : State()
        data class Error(val message: String, val errorCode: Int) : State()
        data class CharacterSelected(val characters: Characters, val uniqueId: Long = System.nanoTime()) : State()
    }

    private var charactersList = listOf<Characters>()

    // Extraemos de forma segura el identificador del usuario (o usamos "default")
    private fun getUserId(): String {
        return prefs.getUserAndPass()?.first ?: "default"
    }

    // Función que centraliza la persistencia de la lista de personajes
    private fun persistCharacters() {
        val json = Gson().toJson(charactersList)
        prefs.saveCharactersList(userId = getUserId(), charactersJson = json)
    }

    fun downloadCharacters(preferencesManager: PreferencesManager) {
        prefs = preferencesManager

        // Intentamos cargar la lista persistida para el usuario actual
        val savedJson = prefs.getCharactersList(getUserId())
        if (savedJson.isNotEmpty()) {
            try {
                val savedCharacters = Gson().fromJson(savedJson, Array<Characters>::class.java).toList()
                if (savedCharacters.isNotEmpty()) {
                    charactersList = savedCharacters
                    _uiState.value = State.Success(charactersList)
                    return
                }
            } catch (e: Exception) {
                // Si falla la conversión, se ignora y se continúa
            }
        }

        // Si no hay datos persistidos, se descargan desde el servidor
        if (charactersList.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = State.Loading

            val userRepository = UserRepository(prefs)
            val charactersRepository = CharactersRepository(prefs)

            userRepository.loadTokenIfNeeded()
            val token = userRepository.getToken()
            if (token.isBlank()) {
                _uiState.value = State.Error("El token está vacío", 401)
                return@launch
            }

            when (val response = charactersRepository.fetchCharacters(token)) {
                is CharactersRepository.CharactersResponse.Success -> {
                    charactersList = response.characters
                    persistCharacters()
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
            if (hero.isDead) return@launch  // No se cura individualmente un héroe muerto
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
            if (hero.isDead) return@launch  // No se daña un héroe ya muerto
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
            _uiState.value = State.CharacterSelected(character)
        }
    }
}