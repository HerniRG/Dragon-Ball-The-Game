package com.keepcoding.dragonball.Heroes.List

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.keepcoding.dragonball.Heroes.HeroesViewModel
import com.keepcoding.dragonball.Heroes.Navigation
import com.keepcoding.dragonball.databinding.FragmentListBinding
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private lateinit var adapter: HeroesAdapter
    private lateinit var binding: FragmentListBinding
    private val viewModel: HeroesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        initViews()
        setObservers()
        setupListeners()
        return binding.root
    }

    private fun initViews() {
        adapter = HeroesAdapter(
            onCharacterClicked = { character ->
                viewModel.selectedCharacter(character)
            }
        )
        binding.heroesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.heroesRecyclerView.adapter = adapter
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HeroesViewModel.State.Loading -> { showLoading() }
                    is HeroesViewModel.State.Success -> {
                        hideLoading()
                        adapter.updateHeroes(state.heroes)
                    }
                    is HeroesViewModel.State.CharacterSelected -> {
                        (activity as? Navigation)?.navToDetail()
                    }
                    is HeroesViewModel.State.Error -> {
                        Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabHealAll.setOnClickListener {
            viewModel.healAllHeroes()
            Toast.makeText(context, "Todos los héroes han sido curados 🎉", Toast.LENGTH_SHORT).show()
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