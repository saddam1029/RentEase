package com.example.rentease

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.databinding.ActivityPostDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var imageAdapter: DetailAdapter
    private val imageList = mutableListOf<Uri>()
    private var postId: String? = null
    private var realtorId: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        postId = intent.getStringExtra("postId")
        realtorId = intent.getStringExtra("realtorId")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val price = intent.getStringExtra("price")
        val propertyArea = intent.getStringExtra("propertyArea")
        val propertyType = intent.getStringExtra("propertyType")
        val rooms = intent.getStringExtra("rooms")

        binding.tvDescription.text = description
        binding.tvLocation.text = location
        binding.tvPrice.text = price
        binding.textView3.text = "Your Dream $propertyType"
        binding.tvArea.text = propertyArea
        binding.tvRoom.text = "$rooms Rooms"

        imageAdapter = DetailAdapter(imageList) { position -> }
        binding.rvPics.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPics.adapter = imageAdapter

        fetchImagesFromFirebase()

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fetchImagesFromFirebase() {
        postId?.let { id ->
            if (realtorId != null) {
                database.reference.child("Realtor").child(realtorId!!).child("Post").child(id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                imageList.clear()
                                val imageUrls = snapshot.child("imageUrls").value as? List<String>
                                imageUrls?.let {
                                    imageList.addAll(it.map { url -> Uri.parse(url) })
                                    imageAdapter.notifyDataSetChanged()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@PostDetailActivity, "Failed to load images", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                database.reference.child("Realtor").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        imageList.clear()
                        for (realtorSnapshot in snapshot.children) {
                            val postSnapshot = realtorSnapshot.child("Post").child(id)
                            if (postSnapshot.exists()) {
                                val imageUrls = postSnapshot.child("imageUrls").value as? List<String>
                                imageUrls?.let {
                                    imageList.addAll(it.map { url -> Uri.parse(url) })
                                    imageAdapter.notifyDataSetChanged()
                                }
                                break
                            }
                        }
                        if (imageList.isEmpty()) {
                            Toast.makeText(this@PostDetailActivity, "No images available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@PostDetailActivity, "Failed to load images", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } ?: run {
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHome()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}