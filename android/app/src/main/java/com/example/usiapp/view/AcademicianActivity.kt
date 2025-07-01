package com.example.usiapp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityAcademicianBinding

class AcademicianActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAcademicianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityAcademicianBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}