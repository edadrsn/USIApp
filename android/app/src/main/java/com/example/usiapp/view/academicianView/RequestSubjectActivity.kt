package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityRequestSubjectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestSubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestSubjectBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email ?: ""

        // Akademisyenin talebini Firebase'e kaydet
        binding.btnCreateRequest.setOnClickListener {
            db.collection("AcademicianInfo")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0] // ilk belgeyi al

                        // Firestore'daki akademisyen verilerini oku
                        val academicianName = document.getString("adSoyad") ?: ""
                        val academicianMail = document.getString("email") ?: ""
                        val academicianPhone = document.getString("personelTel") ?: ""
                        val academicianCorporatePhone = document.getString("kurumsalTel") ?: ""
                        val academicianImage = document.getString("photo") ?: ""

                        // Sayfaya girilen verileri al
                        val requestTitle = binding.requestObjectAcademician.text.toString()
                        val requestMessage = binding.requestMessageAcademician.text.toString()
                        val selectedCategory = intent.getStringExtra("selectedCategory")
                        val currentDate =
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                        // Bilgileri Firestore’a ekle
                        val categoryInfo = hashMapOf(
                            "createdDate" to currentDate,
                            "requestMessage" to requestMessage,
                            "requestTitle" to requestTitle,
                            "requesterEmail" to academicianMail,
                            "requesterID" to (auth.currentUser?.uid ?: ""), // UID de kaydedelim
                            "requesterName" to academicianName,
                            "requesterPhone" to academicianPhone,
                            "requestCategory" to selectedCategory,
                            "requesterImage" to academicianImage,
                            "status" to "pending",
                            "requesterType" to "academician"
                        )

                        db.collection("Requests")
                            .add(categoryInfo)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Talep başarıyla kaydedildi!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, AcademicianMainActivity::class.java)
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
                        Toast.makeText(this, "Akademisyen bilgisi bulunamadı!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    // Geri dön
    fun goToBack(view: View) {
        startActivity(Intent(this@RequestSubjectActivity, RequestCategoryActivity::class.java))
    }
}
