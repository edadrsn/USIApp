package com.usisoftware.usiapp.view.industryView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityIndustryWorkerInfoBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo

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
                    binding.employeeName.setText(document.getString("calisanAd") ?: "")
                    binding.employeePosition.setText(document.getString("calisanPozisyon") ?: "")
                }
            },
            onFailure = { e ->
                Log.e("IndustryWorkerInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveEmployeeInfo.setOnClickListener {
            val employeeName = binding.employeeName.text.toString().trim()
            val employeePosition = binding.employeePosition.text.toString().trim()

            if (employeeName.isEmpty() || employeePosition.isEmpty()) {
                Toast.makeText(this@IndustryWorkerInfoActivity, "Lütfen tüm alanları doldurun !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            IndustryInfo(db).updateIndustryData(
                uid,
                data = hashMapOf(
                    "calisanAd" to employeeName,
                    "calisanPozisyon" to employeePosition
                ),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Log.e("IndustryWorkerInfoActivity", "Firestore error:$it.localizedMessage")
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                })
        }
    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }
}