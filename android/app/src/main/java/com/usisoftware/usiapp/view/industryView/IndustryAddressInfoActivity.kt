package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivityIndustryAddressInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IndustryAddressInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryAddressInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryAddressInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        //Verileri Çek
        IndustryInfo(db).getIndustryData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    binding.industryFirmAddress.setText(document.getString("adres") ?: "")
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveIndustryAddress.setOnClickListener {
            val industryFirmAddress = binding.industryFirmAddress.text.toString()

            if (industryFirmAddress.isEmpty()) {
                Toast.makeText(this, "Lütfen adres bilgisini doldurunuz ! ", Toast.LENGTH_SHORT)
                    .show()
            }
            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf("adres" to industryFirmAddress),
                onSuccess = {
                    Toast.makeText(this, "Adres bilgisi kaydedildi", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, IndustryMainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }

}