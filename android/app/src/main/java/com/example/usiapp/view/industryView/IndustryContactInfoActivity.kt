package com.example.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityIndustryContactInfoBinding
import com.example.usiapp.view.repository.IndustryInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IndustryContactInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryContactInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryContactInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        //Verileri çek
        IndustryInfo(db).getIndustryData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    binding.industryFirmPhone.setText(document.getString("telefon") ?: "")
                    binding.email.setText(document.getString("email") ?: "")
                    binding.firmWebsite.setText(document.getString("firmaWebsite") ?: "")
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })


        //Verileri kaydet
        binding.saveIndustryPhone.setOnClickListener {
            val industryFirmPhone = binding.industryFirmPhone.text.toString()
            val industryEmail = binding.email.text.toString()
            val industryWebsite = binding.firmWebsite.text.toString()

            if (industryFirmPhone.isEmpty() || industryEmail.isEmpty() || industryWebsite.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
            }

            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf(
                    "telefon" to industryFirmPhone,
                    "email" to industryEmail,
                    "firmaWebsite" to industryWebsite
                ),
                onSuccess = {
                    Toast.makeText(this, "İletişim bilgileri kaydedildi! ", Toast.LENGTH_SHORT)
                        .show()
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
        startActivity(Intent(this@IndustryContactInfoActivity, IndustryMainActivity::class.java))
    }

}