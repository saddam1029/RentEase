package com.example.rentease

data class Post(
    val postId: String = "",
    val propertyType: String = "",
    val propertyArea: String = "",
    val price: String = "",
    val description: String = "",
    val location: String = "",
    val rooms: String = "",
    val userInfo: String = "",
    val imageUrls: List<String>? = null // List of image URLs from Cloudinary
)
