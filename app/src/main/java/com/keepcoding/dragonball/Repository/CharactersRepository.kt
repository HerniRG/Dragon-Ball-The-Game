package com.keepcoding.dragonball.Repository

import com.google.gson.Gson
import com.keepcoding.dragonball.Heroes.Data.PreferencesMagager
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Model.CharactersDTO
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class CharactersRepository(private val localDataSource: PreferencesMagager) {

    private val BASE_URL = "https://dragonball.keepcoding.education/api/"
    private var charactersList = listOf<Characters>()

    sealed class CharactersResponse {
        data class Success(val characters: List<Characters>) : CharactersResponse()
        data class Error(val message: String) : CharactersResponse()
    }

    fun fetchCharacters(token: String): CharactersResponse {
        // Si ya están en memoria, devolvemos directamente
        if (charactersList.isNotEmpty()) {
            return CharactersResponse.Success(charactersList)
        }

        // Miramos si existe en local
        val cachedJson = localDataSource.getCharactersList()
        if (cachedJson.isNotEmpty()) {
            val charactersArray = Gson().fromJson(cachedJson, Array<Characters>::class.java)
            if (!charactersArray.isNullOrEmpty()) {
                charactersList = charactersArray.toList()
                return CharactersResponse.Success(charactersList)
            }
        }

        // Si no están en local, los descargamos de internet
        val client = OkHttpClient()
        val url = "${BASE_URL}heros/all"
        val formBody = FormBody.Builder()
            .add("name", "")
            .build()

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

            // Convertimos y guardamos
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
            localDataSource.saveCharactersList(Gson().toJson(charactersList))

            CharactersResponse.Success(charactersList)
        } else {
            CharactersResponse.Error("Error al descargar los personajes: ${response.message}")
        }
    }

    fun updateCharacterLife(characterId: String, damage: Int): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error("No hay personajes cargados.")
        }
        val updatedList = charactersList.map { character ->
            if (character.id == characterId) {
                val newLife = (character.currentLife - damage).coerceAtLeast(0)
                character.copy(currentLife = newLife)
            } else character
        }
        charactersList = updatedList

        // Guardar en local
        localDataSource.saveCharactersList(Gson().toJson(charactersList))
        return CharactersResponse.Success(charactersList)
    }

    fun incrementTimesSelected(characterId: String): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error("No hay personajes cargados.")
        }
        val updatedList = charactersList.map { character ->
            if (character.id == characterId) {
                character.copy(timesSelected = character.timesSelected + 1)
            } else character
        }
        charactersList = updatedList

        localDataSource.saveCharactersList(Gson().toJson(charactersList))
        return CharactersResponse.Success(charactersList)
    }
}