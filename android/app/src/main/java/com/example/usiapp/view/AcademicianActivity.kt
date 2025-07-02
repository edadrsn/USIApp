package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    fun personalInfo(view: View){
        val intent= Intent(this,PersonalInfoActivity::class.java)
        startActivity(intent)

    }

    fun contactInfo(view:View){
        val intent= Intent(this,ContactInfoActivity::class.java)
        startActivity(intent)
    }

    fun goToBack(view: View){
        val intent= Intent(this,AcademicianLoginActivity::class.java)
        startActivity(intent)
    }
}