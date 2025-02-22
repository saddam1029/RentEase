package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.databinding.ActivityRealtorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RealtorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRealtorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val filteredPostList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealtorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView with postList
        postAdapter = PostAdapter(postList) // Use postList directly
        binding.rvProviderHome.layoutManager = LinearLayoutManager(this)
        binding.rvProviderHome.adapter = postAdapter

        loadUserPosts()
        fetchUserInfo()

        binding.etSearch.addTextChangedListener { text ->
            filterPosts(text.toString())
        }

        binding.ivPost.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun filterPosts(query: String) {
        filteredPostList.clear()
        if (query.isEmpty()) {
            filteredPostList.addAll(postList)
        } else {
            postList.forEach {
                if (it.propertyType?.contains(query, ignoreCase = true) == true) {
                    filteredPostList.add(it)
                }
            }
        }
        // Update adapter with filtered list
        postAdapter = PostAdapter(filteredPostList)
        binding.rvProviderHome.adapter = postAdapter
        postAdapter.notifyDataSetChanged()
    }

    private fun fetchUserInfo() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("Realtor").child(userId).child("userInfo")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        val type = snapshot.child("type").getValue(String::class.java)

                        binding.tvRealtorName.text = "Welcome, $name"

                        binding.ivProfile.setOnClickListener {
                            val intent = Intent(this@RealtorActivity, ProfileActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("fromProfile", 1)
                            intent.putExtra("name", name)
                            intent.putExtra("type", type)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(
                            this@RealtorActivity,
                            "User info not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@RealtorActivity,
                        "Failed to fetch user info: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadUserPosts() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("Realtor").child(userId).child("Post")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let { postList.add(it) }
                    }
                    filteredPostList.clear()
                    filteredPostList.addAll(postList) // Initialize filtered list
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@RealtorActivity,
                        "Failed to load posts: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}