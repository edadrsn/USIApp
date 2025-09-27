package com.example.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityIndustrySettingsBinding
import com.example.usiapp.view.academicianView.MainActivity
import com.google.firebase.auth.FirebaseAuth

class IndustrySettingsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityIndustrySettingsBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityIndustrySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

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



        binding.logOutIndustry.setOnClickListener {
            startActivity(Intent(this@IndustrySettingsActivity,MainActivity::class.java))
            auth.signOut()
        }

    }

    // Geri dön
    fun back(view: View) {
        finish()
    }
}