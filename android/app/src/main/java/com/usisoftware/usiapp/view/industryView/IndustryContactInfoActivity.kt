package com.usisoftware.usiapp.view.industryView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityIndustryContactInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo

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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val uid = currentUser.uid

        //Verileri çek
        IndustryInfo(db).getIndustryData(
            uid,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getIndustryData

                if (document != null && document.exists()) {
                    binding.industryFirmPhone.setText(document.getString("telefon") ?: "")
                    binding.email.setText(document.getString("email") ?: "")
                    binding.firmWebsite.setText(document.getString("firmaWebSite") ?: "")
                }
            },
            onFailure = { e ->
                Log.e("IndustryContactInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })


        //Verileri kaydet
        binding.saveIndustryPhone.setOnClickListener {
            val industryFirmPhone = binding.industryFirmPhone.text.toString().trim()
            val industryEmail = binding.email.text.toString().trim()
            val industryWebsite = binding.firmWebsite.text.toString().trim()

            if (industryFirmPhone.isEmpty() || industryEmail.isEmpty() || industryWebsite.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf(
                    "telefon" to industryFirmPhone,
                    "email" to industryEmail,
                    "firmaWebSite" to industryWebsite
                ),
                onSuccess = {
                    Toast.makeText(this, "İletişim bilgileri kaydedildi! ", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Log.e("IndustryContactInfo", "Firestore error:$it.localizedMessage")
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                })
        }
    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }

}