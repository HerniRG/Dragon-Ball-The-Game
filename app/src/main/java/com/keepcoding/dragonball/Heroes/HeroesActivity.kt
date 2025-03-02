package com.keepcoding.dragonball.Heroes

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.Repository.CharactersRepository
import com.keepcoding.dragonball.Repository.UserRepository
import com.keepcoding.dragonball.Heroes.Details.DetailFragment
import com.keepcoding.dragonball.Heroes.List.ListFragment
import com.keepcoding.dragonball.Login.LoginActivity
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.databinding.ActivityHeroesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface Navigation {
    fun navToList()
    fun navToDetail()
}

class HeroesActivity : AppCompatActivity(), Navigation {

    private lateinit var binding: ActivityHeroesBinding
    private lateinit var preferencesManager: PreferencesManager

    private val viewModel: HeroesViewModel by viewModels {
        HeroesViewModelFactory(
            userRepository = UserRepository(preferencesManager),
            charactersRepository = CharactersRepository(preferencesManager),
            preferencesManager = preferencesManager
        )
    }

    private lateinit var listFragment: ListFragment
    private lateinit var detailFragment: DetailFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeroesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        preferencesManager = PreferencesManager(getSharedPreferences("loginPrefs", MODE_PRIVATE))

        listFragment = ListFragment()
        detailFragment = DetailFragment()
        initFragments()

        viewModel.downloadCharacters()
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.heroes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.logout()
                goToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(binding.fragmentContainer.id, detailFragment, "DetailFragment")
            hide(detailFragment)
            add(binding.fragmentContainer.id, listFragment, "ListFragment")
            show(listFragment)
            commit()
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state is HeroesViewModel.State.CharacterSelected) {
                    navToDetail()
                }
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_right)
        startActivity(intent, options.toBundle())
        finish()
    }

    override fun navToList() {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            hide(detailFragment)
            show(listFragment)
            commit()
        }
    }

    override fun navToDetail() {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            hide(listFragment)
            show(detailFragment)
            addToBackStack(null)
            commit()
        }
    }
}