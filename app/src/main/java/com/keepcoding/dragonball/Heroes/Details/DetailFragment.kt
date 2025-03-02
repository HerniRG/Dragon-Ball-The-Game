package com.keepcoding.dragonball.Heroes.Details

import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.keepcoding.dragonball.Heroes.HeroesViewModel
import com.keepcoding.dragonball.Heroes.Navigation
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.databinding.FragmentDetailBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: HeroesViewModel by activityViewModels()

    private val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        initObservers()
        initListeners()
        return binding.root
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state is HeroesViewModel.State.CharacterSelected) {
                    updateHeroUI(state.characters)
                }
            }
        }
    }

    private fun updateHeroUI(hero: Characters) {
        with(binding) {
            nameHero.text = hero.name

            animateProgressBar(hero.currentLife)
            animateLifeText(hero.currentLife, hero.totalLife)

            updateButtonsState(hero.isDead)

            Glide.with(root)
                .load(hero.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .centerInside()
                .into(imageHeroDetail)
        }
    }

    private fun animateProgressBar(newProgress: Int) {
        val currentProgress = binding.lifeBarDetail.progress
        if (currentProgress != newProgress) {
            ObjectAnimator.ofInt(binding.lifeBarDetail, "progress", currentProgress, newProgress)
                .apply { duration = 500L }.start()
        }
    }

    private fun animateLifeText(newLife: Int, totalLife: Int) {
        binding.lifeInfoDetail.animate().alpha(0f).setDuration(200).withEndAction {
            "Vida: $newLife/$totalLife".also { binding.lifeInfoDetail.text = it }
            binding.lifeInfoDetail.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    private fun updateButtonsState(isDead: Boolean) {
        with(binding) {
            if (isDead) {
                Toast.makeText(context, "${nameHero.text} ha muerto", Toast.LENGTH_SHORT).show()
                disableButtons()
                lifecycleScope.launch {
                    delay(1000)
                    (activity as? Navigation)?.navToList()
                }
            } else {
                enableButtons()
            }
        }
    }

    private fun enableButtons() {
        binding.buttonDamage.animate().alpha(1f).setDuration(300).start()
        binding.buttonHeal.animate().alpha(1f).setDuration(300).start()
        binding.buttonDamage.isEnabled = true
        binding.buttonHeal.isEnabled = true
    }

    private fun disableButtons() {
        binding.buttonDamage.animate().alpha(0.5f).setDuration(300).start()
        binding.buttonHeal.animate().alpha(0.5f).setDuration(300).start()
        binding.buttonDamage.isEnabled = false
        binding.buttonHeal.isEnabled = false
    }

    private fun initListeners() {
        binding.buttonGoToList.setOnClickListener {
            (activity as? Navigation)?.navToList()
        }

        binding.buttonDamage.setOnClickListener {
            (viewModel.uiState.value as? HeroesViewModel.State.CharacterSelected)?.characters?.let {
                viewModel.damageHero(it)
            }
        }

        binding.buttonHeal.setOnClickListener {
            (viewModel.uiState.value as? HeroesViewModel.State.CharacterSelected)?.characters?.let {
                viewModel.healHero(it)
            }
        }
    }
}