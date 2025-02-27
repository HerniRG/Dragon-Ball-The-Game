package com.keepcoding.dragonball.Heroes.Data

import android.content.SharedPreferences

class PreferencesMagager(private val sharedPrefs: SharedPreferences) {

    companion object {
        // Clave unificada para token
        private const val KEY_TOKEN = "userToken"
        private const val KEY_USER = "User"
        private const val KEY_PASSWORD = "Password"
        private const val KEY_CHARACTERS_LIST = "charactersList"
    }

    fun saveToken(token: String) {
        sharedPrefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(): String {
        return sharedPrefs.getString(KEY_TOKEN, "") ?: ""
    }

    fun clearToken() {
        sharedPrefs.edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    fun saveUserAndPass(user: String, password: String) {
        sharedPrefs.edit()
            .putString(KEY_USER, user)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun getUserAndPass(): Pair<String, String>? {
        val user = sharedPrefs.getString(KEY_USER, "") ?: ""
        val pass = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""
        return if (user.isNotEmpty() && pass.isNotEmpty()) {
            user to pass
        } else null
    }

    fun clearUserAndPass() {
        sharedPrefs.edit()
            .remove(KEY_USER)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun saveCharactersList(charactersJson: String) {
        sharedPrefs.edit()
            .putString(KEY_CHARACTERS_LIST, charactersJson)
            .apply()
    }

    fun getCharactersList(): String {
        return sharedPrefs.getString(KEY_CHARACTERS_LIST, "") ?: ""
    }
}