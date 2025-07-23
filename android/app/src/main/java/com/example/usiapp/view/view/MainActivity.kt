package com.example.usiapp.view.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    fun clickIndustry(view: View){
        val intent=Intent(this@MainActivity, IndustryActivity::class.java)
        startActivity(intent)
    }

    fun clickAcademician(view:View){
        val intent=Intent(this@MainActivity, AcademicianLoginActivity::class.java)
        startActivity(intent)
    }

}