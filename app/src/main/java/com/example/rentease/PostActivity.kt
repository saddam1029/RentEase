package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rentease.databinding.ActivityPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class PostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var selectedRooms: String = "" // Variable to store selected room count

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.cvSelectType.setOnClickListener {
            showPropertyTypeDialog()
        }

        binding.cvArea.setOnClickListener {
            showPropertyAreaDialog()
        }

        // Listen for room selection
        binding.rgPaymentMethods.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            selectedRooms = selectedRadioButton.text.toString()
        }

        // Handle Post Button Click
        binding.btPost.setOnClickListener {
            savePostToFirebase()
        }
    }

    private fun showPropertyTypeDialog() {
        val propertyTypes = arrayOf("Apartment", "House", "Villa", "Commercial", "Land")

        AlertDialog.Builder(this)
            .setTitle("Select Property Type")
            .setItems(propertyTypes) { _, which ->
                val selectedType = propertyTypes[which]
                binding.tvType.text = selectedType
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPropertyAreaDialog() {
        val propertyTypes = arrayOf("sqft", "meter")

        AlertDialog.Builder(this)
            .setTitle("Select Property Area")
            .setItems(propertyTypes) { _, which ->
                val selectedType = propertyTypes[which]
                binding.tvAreaa.text = selectedType
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun savePostToFirebase() {
        // Get the current user ID
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect data from input fields
        val propertyType = binding.tvType.text.toString()
        val propertyArea = binding.etArea.text.toString()
        val areaUnit = binding.tvAreaa.text.toString()
        val price = binding.etPrice.text.toString()
        val description = binding.etDescription.text.toString()

        // Validate input fields
        if (propertyType.isEmpty() || propertyArea.isEmpty() || areaUnit.isEmpty() ||
            price.isEmpty() || description.isEmpty() || selectedRooms.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine area and area unit
        val combinedArea = "$propertyArea $areaUnit"  // Example: "500 sqft"

        // Generate a unique post ID
        val postId = UUID.randomUUID().toString()

        // Create a map of the post data
        val postMap = hashMapOf(
            "propertyType" to propertyType,
            "propertyArea" to combinedArea,  // Store combined area
            "price" to price,
            "description" to description,
            "rooms" to selectedRooms // Store selected room count

        )

        // Save the data under Realtor/userId/Post/postId/
        database.child("Realtor").child(userId).child("Post").child(postId).setValue(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHome()
    }

    private fun navigateToHome() {
        val intent = Intent(this, RealtorActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
