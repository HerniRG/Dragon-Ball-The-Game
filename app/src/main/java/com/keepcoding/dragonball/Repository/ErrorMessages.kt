package com.keepcoding.dragonball.Repository

import com.keepcoding.dragonball.R

object ErrorMessages {
    fun getErrorMessage(errorCode: Int): Int {
        return when (errorCode) {
            401 -> R.string.error_unauthorized
            500 -> R.string.error_server
            else -> R.string.error_unknown
        }
    }
}