package com.keepcoding.dragonball.Repository

import com.google.gson.Gson
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.data.ApiConstants
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Model.CharactersDTO
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class CharactersRepository(private val preferencesManager: PreferencesManager) {

    private var charactersList = listOf<Characters>()

    sealed class CharactersResponse {
        data class Success(val characters: List<Characters>) : CharactersResponse()
        data class Error(val errorResId: Int) : CharactersResponse()
    }

    private fun getUserId(): String =
        preferencesManager.getUserAndPass()?.first ?: "default"

    private fun loadFromCache(): Boolean {
        val cachedJson = preferencesManager.getCharactersList(getUserId())
        if (cachedJson.isNotEmpty()) {
            try {
                val charactersArray = Gson().fromJson(cachedJson, Array<Characters>::class.java)
                if (charactersArray.isNotEmpty()) {
                    charactersList = charactersArray.toList()
                    return true
                }
            } catch (_: Exception) { }
        }
        return false
    }

    private fun persistCharacters() {
        val json = Gson().toJson(charactersList)
        preferencesManager.saveCharactersList(getUserId(), json)
    }

    fun fetchCharacters(token: String): CharactersResponse {
        if (charactersList.isNotEmpty()) {
            return CharactersResponse.Success(charactersList)
        }
        if (loadFromCache()) {
            return CharactersResponse.Success(charactersList)
        }

        val client = OkHttpClient()
        val url = "${ApiConstants.BASE_URL}${ApiConstants.HEROS_ALL_ENDPOINT}"
        val formBody = FormBody.Builder().add("name", "").build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        return if (response.isSuccessful) {
            val responseBody = response.body?.string().orEmpty()
            val charactersDto: Array<CharactersDTO> =
                Gson().fromJson(responseBody, Array<CharactersDTO>::class.java)
            charactersList = charactersDto.map {
                Characters(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.photo,
                    currentLife = 100,
                    totalLife = 100,
                    timesSelected = 0
                )
            }
            persistCharacters()
            CharactersResponse.Success(charactersList)
        } else {
            CharactersResponse.Error(ErrorMessages.getErrorMessage(response.code))
        }
    }

    fun updateCharacterLife(characterId: String, damage: Int): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error(R.string.error_no_characters)
        }
        charactersList = charactersList.map { character ->
            if (character.id == characterId) {
                val newLife = (character.currentLife - damage).coerceAtLeast(0)
                character.copy(currentLife = newLife, isDead = (newLife == 0))
            } else character
        }
        persistCharacters()
        return CharactersResponse.Success(charactersList)
    }

    fun incrementTimesSelected(characterId: String): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error(R.string.error_no_characters)
        }
        charactersList = charactersList.map { character ->
            if (character.id == characterId) {
                character.copy(timesSelected = character.timesSelected + 1)
            } else character
        }
        persistCharacters()
        return CharactersResponse.Success(charactersList)
    }

    fun healCharacter(characterId: String, healAmount: Int): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error(R.string.error_no_characters)
        }
        charactersList = charactersList.map { character ->
            if (character.id == characterId) {
                val newLife = (character.currentLife + healAmount).coerceAtMost(character.totalLife)
                character.copy(currentLife = newLife)
            } else character
        }
        persistCharacters()
        return CharactersResponse.Success(charactersList)
    }

    fun healAllCharacters(): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error(R.string.error_no_characters)
        }
        charactersList = charactersList.map { character ->
            character.copy(currentLife = character.totalLife, isDead = false)
        }
        persistCharacters()
        return CharactersResponse.Success(charactersList)
    }
}