package com.example.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var postAdapter: UserAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var auth: FirebaseAuth
    private val filteredPostList = mutableListOf<Post>()
    private val database = FirebaseDatabase.getInstance().reference
    private var selectedPriceLevel: String? = null
    private var selectedSizeLevel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.rvProviderHome.layoutManager = LinearLayoutManager(this)
        postAdapter = UserAdapter(filteredPostList)
        binding.rvProviderHome.adapter = postAdapter

        binding.etSearch.addTextChangedListener { text ->
            filterPosts(text.toString())
        }

        binding.cvPrice.setOnClickListener {
            showPriceFilterDialog()
        }

        binding.cvSize.setOnClickListener {
            showSizeFilterDialog()
        }

        binding.ivProfile.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                fetchUserData(userId)
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }

        fetchPostsFromFirebase()
    }

    private fun fetchUserData(userId: String) {
        val userRef = database.child("Regular User").child(userId).child("userInfo")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                    val type = snapshot.child("type").getValue(String::class.java) ?: "N/A"

                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("type", type)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPostsFromFirebase() {
        database.child("Realtor").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (userSnapshot in snapshot.children) {
                    for (postSnapshot in userSnapshot.child("Post").children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let {
                            postList.add(it)
                        }
                    }
                }
                filteredPostList.clear()
                filteredPostList.addAll(postList)
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterPosts(query: String) {
        filteredPostList.clear()
        val filteredBySearch = if (query.isEmpty()) {
            postList
        } else {
            postList.filter {
                it.propertyType?.contains(query, ignoreCase = true) == true
            }
        }

        filteredPostList.addAll(filteredBySearch)

        if (!selectedPriceLevel.isNullOrEmpty()) {
            filterPostsByPrice(selectedPriceLevel!!)
        }

        if (!selectedSizeLevel.isNullOrEmpty()) {
            filterPostsBySize(selectedSizeLevel!!)
        }

        postAdapter.notifyDataSetChanged()
    }

    private fun filterPostsByPrice(priceLevel: String) {
        val currentList = ArrayList(filteredPostList)
        filteredPostList.clear()
        currentList.forEach {
            val price = it.price?.toIntOrNull() ?: return@forEach
            when (priceLevel) {
                "Price < 5000" -> if (price < 5000) filteredPostList.add(it)
                "Price  5000 to 12000" -> if (price in 5000..12000) filteredPostList.add(it)
                "Price > 12000" -> if (price > 12000) filteredPostList.add(it)
            }
        }
    }

    private fun filterPostsBySize(sizeLevel: String) {
        val currentList = ArrayList(filteredPostList)
        filteredPostList.clear()
        currentList.forEach {
            val sizeString = it.propertyArea?.replace("[^0-9]".toRegex(), "")
            val size = sizeString?.toIntOrNull() ?: return@forEach
            when (sizeLevel) {
                "Size < 100sqft" -> if (size < 100) filteredPostList.add(it)
                "Size 100sqft to 500sqft" -> if (size in 100..500) filteredPostList.add(it)
                "Size > 500sqft" -> if (size > 500) filteredPostList.add(it)
            }
        }
    }

    private fun showPriceFilterDialog() {
        val priceOptions = arrayOf("Price < 5000", "Price  5000 to 12000", "Price > 12000")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Price Range")
            .setItems(priceOptions) { _, which ->
                selectedPriceLevel = priceOptions[which]
                filterPosts(binding.etSearch.text.toString())
            }
            .create()
            .show()
    }

    private fun showSizeFilterDialog() {
        val sizeOptions = arrayOf("Size < 100sqft", "Size 100sqft to 500sqft", "Size > 500sqft")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Property Size")
            .setItems(sizeOptions) { _, which ->
                selectedSizeLevel = sizeOptions[which]
                filterPosts(binding.etSearch.text.toString())
            }
            .create()
            .show()
    }
}