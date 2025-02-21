package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rentease.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Check if value 1 is received
        val fromProfile = intent.getIntExtra("fromProfile", 0)
        if (fromProfile == 1) {
            binding.imageView3.setImageResource(R.drawable.post) // Change image
            binding.tvYourPost.text = "Your Posts" // Change text
        }


        // Set up logout button
        binding.cvLogOut.setOnClickListener {
            auth.signOut()

            // Clear login state from SharedPreferences
            val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
            sharedPref.edit().putBoolean("isLoggedIn", false).apply()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
