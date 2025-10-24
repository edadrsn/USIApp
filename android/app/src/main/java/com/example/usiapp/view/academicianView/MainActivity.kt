package com.example.usiapp.view.academicianView

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityMainBinding
import com.example.usiapp.view.industryView.IndustryActivity
import com.example.usiapp.view.industryView.IndustryMainActivity
import com.example.usiapp.view.studentView.StudentLoginActivity
import com.example.usiapp.view.studentView.StudentMainActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userType = getSharedPreferences("UserData", MODE_PRIVATE).getString("userType", null)

        if (currentUser != null && userType != null) {
            when (userType) {
                "student" -> startActivity(Intent(this@MainActivity, StudentMainActivity::class.java))
                "academician" -> startActivity(Intent(this@MainActivity, AcademicianMainActivity::class.java))
                "industry" -> startActivity(Intent(this@MainActivity, IndustryMainActivity::class.java))
            }
            finish()
        }
    }


    //Sanayi girişi
    fun clickIndustry(view: View) {
        startActivity(Intent(this@MainActivity, IndustryActivity::class.java))
    }


    //Akademisyen girişi
    fun clickAcademician(view: View) {
        startActivity(Intent(this@MainActivity, AcademicianLoginActivity::class.java))
    }


    //Öğrenci Girişi
    fun clickStudent(view: View) {
        startActivity(Intent(this@MainActivity, StudentLoginActivity::class.java))
    }


}
