package com.keepcoding.dragonball.Heroes

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.keepcoding.dragonball.Model.Character
import com.keepcoding.dragonball.databinding.HeroRowBinding

class HeroesAdapter(
    private val onCharacterClicked: (Character) -> Unit
) : RecyclerView.Adapter<HeroesAdapter.CharacterViewHolder>() {

    private var heroes = listOf<Character>()

    fun updateHeroes(newHeroes: List<Character>) {
        this.heroes = newHeroes
        notifyDataSetChanged()
    }

    class CharacterViewHolder(
        private val onCharacterClicked: (Character) -> Unit,
        private val binding: HeroRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Character) {
            binding.nameHero.text = character.name
            binding.lifeInfo.text = "Life: ${character.currentLife}/${character.totalLife}"

            binding.lifeBar.max = character.totalLife
            binding.lifeBar.progress = character.currentLife

            Glide.with(binding.root)
                .load(character.imageUrl)
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .centerInside()
                .into(binding.imageHero)

            binding.root.setOnClickListener {
                onCharacterClicked(character)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = HeroRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(
            onCharacterClicked,
            binding
        )
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(heroes[position])
    }

    override fun getItemCount(): Int = heroes.size
}