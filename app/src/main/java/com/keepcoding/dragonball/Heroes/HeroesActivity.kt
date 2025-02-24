package com.keepcoding.dragonball.Heroes

import android.content.Context
import android.content.Intent
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

    companion object {
        private val TAG_TOKEN = "token"
        fun startHeroesActivity(context: Context, token: String) {
            val intent = Intent(context, HeroesActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityHeroesBinding
    private val viewModel: HeroesViewModel by viewModels()
    private lateinit var adapter: HeroesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeroesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val token = intent.getStringExtra("token")
        token?.let {
            viewModel.setToken(token)
        } ?: run {
            Toast.makeText(this, "Token no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

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