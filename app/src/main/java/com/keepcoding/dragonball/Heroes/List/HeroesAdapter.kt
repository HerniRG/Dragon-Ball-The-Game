/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.keepcoding.dragonball.Heroes.List

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.keepcoding.dragonball.Model.Characters
import com.keepcoding.dragonball.databinding.HeroRowBinding

class HeroesAdapter(
    private val onCharacterClicked: (Characters) -> Unit
) : RecyclerView.Adapter<HeroesAdapter.CharacterViewHolder>() {

    private var heroes = listOf<Characters>()

    fun updateHeroes(newHeroes: List<Characters>) {
        this.heroes = newHeroes
        notifyDataSetChanged()
    }

    class CharacterViewHolder(
        private val onCharacterClicked: (Characters) -> Unit,
        private val binding: HeroRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(characters: Characters) {
            binding.nameHero.text = characters.name
            binding.lifeInfo.text = "Life: ${characters.currentLife}/${characters.totalLife}"

            binding.lifeBar.max = characters.totalLife
            binding.lifeBar.progress = characters.currentLife

            Glide.with(binding.root)
                .load(characters.imageUrl)
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .centerInside()
                .into(binding.imageHero)

            binding.root.setOnClickListener {
                onCharacterClicked(characters)
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