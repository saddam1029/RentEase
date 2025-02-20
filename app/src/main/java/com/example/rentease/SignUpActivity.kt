package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
//
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Get the selected RadioButton state
        val selectedRoleId = binding.rgChoice.checkedRadioButtonId
        val selectedRoleButton = findViewById<RadioButton>(selectedRoleId)

//        // Reset all RadioButton text colors to default (black or as per your unselected color)
//        for (i in 0 until binding.rgChoice.childCount) {
//            val radioButton = binding.rgChoice.getChildAt(i) as? RadioButton
//            radioButton?.setTextColor(ContextCompat.getColor(this, android.R.color.black))
//        }

        // Set the selected RadioButton's text color to white
        selectedRoleButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        binding.btSighUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()

            // Get the selected RadioButton state
            val selectedRoleId = binding.rgChoice.checkedRadioButtonId
            val selectedRole = findViewById<RadioButton>(selectedRoleId).text.toString()

            // Validate input fields
            if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() &&
                lastName.isNotEmpty()
            ) {
                // Create user with Firebase Authentication
                createUserWithEmailAndPassword(email, password, firstName, lastName, selectedRole)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
                    // User created successfully
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Save user data in Firebase Realtime Database
                        saveUserData(userId, firstName, lastName, email, selectedRole)
                    }
                } else {
                    // Handle failed user creation
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
        // Create a map of the user data
        val userMap = hashMapOf(
            "name" to "$firstName $lastName",
            "email" to email,
            "type" to selectedRole,
            "userId" to userId
        )

        // Determine the parent node based on the selected role
        val parentNode = if (selectedRole.equals("Regular User", ignoreCase = true)) {
            "Regular User"
        } else {
            "Realtor"
        }

        // Save the data under the appropriate parent node
        database.child(parentNode).child(userId).child("userInfo").setValue(userMap)
            .addOnSuccessListener {
                // Show success message
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                try {
                    // Navigate to the appropriate home activity based on user role
                    if (selectedRole.equals("Realtor", ignoreCase = true)) {
                        startActivity(Intent(this, RealtorActivity::class.java))
                    } else if (selectedRole.equals("Regular User", ignoreCase = true)) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }

                    finish() // Close the SignUp activity
                } catch (e: Exception) {
                    // Log the exception if navigation fails
                    e.printStackTrace()
                    Toast.makeText(this, "Error navigating to home: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to save user data
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Navigate to the LoginActivity when back button is pressed
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Optional: this will ensure the SignUpActivity is closed when navigating to LoginActivity
    }


}