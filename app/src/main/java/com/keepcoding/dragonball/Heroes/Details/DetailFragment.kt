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
import com.keepcoding.dragonball.R
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

            timesSelectedText.text = resources.getQuantityString(
                R.plurals.times_selected,
                hero.timesSelected,
                hero.timesSelected
            )

            animateProgressBar(hero.currentLife, hero.totalLife)
            animateLifeText(hero.currentLife, hero.totalLife)

            updateButtonsState(hero.isDead)

            if (hero.currentLife in 1..20 && !hero.isTransformed && !hero.isDead) {
                if (buttonTransform.visibility == View.GONE) {
                    buttonTransform.visibility = View.VISIBLE
                    buttonTransform.alpha = 0f
                    buttonTransform.scaleX = 0.8f
                    buttonTransform.scaleY = 0.8f

                    buttonTransform.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(500)
                        .start()
                }
            } else {
                buttonTransform.visibility = View.GONE
            }

            Glide.with(root)
                .load(hero.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .centerInside()
                .into(imageHeroDetail)
        }
    }

    private fun animateProgressBar(newProgress: Int, maxProgress: Int) {
        binding.lifeBarDetail.max = maxProgress
        val currentProgress = binding.lifeBarDetail.progress
        if (currentProgress != newProgress) {
            ObjectAnimator.ofInt(binding.lifeBarDetail, "progress", currentProgress, newProgress)
                .apply { duration = 500L }.start()
        }
    }

    private fun animateLifeText(newLife: Int, totalLife: Int) {
        binding.lifeInfoDetail.animate().alpha(0f).setDuration(200).withEndAction {
            binding.lifeInfoDetail.text = getString(R.string.life_placeholder, newLife, totalLife)
            binding.lifeInfoDetail.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    private fun animateTransformEffect() {
        binding.flashEffect.visibility = View.VISIBLE
        binding.flashEffect.alpha = 1f
        binding.flashEffect.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.flashEffect.alpha = 1f
                binding.flashEffect.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        binding.flashEffect.visibility = View.GONE
                    }.start()
            }.start()

        binding.imageHeroDetail.animate()
            .scaleX(1.2f).scaleY(1.2f)
            .alpha(0.7f)
            .setDuration(300)
            .withEndAction {
                binding.imageHeroDetail.animate()
                    .scaleX(1.05f).scaleY(1.05f)
                    .setDuration(100)
                    .withEndAction {
                        binding.imageHeroDetail.animate()
                            .scaleX(1.15f).scaleY(1.15f)
                            .setDuration(100)
                            .withEndAction {
                                binding.imageHeroDetail.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .alpha(1f)
                                    .setDuration(300)
                                    .start()
                            }.start()
                    }.start()
            }.start()

        binding.lifeBarDetail.animate()
            .scaleX(1.1f).scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                binding.lifeBarDetail.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(200)
                    .start()
            }.start()

        // ✨ Brillo en botones de acción
        val buttons = listOf(binding.buttonDamage, binding.buttonHeal)
        buttons.forEach { button ->
            button.animate()
                .scaleX(1.1f).scaleY(1.1f)
                .setDuration(200)
                .withEndAction {
                    button.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(200)
                        .start()
                }.start()
        }
    }

    private fun updateButtonsState(isDead: Boolean) {
        with(binding) {
            if (isDead) {
                Toast.makeText(context, getString(R.string.hero_dead, nameHero.text), Toast.LENGTH_SHORT).show()
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

        binding.buttonTransform.setOnClickListener {
            (viewModel.uiState.value as? HeroesViewModel.State.CharacterSelected)?.characters?.let { hero ->
                viewModel.transformHero(hero)
                animateTransformEffect()
                Toast.makeText(context, getString(R.string.hero_transformed, hero.name), Toast.LENGTH_SHORT).show()
            }
        }
    }
}