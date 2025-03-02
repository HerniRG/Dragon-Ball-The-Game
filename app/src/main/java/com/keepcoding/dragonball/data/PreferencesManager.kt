package com.keepcoding.dragonball.data

import android.content.SharedPreferences

open class PreferencesManager(private val sharedPrefs: SharedPreferences) {

    companion object {
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

    fun getToken(): String = sharedPrefs.getString(KEY_TOKEN, "") ?: ""

    fun clearToken() {
        sharedPrefs.edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    open fun saveUserAndPass(user: String, password: String) {
        sharedPrefs.edit()
            .putString(KEY_USER, user)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    open fun getUserAndPass(): Pair<String, String>? {
        val user = sharedPrefs.getString(KEY_USER, "") ?: ""
        val pass = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""
        return if (user.isNotEmpty() && pass.isNotEmpty()) user to pass else null
    }

    open fun clearUserAndPass() {
        sharedPrefs.edit()
            .remove(KEY_USER)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun saveCharactersList(userId: String, charactersJson: String) {
        sharedPrefs.edit()
            .putString("$KEY_CHARACTERS_LIST$userId", charactersJson)
            .apply()
    }

    fun getCharactersList(userId: String): String {
        return sharedPrefs.getString("$KEY_CHARACTERS_LIST$userId", "") ?: ""
    }
}