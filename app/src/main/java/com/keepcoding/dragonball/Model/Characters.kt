package com.keepcoding.dragonball.Model

data class Characters(
    val id: String,
    val name: String,
    val imageUrl : String,
    var currentLife : Int,
    val totalLife: Int = 100,
    var timesSelected: Int = 0)