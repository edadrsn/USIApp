package com.usisoftware.usiapp.view.industryView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityIndustryAddressInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo

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
                    binding.industryFirmAddress.setText(document.getString("adres") ?: "")
                }
            },
            onFailure = { e ->
                Log.e("IndustryAddressInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveIndustryAddress.setOnClickListener {
            val industryFirmAddress = binding.industryFirmAddress.text.toString().trim()

            if (industryFirmAddress.isEmpty()) {
                Toast.makeText(this, "Lütfen adres bilgisini doldurunuz ! ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf("adres" to industryFirmAddress),
                onSuccess = {
                    Toast.makeText(this, "Adres bilgisi kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Log.e("IndustryAddressInfoActivity", "Firestore error:$it.localizedMessage")
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                }
            )
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }

}