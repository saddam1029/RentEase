package com.example.rentease

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rentease.databinding.ActivityRealtorBinding
import com.example.rentease.databinding.ActivitySignUpBinding

class RealtorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRealtorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealtorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("fromProfile", 1)
            startActivity(intent)
        }


    }
}