package com.keepcoding.dragonball.Repository

import android.util.Base64
import com.keepcoding.dragonball.data.ApiConstants
import com.keepcoding.dragonball.data.PreferencesManager
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class UserRepository(private val preferencesManager: PreferencesManager) {

    companion object {
        private var token: String = ""
    }

    sealed class LoginResponse {
        object Success : LoginResponse()
        data class Error(val message: String) : LoginResponse()
    }

    fun login(user: String, password: String): LoginResponse {
        return try {
            val credentials = "$user:$password"
            val base64Credentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

            val client = OkHttpClient()
            val url = "${ApiConstants.BASE_URL}${ApiConstants.LOGIN_ENDPOINT}"
            val formBody = FormBody.Builder().build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("Authorization", "Basic $base64Credentials")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                token = response.body?.string().orEmpty()
                preferencesManager.saveToken(token)
                LoginResponse.Success
            } else {
                LoginResponse.Error("Error al hacer login. Código HTTP: ${response.code}")
            }
        } catch (e: Exception) {
            LoginResponse.Error("Error al hacer login: ${e.message}")
        }
    }

    fun getToken(): String {
        if (token.isEmpty()) {
            token = preferencesManager.getToken()
        }
        return token
    }

    fun clearToken() {
        token = ""
        preferencesManager.clearToken()
    }

    fun loadTokenIfNeeded() {
        if (token.isEmpty()) {
            token = preferencesManager.getToken()
        }
    }
}