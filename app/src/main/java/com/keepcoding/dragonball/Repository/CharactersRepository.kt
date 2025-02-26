package com.keepcoding.dragonball.Repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.Model.CharactersDTO
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class CharactersRepository {

    private val BASE_URL = "https://dragonball.keepcoding.education/api/"
    private var charactersList = listOf<Characters>()

    sealed class CharactersResponse {
        data class Success(val characters: List<Characters>) : CharactersResponse()
        data class Error(val message: String) : CharactersResponse()
    }

    //Si ya tenemos la lista en memoria, la devolvemos directamente. Si no, la descargamos de Internet.
    fun fetchCharacters(token: String, sharedPreferences: SharedPreferences? = null): CharactersResponse {
        // Si ya tenemos la lista en memoria, la devolvemos directamente.
        if (charactersList.isNotEmpty()) {
            return CharactersResponse.Success(charactersList)
        }

        // Comprobamos si existe en SharedPreferences
        sharedPreferences?.let {
            val charactersListJson = it.getString("charactersList", "")
            val charactersArray: Array<Characters>? =
                Gson().fromJson(charactersListJson, Array<Characters>::class.java)

            // Si la tenemos guardada, la usamos
            if (!charactersArray.isNullOrEmpty()) {
                charactersList = charactersArray.toList()
                return CharactersResponse.Success(charactersList)
            }
        }

        // Si no la tenemos en SharedPreferences, la descargamos de Internet
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

        val call = client.newCall(request)
        val response = call.execute()

        return if (response.isSuccessful) {
            val charactersDto: Array<CharactersDTO> =
                Gson().fromJson(response.body?.string(), Array<CharactersDTO>::class.java)

            // Convertimos DTO a nuestra clase Characters
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

            // Guardamos la lista en SharedPreferences
            sharedPreferences?.edit()?.apply {
                putString("charactersList", Gson().toJson(charactersList))
                apply()
            }

            CharactersResponse.Success(charactersList)
        } else {
            CharactersResponse.Error("Error al descargar los personajes: ${response.message}")
        }
    }

    // Actualiza la vida de un personaje y lo guarda en SharedPreferences
    fun updateCharacterLife(
        characterId: String,
        damage: Int,
        sharedPreferences: SharedPreferences?
    ): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error("No hay personajes cargados.")
        }

        // Buscamos el personaje en memoria
        val updatedList = charactersList.map { character ->
            if (character.id == characterId) {
                val newLife = (character.currentLife - damage).coerceAtLeast(0)
                character.copy(currentLife = newLife)
            } else {
                character
            }
        }

        // Actualizamos la lista en memoria
        charactersList = updatedList

        // Lo guardamos en SharedPreferences
        sharedPreferences?.edit()?.apply {
            putString("charactersList", Gson().toJson(charactersList))
            apply()
        }

        return CharactersResponse.Success(charactersList)
    }

    // Incrementa en 1 el contador de veces que se ha seleccionado un personaje
    fun incrementTimesSelected(
        characterId: String,
        sharedPreferences: SharedPreferences?
    ): CharactersResponse {
        if (charactersList.isEmpty()) {
            return CharactersResponse.Error("No hay personajes cargados.")
        }

        val updatedList = charactersList.map { character ->
            if (character.id == characterId) {
                character.copy(timesSelected = character.timesSelected + 1)
            } else {
                character
            }
        }

        charactersList = updatedList

        sharedPreferences?.edit()?.apply {
            putString("charactersList", Gson().toJson(charactersList))
            apply()
        }

        return CharactersResponse.Success(charactersList)
    }
}