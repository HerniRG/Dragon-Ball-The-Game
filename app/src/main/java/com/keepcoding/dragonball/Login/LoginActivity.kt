package com.keepcoding.dragonball.Login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.keepcoding.dragonball.R
import com.keepcoding.dragonball.data.PreferencesManager
import com.keepcoding.dragonball.databinding.ActivityLoginBinding
import com.keepcoding.dragonball.Heroes.HeroesActivity
import com.keepcoding.dragonball.Repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(getSharedPreferences("loginPrefs", MODE_PRIVATE))
    }
    private val userRepository: UserRepository by lazy {
        UserRepository(preferencesManager)
    }
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(userRepository, preferencesManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupAnimations()
        setupListeners()
        setObservers()

        viewModel.checkIfLoggedIn()

        val storedCredentials = viewModel.getStoredCredentials()
        storedCredentials?.let { (email, password) ->
            binding.editTextEmail.setText(email)
            binding.editTextPassword.setText(password)
            binding.checkBoxRememberMe.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                showToast(R.string.error_empty_credentials)
                return@setOnClickListener
            }

            viewModel.login(email, password)

            if (binding.checkBoxRememberMe.isChecked) {
                viewModel.saveUserAndPass(email, password)
            } else {
                viewModel.clearUserAndPass()
            }
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is LoginViewModel.State.Idle -> hideLoading()
                    is LoginViewModel.State.Loading -> showLoading()
                    is LoginViewModel.State.Success -> {
                        hideLoading()
                        navigateToHeroes()
                    }
                    is LoginViewModel.State.Error -> {
                        hideLoading()
                        showToast(state.errorResId)                    }
                }
            }
        }
    }

    private fun setupAnimations() {
        binding.imageHeader.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
        val alphaAnim = ObjectAnimator.ofFloat(binding.loginFormContainer, "alpha", 0f, 1f)
        val translateAnim = ObjectAnimator.ofFloat(binding.loginFormContainer, "translationY", 50f, 0f)
        val scaleXAnim = ObjectAnimator.ofFloat(binding.loginFormContainer, "scaleX", 0.9f, 1f)
        val scaleYAnim = ObjectAnimator.ofFloat(binding.loginFormContainer, "scaleY", 0.9f, 1f)

        AnimatorSet().apply {
            playTogether(alphaAnim, translateAnim, scaleXAnim, scaleYAnim)
            duration = 1000
            interpolator = OvershootInterpolator()
            startDelay = 300
            start()
        }
    }

    private fun navigateToHeroes() {
        startActivity(Intent(this, HeroesActivity::class.java))
        showToast(R.string.login_success)
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.progressIndicator.isVisible = true
        binding.progressIndicator.isIndeterminate = true
        binding.progressIndicator.animate().alpha(1f).setDuration(300).start()

        binding.loginFormContainer.animate().alpha(0.5f).setDuration(300).start()
    }

    private fun hideLoading() {
        binding.progressIndicator.isVisible = false
        binding.loginFormContainer.animate().alpha(1f).setDuration(300).start()
    }
}