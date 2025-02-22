package com.example.rentease

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.rentease.databinding.ActivityPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class PostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var selectedRooms: String = ""
    private lateinit var imageAdapter: ImageAdapter
    private val imageList = mutableListOf<Uri>()
    private val imageUrls = mutableListOf<String>()
    private var postId: String? = null
    private var editCode: Int = 0

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageList.add(it)
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        postId = intent.getStringExtra("postId")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val price = intent.getStringExtra("price")
        val propertyArea = intent.getStringExtra("propertyArea")
        val propertyType = intent.getStringExtra("propertyType")
        val rooms = intent.getStringExtra("rooms")
        editCode = intent.getIntExtra("editCode", 0)

        binding.ivDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.ivDelete.visibility = if (editCode == 0) View.GONE else View.VISIBLE

        // Initialize RecyclerView adapter
        imageAdapter = ImageAdapter(imageList) { position ->
            imageList.removeAt(position)
            imageAdapter.notifyDataSetChanged()
        }

        binding.rvPics.apply {
            layoutManager = LinearLayoutManager(this@PostActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        // Load existing data if editing
        if (editCode == 1) {
            binding.tvType.text = propertyType
            val areaNumber = propertyArea?.filter { it.isDigit() }
            binding.etArea.setText(areaNumber)
            binding.etPrice.setText(price)
            binding.etLocation.setText(location)
            binding.etDescription.setText(description)

            when (rooms) {
                "01" -> binding.rb1.isChecked = true
                "02" -> binding.rb2.isChecked = true
                "03" -> binding.rb3.isChecked = true
                "04" -> binding.rb4.isChecked = true
                "04+" -> binding.rb5.isChecked = true
                else -> binding.rgPaymentMethods.clearCheck()
            }

            // Fetch existing images from Firebase
            postId?.let { id ->
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    database.child("Realtor").child(userId).child("Post").child(id)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val urls = snapshot.child("imageUrls").value as? List<String>
                                urls?.let {
                                    imageUrls.clear()
                                    imageUrls.addAll(it)
                                    imageList.clear()
                                    imageList.addAll(it.map { url -> Uri.parse(url) })
                                    imageAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("PostActivity", "Failed to load images: ${error.message}")
                                Toast.makeText(this@PostActivity, "Failed to load images", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.cvSelectType.setOnClickListener {
            showPropertyTypeDialog()
        }

        binding.cvArea.setOnClickListener {
            showPropertyAreaDialog()
        }

        binding.rgPaymentMethods.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            selectedRooms = selectedRadioButton?.text.toString() ?: ""
        }

        binding.btPost.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editCode == 1 && postId != null) {
                updatePostData(postId!!)
            } else {
                savePostToFirebase()
            }
        }

        binding.cvImage.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun uploadImagesToCloudinary(onComplete: () -> Unit) {
        val newImages = imageList.filter { !imageUrls.contains(it.toString()) } // Only upload new images
        if (newImages.isEmpty()) {
            onComplete()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        var uploadCount = 0

        for (uri in newImages) {
            Log.d("PostActivity", "Attempting upload with preset: rentease_preset")
            MediaManager.get().upload(uri)
                .unsigned("rentease_preset")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("PostActivity", "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as String
                        imageUrls.add(url)
                        uploadCount++
                        Log.d("PostActivity", "Uploaded image URL: $url")
                        if (uploadCount == newImages.size) {
                            binding.progressBar.visibility = View.GONE
                            onComplete()
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("PostActivity", "Upload error: ${error.description}")
                        Toast.makeText(this@PostActivity, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("PostActivity", "Upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()
        }
    }

    private fun savePostToFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btPost.isEnabled = false

        uploadImagesToCloudinary {
            val propertyType = binding.tvType.text.toString()
            val propertyArea = binding.etArea.text.toString()
            val areaUnit = binding.tvAreaa.text.toString()
            val price = binding.etPrice.text.toString()
            val description = binding.etDescription.text.toString()
            val location = binding.etLocation.text.toString()

            if (propertyType.isEmpty() || propertyArea.isEmpty() || areaUnit.isEmpty() ||
                price.isEmpty() || description.isEmpty() || selectedRooms.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                binding.btPost.isEnabled = true
                return@uploadImagesToCloudinary
            }

            val combinedArea = "$propertyArea $areaUnit"
            val postId = UUID.randomUUID().toString()

            val postMap = hashMapOf(
                "propertyType" to propertyType,
                "postId" to postId,
                "propertyArea" to combinedArea,
                "price" to price,
                "description" to description,
                "location" to location,
                "rooms" to selectedRooms,
                "imageUrls" to imageUrls
            )

            database.child("Realtor").child(userId).child("Post").child(postId).setValue(postMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    binding.btPost.isEnabled = true
                }
        }
    }

    private fun updatePostData(postId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btPost.isEnabled = false

        uploadImagesToCloudinary {
            val propertyType = binding.tvType.text.toString()
            val propertyArea = binding.etArea.text.toString()
            val areaUnit = binding.tvAreaa.text.toString()
            val price = binding.etPrice.text.toString()
            val description = binding.etDescription.text.toString()
            val location = binding.etLocation.text.toString()
            val combinedArea = "$propertyArea $areaUnit"

            // Update imageUrls with the current list from imageList
            val updatedImageUrls = mutableListOf<String>()
            imageList.forEach { uri ->
                val uriString = uri.toString()
                if (imageUrls.contains(uriString)) {
                    updatedImageUrls.add(uriString) // Keep existing URLs
                } else if (!updatedImageUrls.contains(uriString)) {
                    updatedImageUrls.add(uriString) // Add new uploaded URLs
                }
            }

            val postMap = hashMapOf(
                "propertyType" to propertyType,
                "propertyArea" to combinedArea,
                "price" to price,
                "description" to description,
                "location" to location,
                "rooms" to selectedRooms,
                "imageUrls" to updatedImageUrls
            )

            database.child("Realtor").child(userId).child("Post").child(postId)
                .updateChildren(postMap as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    binding.btPost.isEnabled = true
                }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { dialog, _ ->
                deletePostFromFirebase()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deletePostFromFirebase() {
        postId?.let { id ->
            val userId = auth.currentUser?.uid
            if (userId != null) {
                database.child("Realtor").child(userId).child("Post").child(id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("PostActivity", "Failed to delete post: ${e.message}")
                        Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun showImagePickerDialog() {
        getImage.launch("image/*")
    }

    private fun showPropertyTypeDialog() {
        val propertyTypes = arrayOf("Apartment", "House", "Villa", "Commercial")
        AlertDialog.Builder(this)
            .setTitle("Select Property Type")
            .setItems(propertyTypes) { _, which ->
                binding.tvType.text = propertyTypes[which]
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPropertyAreaDialog() {
        val propertyTypes = arrayOf("sqft")
        AlertDialog.Builder(this)
            .setTitle("Select Property Area")
            .setItems(propertyTypes) { _, which ->
                binding.tvAreaa.text = propertyTypes[which]
            }
            .setNegativeButton("Cancel", null)
            .show()
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