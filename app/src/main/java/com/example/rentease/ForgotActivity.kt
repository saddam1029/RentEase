package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rentease.databinding.ActivityForgotBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get references to views
        val emailInputLayout: TextInputLayout = findViewById(R.id.email_text_input_layout)

        val resetPasswordButton: Button = findViewById(R.id.btnResetPassword)

        resetPasswordButton.setOnClickListener {
            val email = emailInputLayout.editText?.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(email)
            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = task.exception?.message ?: "An error occurred"
                    Toast.makeText(this, "Failed to send reset email: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Navigate to the LoginActivity when back button is pressed
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Optional: this will ensure the SignUpActivity is closed when navigating to LoginActivity
    }
}