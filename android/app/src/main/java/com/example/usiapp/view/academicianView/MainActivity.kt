package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityMainBinding
import com.example.usiapp.view.industryView.IndustryActivity
import com.example.usiapp.view.studentView.StudentLoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    //Sanayi girişi
    fun clickIndustry(view: View) {
        startActivity( Intent(this@MainActivity, IndustryActivity::class.java))
    }

    //Akademisyen girişi
    fun clickAcademician(view: View) {
        startActivity(Intent(this@MainActivity, AcademicianLoginActivity::class.java))
    }

    //Öğrenci Girişi
    fun clickStudent(view:View){
        startActivity(Intent(this@MainActivity, StudentLoginActivity::class.java))
    }
}
