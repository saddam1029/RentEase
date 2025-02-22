package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        // Retrieve user info from Intent
        val name = intent.getStringExtra("name")
        val type = intent.getStringExtra("type")

        // Display user info
        binding.tvAccountName.text = name
        binding.tvAccountRole.text = type

        binding.ivBack.setOnClickListener {
            // Handle back button based on user type
            if (type == "Realtor") {
                // If the user is a Realtor, go to RealtorActivity
                val intent = Intent(this, RealtorActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                // If the user is a Regular User, go to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        if (fromProfile == 1) {
            binding.imageView3.setImageResource(R.drawable.post) // Change image
            binding.tvYourPost.text = "Your Posts" // Change text
        }

        // Set up logout button
        binding.cvLogOut.setOnClickListener {
            // Show confirmation dialog before logging out
            showLogoutConfirmationDialog()
        }
    }

    // Function to show a confirmation dialog before logout
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            // Proceed with logout
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

            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            // If user selects No, dismiss the dialog
            dialog.dismiss()
        }

        builder.create().show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
