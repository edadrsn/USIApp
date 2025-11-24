package com.usisoftware.usiapp.view.industryView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityIndustryPreviewBinding
import com.usisoftware.usiapp.view.repository.IndustryInfo
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class IndustryPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryPreviewBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userId = intent.getStringExtra("USER_ID")
        val uidToFetch = if (!userId.isNullOrEmpty()) userId else auth.currentUser?.uid

        if (uidToFetch.isNullOrEmpty()) {
            Toast.makeText(this, "Kullanıcı bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //Verileri çek
        val industryInfo= IndustryInfo(db)
        industryInfo.getIndustryData(
            uidToFetch,
            onSuccess = { document ->
                if (document.exists()) {
                    val getPhoto = document.getString("requesterImage")
                    if (!getPhoto.isNullOrEmpty()) {
                        loadImageWithCorrectRotation(
                            this@IndustryPreviewActivity,
                            getPhoto,
                            binding.firmImage,
                            R.drawable.person)
                    } else {
                        binding.firmImage.setImageResource(R.drawable.person)
                    }

                    binding.firmName.setText(document.getString("firmaAdi") ?: "")
                    binding.firmEmail.setText(document.getString("email") ?: "")
                    binding.firmName2.setText(document.getString("firmaAdi") ?: "")
                    binding.workArea.setText(document.getString("calismaAlanlari") ?: "")
                    binding.mail.setText(document.getString("email") ?: "")
                    binding.phone.setText(document.getString("telefon") ?: "")
                    binding.website.setText(document.getString("firmaWebsite") ?: "")
                    binding.address.setText(document.getString("adres") ?: "")
                    binding.employeeName.setText(document.getString("calisanAd") ?: "")
                    binding.employeePosition.setText(document.getString("calisanPozisyon") ?: "")
                }
                else{
                    Toast.makeText(this, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

    }

    fun goToBack(view: View) {
        finish()
    }
}