package com.example.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityRequestContentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RequestContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestContentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return


        //Firebase'e oluşturulan talepleri kaydet
        binding.btnCreateRequest.setOnClickListener {
            db.collection("Industry").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        //Firebasedeki firma verilerini al oku
                        val firmaAdi = document.getString("firmaAdi") ?: ""
                        val firmaCalismaAlanlari = document.getString("calismaAlanlari") ?: ""
                        val firmaPhone = document.getString("telefon") ?: ""
                        val email = document.getString("email") ?: ""
                        val address=document.getString("adres") ?: ""
                        val firmImage=document.getString("requesterImage") ?: ""

                        //Sayfaya girilen verileri al
                        val requestTitle = binding.requestObject.text.toString()
                        val requestMessage = binding.requestMessage.text.toString()
                        val selectedCategories = intent.getStringArrayListExtra("selectedCategories") ?: arrayListOf()
                        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                        // Bilgileri Firestore’a ekle
                        val categoryInfo = hashMapOf(
                            "createdDate" to currentDate,
                            "requestMessage" to requestMessage,
                            "requestTitle" to requestTitle,
                            "requesterCategories" to firmaCalismaAlanlari,
                            "requesterEmail" to email,
                            "requesterID" to userId,
                            "requesterName" to firmaAdi,
                            "requesterPhone" to firmaPhone,
                            "requesterAddress" to address,
                            "selectedCategories" to selectedCategories,
                            "status" to "pending",
                            "requesterImage" to firmImage,
                            "requesterType" to "industry"
                        )
                        db.collection("Requests")
                            .add(categoryInfo)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Talep başarıyla kaydedildi!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, IndustryMainActivity::class.java)
                                intent.putExtra("goToFragment", "request")
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Hata: ${it.localizedMessage}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "Firma bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    //CreateRequestActivity'e geri dön
    fun goToCreateRequest(view: View) {
        val intent = Intent(this@RequestContentActivity, CreateRequestActivity::class.java)
        startActivity(intent)
    }

}