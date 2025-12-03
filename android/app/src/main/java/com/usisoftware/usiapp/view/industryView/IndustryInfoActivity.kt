package com.usisoftware.usiapp.view.industryView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityIndustryInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo

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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val uid = currentUser.uid

        //Verileri Çek
        IndustryInfo(db).getIndustryData(
            uid,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getIndustryData

                if (document != null && document.exists()) {
                    binding.industryFirmName.setText(document.getString("firmaAdi") ?: "")
                    binding.industryFirmWorkArea.setText(
                        document.getString("calismaAlanlari") ?: ""
                    )
                }
            },
            onFailure = { e ->
                Log.e("IndustryInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveIndustryInfo.setOnClickListener {
            val industryFirmName = binding.industryFirmName.text.toString().trim()
            val industryFirmWorkArea = binding.industryFirmWorkArea.text.toString().trim()

            if (industryFirmName.isEmpty() || industryFirmWorkArea.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf(
                    "firmaAdi" to industryFirmName,
                    "calismaAlanlari" to industryFirmWorkArea,
                ),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Log.e("IndustryInfoActivity", "Firestore error:$it.localizedMessage")
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                })
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }
}