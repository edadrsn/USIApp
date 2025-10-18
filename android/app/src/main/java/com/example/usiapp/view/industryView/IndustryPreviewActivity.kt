package com.example.usiapp.view.industryView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityIndustryPreviewBinding
import com.example.usiapp.view.repository.IndustryInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

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
                        Picasso.get()
                            .load(getPhoto)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(binding.firmImage)
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