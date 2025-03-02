package com.keepcoding.dragonball.Heroes.List

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.databinding.HeroRowBinding

class HeroesAdapter(
    private val onCharacterClicked: (Characters) -> Unit
) : RecyclerView.Adapter<HeroesAdapter.CharacterViewHolder>() {

    private var heroes = listOf<Characters>()

    fun updateHeroes(newHeroes: List<Characters>) {
        heroes = newHeroes
        notifyDataSetChanged()
    }

    class CharacterViewHolder(
        private val onCharacterClicked: (Characters) -> Unit,
        private val binding: HeroRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Characters) {
            val context = binding.root.context

            binding.nameHero.text = character.name

            val newLifeText = context.getString(R.string.life_placeholder, character.currentLife, character.totalLife)
            binding.lifeInfo.animate().alpha(0f).setDuration(200).withEndAction {
                binding.lifeInfo.text = newLifeText
                binding.lifeInfo.animate().alpha(1f).setDuration(200).start()
            }.start()

            val currentProgress = binding.lifeBar.progress
            if (currentProgress != character.currentLife) {
                ObjectAnimator.ofInt(binding.lifeBar, "progress", currentProgress, character.currentLife)
                    .apply {
                        duration = 500L
                        start()
                    }
            } else {
                binding.lifeBar.progress = character.currentLife
            }
            binding.lifeBar.max = character.totalLife

            if (character.isDead) {
                binding.root.animate().alpha(0.5f).setDuration(300).start()
                binding.root.isClickable = false
                binding.overlayDead.visibility = View.VISIBLE
            } else {
                binding.root.animate().alpha(1f).setDuration(300).start()
                binding.root.isClickable = true
                binding.overlayDead.visibility = View.GONE
            }

            Glide.with(binding.root)
                .load(character.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerInside()
                .into(binding.imageHero)

            binding.root.setOnClickListener {
                if (!character.isDead) {
                    onCharacterClicked(character)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = HeroRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(onCharacterClicked, binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(heroes[position])
    }

    override fun getItemCount(): Int = heroes.size
}