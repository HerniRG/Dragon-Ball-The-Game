package com.keepcoding.dragonball.Heroes.Details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.keepcoding.dragonball.Heroes.HeroesViewModel
import com.keepcoding.dragonball.databinding.FragmentDetailBinding
import kotlinx.coroutines.launch


class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: HeroesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(layoutInflater)
        initObservers()
        return binding.root
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HeroesViewModel.State.CharacterSelected -> {
                        binding.nameHero.text = state.characters.name
                        binding.lifeBar.progress = state.characters.currentLife
                    }
                    else -> Unit
                }
            }
        }
    }

}