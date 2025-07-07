package com.example.usiapp.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicInfoBinding

class AcademicInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityAcademicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        


    }

    fun goToBack(view: View) {
        val intent = Intent(this@AcademicInfoActivity, AcademicianActivity::class.java)
        startActivity(intent)
    }
}
