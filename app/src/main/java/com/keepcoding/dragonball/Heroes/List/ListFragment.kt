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
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.Repository.ErrorMessages
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
                    is HeroesViewModel.State.Loading -> showLoading()
                    is HeroesViewModel.State.Success -> {
                        hideLoading()
                        adapter.updateHeroes(state.heroes)
                    }
                    is HeroesViewModel.State.CharacterSelected -> {
                        (activity as? Navigation)?.navToDetail()
                    }
                    is HeroesViewModel.State.Error -> {
                        showToast(ErrorMessages.getErrorMessage(state.errorResId))
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabHealAll.setOnClickListener {
            viewModel.healAllHeroes()
            showToast(R.string.heal_all_heroes)
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

    private fun showToast(messageResId: Int) {
        Toast.makeText(context, getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}