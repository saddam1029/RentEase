package com.example.rentease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Handle system insets for splash screen layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Show splash screen for 2 seconds
        lifecycleScope.launch {
            delay(2000)

            // Check if a user is already logged in
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid

                // Check if the user is a Provider (Realtor)
                database.child("Provider").child(userId).child("userInfo").child("type").get()
                    .addOnSuccessListener { providerSnapshot ->
                        if (providerSnapshot.exists()) {
                            // Navigate to RealtorActivity
                            startActivity(Intent(this@SplashActivity, RealtorActivity::class.java))
                            finish()
                        } else {
                            // Check if the user is a Client (Regular User)
                            database.child("Client").child(userId).child("userInfo").child("type").get()
                                .addOnSuccessListener { clientSnapshot ->
                                    if (clientSnapshot.exists()) {
                                        // Navigate to MainActivity
                                        Toast.makeText(this@SplashActivity, "Welcome Client", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                        finish()
                                    } else {
                                        // User type not found in both nodes
                                        Toast.makeText(
                                            this@SplashActivity,
                                            "Unknown user type. Redirecting to login.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navigateToLogin()
                                    }
                                }
                                .addOnFailureListener { clientError ->
                                    Toast.makeText(
                                        this@SplashActivity,
                                        "Failed to check Client node: ${clientError.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToLogin()
                                }
                        }
                    }
                    .addOnFailureListener { providerError ->
                        Toast.makeText(
                            this@SplashActivity,
                            "Failed to check Provider node: ${providerError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToLogin()
                    }
            } else {
                // No user is logged in, navigate to LoginActivity
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}