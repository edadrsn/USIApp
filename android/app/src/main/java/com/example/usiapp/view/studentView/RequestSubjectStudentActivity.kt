package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityRequestSubjectStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestSubjectStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestSubjectStudentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestSubjectStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: ""

        // Switch’in durumunu al
        val switchRequestType = binding.switchRequestType.isChecked

        //Öğrencinin talebini Firebase'e  kaydet
        binding.btnCreateRequest.setOnClickListener {
            db.collection("Students").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        //Firebasedeki öğrenci verilerini al oku
                        val ogrAd = document.getString("studentName") ?: ""
                        val ogrEmail = document.getString("studentEmail") ?: ""
                        val ogrTelefon = document.getString("studentPhone") ?: ""
                        val ogrResim = document.getString("studentImage") ?: ""

                        //Sayfaya girilen verileri al
                        val requestTitle = binding.reqTitle.text.toString()
                        val requestMessage = binding.reqMessage.text.toString()
                        val selectedCategory = intent.getStringExtra("selectedCategory")
                        val currentDate =
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                                Date()
                            )

                        // Bilgileri Firestore’a ekle
                        val categoryInfo = hashMapOf(
                            "createdDate" to currentDate,
                            "requestMessage" to requestMessage,
                            "requestTitle" to requestTitle,
                            "requesterEmail" to ogrEmail,
                            "requesterID" to document.id,
                            "requesterName" to ogrAd,
                            "requesterPhone" to ogrTelefon,
                            "requestCategory" to selectedCategory,
                            "requesterImage" to ogrResim,
                            "status" to "pending",
                            "requesterType" to "student",
                            "requestType" to switchRequestType
                        )
                        db.collection("Requests")
                            .add(categoryInfo)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Talep başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, StudentMainActivity::class.java)
                                intent.putExtra("goToFragment", "createRequestStudent")
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Hata: ${it.localizedMessage}!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Öğrenci bilgisi bulunamadı!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

    }

    //Önceki sayfaya git
    fun goToPrev(view: View) {
        finish()
    }
}