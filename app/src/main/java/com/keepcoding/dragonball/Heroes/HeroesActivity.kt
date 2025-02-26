package com.keepcoding.dragonball.Heroes

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.keepcoding.dragonball.Repository.UserRepository
import com.keepcoding.dragonball.Heroes.Details.DetailFragment
import com.keepcoding.dragonball.Heroes.List.ListFragment
import com.keepcoding.dragonball.databinding.ActivityHeroesBinding

interface Navigation {
    fun navToList()
    fun navToDetail()
}

class HeroesActivity : AppCompatActivity(), Navigation {

    private lateinit var binding: ActivityHeroesBinding
    private val viewModel: HeroesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeroesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Cargamos el token desde SharedPreferences en UserRepository
        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        UserRepository().loadTokenFromPrefs(prefs)

        // 2) Descargamos los personajes pasando 'prefs' para que CharactersRepository pueda cachear
        viewModel.downloadCharacters(prefs)

        initFragments()
    }

    private fun initFragments() {
        navToList()
    }

    override fun navToList() {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainer.id, ListFragment())
            addToBackStack(null)
            commit()
        }
    }
    override fun navToDetail() {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainer.id, DetailFragment())
            addToBackStack(null)
            commit()
        }
    }
}