package com.example.usiapp.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import com.example.usiapp.databinding.ActivityAcademicianBinding

class AcademicianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val switchProject = binding.switchProject
        var isProjectSelected = switchProject.isChecked

        fun setSwitchColor(isChecked: Boolean) {
            val color = if (isChecked) "#4EA222" else "#FF0000" // Yeşil veya Kırmızı
            val colorStateList = ColorStateList.valueOf(Color.parseColor(color))
            switchProject.thumbTintList = colorStateList
            switchProject.trackTintList = colorStateList
        }

        setSwitchColor(isProjectSelected)

        switchProject.setOnCheckedChangeListener { _, isChecked ->
            isProjectSelected = isChecked
            setSwitchColor(isChecked)
        }



    }

    fun personalInfo(view: View) {
        val intent = Intent(this, PersonalInfoActivity::class.java)
        startActivity(intent)

    }

    fun contactInfo(view: View) {
        val intent = Intent(this, ContactInfoActivity::class.java)
        startActivity(intent)
    }

    fun goToBack(view: View) {
        val intent = Intent(this, AcademicianLoginActivity::class.java)
        startActivity(intent)
    }
}