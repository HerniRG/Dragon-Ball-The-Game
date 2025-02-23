package com.keepcoding.dragonball.Heroes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.keepcoding.dragonball.databinding.ActivityHeroesBinding
import kotlinx.coroutines.launch

class HeroesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeroesBinding
    private val viewModel: HeroesViewModel by viewModels()
    private lateinit var adapter: HeroesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeroesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setObservers()
        viewModel.downloadCharacters()
    }

    private fun initViews() {
        adapter = HeroesAdapter(
            //lambda
            onCharacterClicked = { character ->
                Toast.makeText(this, "Click en ${character.name}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.heroesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.heroesRecyclerView.adapter = adapter
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HeroesViewModel.State.Loading -> showLoading()
                    is HeroesViewModel.State.Success -> {
                        hideLoading()
                        adapter.updateHeroes(state.heroes)
                    }
                    is HeroesViewModel.State.Error -> {
                        hideLoading()
                        // Show error message
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressIndicator.visibility = View.VISIBLE
        binding.heroesRecyclerView.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        binding.progressIndicator.visibility = View.GONE
        binding.heroesRecyclerView.visibility = View.VISIBLE

    }
}