package com.example.rentease

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

    }
}