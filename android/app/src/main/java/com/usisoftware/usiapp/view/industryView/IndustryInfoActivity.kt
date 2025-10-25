package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivityIndustryInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IndustryInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        //Verileri Çek
        IndustryInfo(db).getIndustryData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    binding.industryFirmName.setText(document.getString("firmaAdi") ?: "")
                    binding.industryFirmWorkArea.setText(
                        document.getString("calismaAlanlari") ?: ""
                    )
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveIndustryInfo.setOnClickListener {
            val industryFirmName = binding.industryFirmName.text.toString()
            val industryFirmWorkArea = binding.industryFirmWorkArea.text.toString()

            if (industryFirmName.isEmpty() || industryFirmWorkArea.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
            }

            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf(
                    "firmaAdi" to industryFirmName,
                    "calismaAlanlari" to industryFirmWorkArea,
                ),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, IndustryMainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                })
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }
}