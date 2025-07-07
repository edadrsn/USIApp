package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
       binding=ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val bottomNavigation = binding.bottomNavigation

        // Home sekmesi seçili görünsün çünkü bu PreviewActivity
        bottomNavigation.selectedItemId = R.id.home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Zaten bu sayfadasın, hiçbir şey yapma
                    true
                }

                R.id.profile -> {
                    // Profile sekmesine basınca AcademicianActivity'e geç
                    val intent = Intent(this, AcademicianActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }




    }

    fun goToBack(view: View) {
        val intent = Intent(this@PreviewActivity, AcademicianActivity::class.java)
        startActivity(intent)
    }
}