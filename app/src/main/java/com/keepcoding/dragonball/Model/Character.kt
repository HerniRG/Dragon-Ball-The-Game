package com.keepcoding.dragonball.Model

data class Character(
    val id: String,
    val name: String,
    val imageUrl: String,
    val currentLife: Int,
    val totalLife: Int = 100
)