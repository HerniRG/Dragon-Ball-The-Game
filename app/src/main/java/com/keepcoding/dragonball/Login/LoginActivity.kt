package com.keepcoding.dragonball.Login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
        enableEdgeToEdge()

        setupAnimations()
        setupListeners()
        setObservers()

        // 1) Verificar si ya hay un token almacenado (usuario logueado) para saltarse el login
        viewModel.checkIfLoggedIn(getSharedPreferences("loginPrefs", MODE_PRIVATE))

        // 2) (Opcional) Recuperar credenciales guardadas y rellenar los campos de texto
        viewModel.getStoredCredentials(getSharedPreferences("loginPrefs", MODE_PRIVATE))?.let { (usuario, password) ->
            binding.editTextEmail.setText(usuario)
            binding.editTextPassword.setText(password)
        }
    }

    private fun setupAnimations() {
        // Animación para la imagen del header (fade in)
        binding.imageHeader?.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Animación para la entrada del formulario con efecto de rebote
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

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                showToast("Por favor, introduce usuario y contraseña")
                return@setOnClickListener
            }

            // 3) Iniciar el login con el método del ViewModel
            viewModel.login(
                user = email,
                password = password,
                preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
            )

            // Efecto de pulsación en el botón
            binding.buttonLogin.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(150)
                .withEndAction {
                    binding.buttonLogin.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()

            // 4) (Opcional) Guardar usuario y contraseña
            //    Supongamos que tienes un "checkBoxRememberMe" en el layout:
            //    if (binding.checkBoxRememberMe.isChecked) {
            //        viewModel.saveUserAndPass(
            //            getSharedPreferences("loginPrefs", MODE_PRIVATE),
            //            email,
            //            password
            //        )
            //    }
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is LoginViewModel.State.Idle -> {
                        // Lógica para Idle
                        hideLoading()
                    }
                    is LoginViewModel.State.Loading -> {
                        // Lógica para Loading
                        showLoading()
                    }
                    is LoginViewModel.State.Success -> {
                        // Lógica para Success
                        hideLoading()
                        navigateToHeroes()
                    }
                    is LoginViewModel.State.Error -> {
                        // Lógica para Error
                        hideLoading()
                        showToast("Error: ${state.message} (Código: ${state.errorCode})")
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressIndicator.apply {
            isVisible = true
            isIndeterminate = true
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .start()
        }
        binding.loginFormContainer.animate()
            .alpha(0.5f)
            .setDuration(300)
            .start()
    }

    private fun hideLoading() {
        binding.progressIndicator.apply {
            isIndeterminate = false
            animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction { isVisible = false }
                .start()
        }
        binding.loginFormContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHeroes() {
        // No pasamos el token
        val intent = Intent(this, HeroesActivity::class.java)
        startActivity(intent)
        showToast("Login exitoso")
    }
}