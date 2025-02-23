package com.keepcoding.dragonball.Login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.keepcoding.dragonball.Heroes.HeroesActivity
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

        setupListeners()
        setObservers()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            viewModel.login(email, password)
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
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressIndicator.visibility = View.VISIBLE
        binding.loginFormContainer.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        binding.progressIndicator.visibility = View.GONE
        binding.loginFormContainer.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHeroes() {
        showToast("Login exitoso")
        val intent = Intent(this, HeroesActivity::class.java)
        startActivity(intent)
        finish()
    }
}