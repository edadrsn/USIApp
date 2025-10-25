package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivityIndustryWorkerInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IndustryWorkerInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryWorkerInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryWorkerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        //Verileri çek
        IndustryInfo(db).getIndustryData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    binding.employeeName.setText(document.getString("calisanAd") ?: "")
                    binding.employeePosition.setText(document.getString("calisanPozisyon") ?: "")
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveEmployeeInfo.setOnClickListener {
            val employeeName = binding.employeeName.text.toString()
            val employeePosition = binding.employeePosition.text.toString()

            if (employeeName.isEmpty() || employeePosition.isEmpty()) {
                Toast.makeText(
                    this@IndustryWorkerInfoActivity,
                    "Lütfen tüm alanları doldurun !",
                    Toast.LENGTH_SHORT
                ).show()
            }

            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf(
                    "calisanAd" to employeeName,
                    "calisanPozisyon" to employeePosition
                ),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, IndustryMainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT).show()
                })
        }
    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }
}