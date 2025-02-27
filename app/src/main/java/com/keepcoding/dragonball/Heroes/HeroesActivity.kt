package com.keepcoding.dragonball.Heroes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.keepcoding.dragonball.Heroes.Data.PreferencesMagager
import com.keepcoding.dragonball.Heroes.Details.DetailFragment
import com.keepcoding.dragonball.Heroes.List.ListFragment
import com.keepcoding.dragonball.Login.LoginActivity
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.databinding.ActivityHeroesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface Navigation {
    fun navToList()
    fun navToDetail()
}

class HeroesActivity : AppCompatActivity(), Navigation {

    private lateinit var binding: ActivityHeroesBinding
    private val viewModel: HeroesViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesMagager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeroesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        preferencesManager = PreferencesMagager(getSharedPreferences("loginPrefs", MODE_PRIVATE))

        // Descargamos personajes
        viewModel.downloadCharacters(preferencesManager)

        initFragments()
        setupListeners()
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.heroes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.logout(preferencesManager)
                goToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initFragments() {
        navToList()
    }

    private fun setupListeners() {
        binding.fabHealAll.setOnClickListener {
            viewModel.healAllHeroes()
            // Se muestra el Toast cada vez que se pulsa el botón
            Toast.makeText(this, "Todos los héroes han sido curados 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HeroesViewModel.State.Success -> { }
                    is HeroesViewModel.State.Error -> {
                        Toast.makeText(this@HeroesActivity, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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