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
import com.keepcoding.dragonball.Heroes.HeroesActivity
import com.keepcoding.dragonball.LoginViewModel
import com.keepcoding.dragonball.databinding.ActivityLoginBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        setupListeners()
        setObservers()

        // Cargar preferencias
        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        // Verificar si ya hay un usuario logueado
        viewModel.checkIfLoggedIn(prefs)

        // Recuperar credenciales guardadas y marcar el CheckBox si estaban guardadas
        val storedCredentials = viewModel.getStoredCredentials(prefs)
        if (storedCredentials != null) {
            binding.editTextEmail.setText(storedCredentials.first)
            binding.editTextPassword.setText(storedCredentials.second)
            binding.checkBoxRememberMe.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                showToast("Por favor, introduce usuario y contraseña")
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
            viewModel.login(email, password, prefs)

            // Guardar credenciales si el usuario marcó "Recordar usuario"
            if (binding.checkBoxRememberMe.isChecked) {
                viewModel.saveUserAndPass(prefs, email, password)
            } else {
                viewModel.clearUserAndPass(prefs)  // Borra usuario si desmarcan el check
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
                        showToast("Error: ${state.message} (Código: ${state.errorCode})")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupAnimations() {
        binding.imageHeader?.apply {
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
        showToast("Login exitoso")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.progressIndicator.isVisible = true
        binding.loginFormContainer.animate().alpha(0.5f).setDuration(300).start()
    }

    private fun hideLoading() {
        binding.progressIndicator.isVisible = false
        binding.loginFormContainer.animate().alpha(1f).setDuration(300).start()
    }
}