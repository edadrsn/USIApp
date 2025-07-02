package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityContactInfoBinding

class ContactInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityContactInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    fun goToBack(view: View){
        val intent= Intent(this,AcademicianActivity::class.java)
        startActivity(intent)
    }
}