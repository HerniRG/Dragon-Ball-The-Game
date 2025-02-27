package com.keepcoding.dragonball.Repository

import android.util.Base64
import com.keepcoding.dragonball.Heroes.Data.PreferencesMagager
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class UserRepository(private val localDataSource: PreferencesMagager) {

    companion object {
        private var token = ""
    }

    sealed class LoginResponse {
        object Success : LoginResponse()
        data class Error(val message: String) : LoginResponse()
    }

    fun login(user: String, password: String): LoginResponse {
        return try {
            val userAndPass = "$user:$password"
            val base64UserPass = Base64.encodeToString(userAndPass.toByteArray(), Base64.NO_WRAP)

            val client = OkHttpClient()
            val url = "https://dragonball.keepcoding.education/api/auth/login"
            val formBody = FormBody.Builder().build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("Authorization", "Basic $base64UserPass")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string().orEmpty()
                token = responseBody
                // Guardamos en local
                localDataSource.saveToken(token)
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
            token = localDataSource.getToken()
        }
        return token
    }

    fun clearToken() {
        token = ""
        localDataSource.clearToken()
    }

    fun loadTokenIfNeeded() {
        if (token.isEmpty()) {
            token = localDataSource.getToken()
        }
    }
}