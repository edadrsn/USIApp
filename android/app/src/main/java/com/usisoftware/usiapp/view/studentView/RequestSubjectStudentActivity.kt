package com.usisoftware.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityRequestSubjectStudentBinding
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

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = currentUser.uid

        // Öğrencinin talebini kaydet
        binding.btnCreateRequest.setOnClickListener {

            db.collection("Students")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    if (document == null || !document.exists()) {
                        Toast.makeText(this, "Öğrenci bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Öğrenci bilgileri
                    val ogrAd = document.getString("studentName") ?: ""
                    val ogrEmail = document.getString("studentEmail") ?: ""
                    val ogrTelefon = document.getString("studentPhone") ?: ""
                    val ogrResim = document.getString("studentImage") ?: ""
                    val ogrUniversite = document.getString("universityName") ?: ""

                    if (ogrUniversite.isEmpty()) {
                        Toast.makeText(this, "Üniversite bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val requestTitle = binding.reqTitle.text.toString().trim()
                    val requestMessage = binding.reqMessage.text.toString().trim()
                    val selectedCategory = intent.getStringExtra("selectedCategory")
                    val currentDate = SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault()
                    ).format(Date())

                    val switchRequestType = binding.switchRequestType.isChecked

                    // Authorities koleksiyonunda üniversite adına göre eşleşme
                    db.collection("Authorities")
                        .whereEqualTo("universityName", ogrUniversite)
                        .get()
                        .addOnSuccessListener { snapshot ->

                            if (snapshot.isEmpty) {
                                Toast.makeText(this, "Üniversite yetkilisi bulunamadı!", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            // Eşleşen Authority dokümanının ID'si
                            val authorityId = snapshot.documents.first().id

                            // Status map → key = authorityId
                            val statusMap = mapOf(authorityId to "pending")

                            val requestData = hashMapOf(
                                "createdDate" to currentDate,
                                "requestMessage" to requestMessage,
                                "requestTitle" to requestTitle,
                                "requesterEmail" to ogrEmail,
                                "requesterID" to userId,
                                "requesterName" to ogrAd,
                                "requesterPhone" to ogrTelefon,
                                "requestCategory" to selectedCategory,
                                "requesterImage" to ogrResim,
                                "status" to statusMap,
                                "requesterType" to "student",
                                "requestType" to switchRequestType
                            )

                            db.collection("Requests")
                                .add(requestData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Talep başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                                    startActivity(
                                        Intent(this, StudentMainActivity::class.java)
                                            .putExtra("goToFragment", "createRequestStudent")
                                    )
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Talep kaydedilemedi!", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Üniversite verisi okunamadı!", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Öğrenci bilgileri okunamadı!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Geri dön
    fun goToPrev(view: View) {
        finish()
    }
}
