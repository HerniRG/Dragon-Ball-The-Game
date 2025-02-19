package com.keepcoding.dragonball

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.keepcoding.dragonball.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        viewModel.saveUser(
            preferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE),
            user ="hernan",
            password = "123456")

        // usar binding para acceder a los elementos de la vista
        binding.buttonLogin.setOnClickListener {
            // si ya he pulsado y aun sigue un toast ejecutandose lo cancela
            Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        }

    }
}