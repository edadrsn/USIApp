package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityAcademicianSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class AcademicianSettingsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAcademicianSettingsBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityAcademicianSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        val languages = listOf("Türkçe")
        val themes = listOf("Aydınlık")

        val langAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        val themeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, themes)

        binding.dropdownLanguage.setAdapter(langAdapter)
        binding.dropdownTheme.setAdapter(themeAdapter)

        //SharedPreferences
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val editor = sharedPref.edit()

        //Önceden kaydedilmiş değerleri yükle
        val savedLanguage = sharedPref.getString("appLanguage", "Türkçe")
        val savedTheme = sharedPref.getString("appTheme", "Aydınlık")

        binding.dropdownLanguage.setText(savedLanguage, false)
        binding.dropdownTheme.setText(savedTheme, false)

        //Seçim yapılınca kaydet
        binding.dropdownLanguage.setOnItemClickListener { _, _, position, _ ->
            val selectedLang = languages[position]
            editor.putString("appLanguage", selectedLang)
            editor.apply()
        }

        binding.dropdownTheme.setOnItemClickListener { _, _, position, _ ->
            val selectedTheme = themes[position]
            editor.putString("appTheme", selectedTheme)
            editor.apply()
        }

        binding.logout.setOnClickListener {
            // Ana sayfaya (MainActivity) yönlendir
            startActivity(Intent(this, MainActivity::class.java))
            auth.signOut()

        }
    }

    // Geri dön
    fun back(view: View) {
        finish()
    }
}