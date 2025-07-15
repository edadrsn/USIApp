package com.example.usiapp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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