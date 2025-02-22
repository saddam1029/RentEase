package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.rentease.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btSighUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()

            val selectedRoleId = binding.rgChoice.checkedRadioButtonId
            val selectedRole = if (selectedRoleId != -1) {
                findViewById<RadioButton>(selectedRoleId).text.toString()
            } else {
                ""
            }

            // **Validation Checks**
            if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
            } else {
                // If validation passes, create user
                createUserWithEmailAndPassword(email, password, firstName, lastName, selectedRole)
            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        selectedRole: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserData(userId, firstName, lastName, email, selectedRole)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserData(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        selectedRole: String
    ) {
        val userMap = hashMapOf(
            "name" to "$firstName $lastName",
            "email" to email,
            "type" to selectedRole,
            "userId" to userId
        )

        val parentNode = if (selectedRole.equals("Regular User", ignoreCase = true)) {
            "Regular User"
        } else {
            "Realtor"
        }

        database.child(parentNode).child(userId).child("userInfo").setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                navigateToHome(selectedRole)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome(selectedRole: String) {
        try {
            val intent = if (selectedRole.equals("Realtor", ignoreCase = true)) {
                Intent(this, RealtorActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error navigating to home: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
