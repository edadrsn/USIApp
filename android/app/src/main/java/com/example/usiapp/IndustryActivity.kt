package com.example.usiapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.databinding.ActivityIndustryBinding

class IndustryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityIndustryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}