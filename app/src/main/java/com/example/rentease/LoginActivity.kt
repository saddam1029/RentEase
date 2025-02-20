package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rentease.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Navigate to SignUpActivity when "Sign Up" is clicked
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Handle login button click
        binding.btSighIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            handleLogin(email, password)
        }
    }

    private fun handleLogin(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            // Show progress bar
            binding.progressBar.visibility = View.VISIBLE

            // Normal login flow
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Hide progress bar regardless of the outcome
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        saveLoginState()
                        val userId = auth.currentUser?.uid ?: ""
                        determineUserType(userId)
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLoginState() {
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPref.edit().putBoolean("isLoggedIn", true).apply()
    }

    private fun determineUserType(userId: String) {
        binding.progressBar.visibility = View.VISIBLE // Show progress bar
        database.child("Realtor").child(userId).child("userInfo").child("type").get()
            .addOnSuccessListener { providerSnapshot ->
                binding.progressBar.visibility = View.GONE // Hide progress bar
                if (providerSnapshot.exists()) {
                    navigateToHome()
                } else {
                    database.child("Regular User").child(userId).child("userInfo").child("type").get()
                        .addOnSuccessListener { clientSnapshot ->
                            binding.progressBar.visibility = View.GONE // Hide progress bar
                            if (clientSnapshot.exists()) {
                                navigateToRegularUserHome()
                            } else {
                                Toast.makeText(this, "Unknown user type", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            binding.progressBar.visibility = View.GONE // Hide progress bar
                            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE // Hide progress bar
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        Log.d("LoginActivity", "User is a Realtor")
        startActivity(Intent(this, RealtorActivity::class.java))
        finish() // Ensure the user cannot return to the login screen
    }

    private fun navigateToRegularUserHome() {
        Log.d("LoginActivity", "User is a Regular User")
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Ensure the user cannot return to the login screen
    }
}