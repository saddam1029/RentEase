package com.example.rentease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.window.SplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
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

        // Ensure Firebase is initialized before using it
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Show splash screen for 2 seconds
        lifecycleScope.launch {
            delay(2000)

            // Check if a user is already logged in
            val currentUser = auth.currentUser

            if (currentUser != null) {
                checkUserType(currentUser.uid)
            } else {
                navigateToLogin()
            }
        }
    }

    private fun checkUserType(userId: String) {
        database.child("Realtor").child(userId).child("userInfo").child("type").get()
            .addOnSuccessListener { providerSnapshot ->
                if (providerSnapshot.exists()) {
                    startActivity(Intent(this@SplashActivity, RealtorActivity::class.java))
                    finish()
                } else {
                    checkClientNode(userId)
                }
            }
            .addOnFailureListener { providerError ->
                handleError("Provider node check failed: ${providerError.message}")
            }
    }

    private fun checkClientNode(userId: String) {
        database.child("Regular User").child(userId).child("userInfo").child("type").get()
            .addOnSuccessListener { clientSnapshot ->
                if (clientSnapshot.exists()) {
                    Toast.makeText(this@SplashActivity, "Welcome Client", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                } else {
                    handleError("Unknown user type. Redirecting to login.")
                }
            }
            .addOnFailureListener { clientError ->
                handleError("Client node check failed: ${clientError.message}")
            }
    }

    private fun handleError(message: String) {
        Toast.makeText(this@SplashActivity, message, Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}