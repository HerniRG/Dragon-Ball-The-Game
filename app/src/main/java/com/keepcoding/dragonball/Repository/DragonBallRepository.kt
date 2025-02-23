/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.keepcoding.dragonball.Repository

import com.keepcoding.dragonball.Model.Character
import kotlinx.coroutines.delay

object DragonBallRepository {
    data class Response<T>(val isSuccessful: Boolean, val body: T, val message: String, val code: Int)
    data class LoginBody(val token: String)

    suspend fun login(email: String, password: String): Response<LoginBody> {
        delay(2000) // Simula una llamada de red
        return if (email == "a@b.com" && password == "1234") {
            Response(true, LoginBody("superSaiyanToken123"), "Success", 200)
        } else {
            Response(false, LoginBody(""), "Invalid credentials", 401)
        }
    }

    suspend fun getCharacters(): Response<List<Character>> {
        delay(2000) // Simula una carga de datos
        val characters = listOf(
            Character("1", "Goku", "https://cdn.alfabetajuega.com/alfabetajuega/2020/12/goku1.jpg", 100),
            Character("2", "Vegeta", "https://cdn.alfabetajuega.com/alfabetajuega/2020/12/vegetita.jpg", 90),
            Character("3", "Piccolo", "https://cdn.alfabetajuega.com/alfabetajuega/2020/09/piccolo.jpg", 80)
        )
        return Response(true, characters, "Success", 200)
    }
}