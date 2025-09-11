package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("firstTime", true)

        // Daha önce açıldıysa direkt MainActivity aç
        if (!isFirstTime) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // İlk kez açılıyorsa
        binding.btnStart.setOnClickListener {
            // Bir daha göstermemek için kaydet
            sharedPref.edit().putBoolean("firstTime", false).apply()

            // Ana sayfaya geç
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
