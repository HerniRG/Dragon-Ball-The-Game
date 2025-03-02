package com.keepcoding.dragonball.Model

data class Characters(
    val id: String,
    val name: String,
    val imageUrl: String,
    val currentLife: Int,
    val totalLife: Int,
    val timesSelected: Int,
    val isDead: Boolean = false,
    val isTransformed: Boolean = false
)