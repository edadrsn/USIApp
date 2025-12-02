package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityRequestSubjectBinding
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
        // Giriş yapan kullanıcı uid
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId=currentUser.uid

        // Akademisyenin talebini Firebase'e kaydet
        binding.btnCreateRequest.setOnClickListener {
            db.collection("Academician")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener

                    if (document != null && document.exists()) {
                        // Firestore'daki akademisyen verilerini oku
                        val academicianName = document.getString("adSoyad") ?: ""
                        val academicianMail = document.getString("email") ?: ""
                        val academicianPhone = document.getString("personelTel") ?: ""
                        val academicianCorporatePhone = document.getString("kurumsalTel") ?: ""
                        val academicianImage = document.getString("photo") ?: ""

                        // Sayfaya girilen verileri al
                        val requestTitle = binding.requestObjectAcademician.text.toString()
                        val requestMessage = binding.requestMessageAcademician.text.toString()
                        val selectedCategory = intent.getStringExtra("selectedCategory") ?: ""
                        val currentDate =
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        val switchRequestType = binding.switchRequestType.isChecked

                        val emailDomain = try { academicianMail.substringAfter("@") } catch (e: Exception) { "" }

                        // Authorities koleksiyonundan üniversite adını bul
                        db.collection("Authorities")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (isFinishing || isDestroyed) return@addOnSuccessListener

                                var universityName: String? = null
                                for (doc in snapshot.documents) {
                                    val academicianDomain = doc.getString("academician")
                                    if (academicianDomain == emailDomain) {
                                        universityName = doc.id
                                        break
                                    }
                                }

                                if (universityName != null) {
                                    // Status mapini oluştur
                                    val statusMap = mapOf(universityName to "pending")

                                    // Bilgileri Firestore’a ekle
                                    val categoryInfo = hashMapOf(
                                        "createdDate" to currentDate,
                                        "requestMessage" to requestMessage,
                                        "requestTitle" to requestTitle,
                                        "requesterEmail" to academicianMail,
                                        "requesterID" to userId,
                                        "requesterName" to academicianName,
                                        "requesterPhone" to academicianPhone,
                                        "requestCategory" to selectedCategory,
                                        "requesterImage" to academicianImage,
                                        "status" to statusMap,
                                        "requesterType" to "academician",
                                        "requestType" to switchRequestType
                                    )

                                    db.collection("Requests")
                                        .add(categoryInfo)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Talep başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, AcademicianMainActivity::class.java)
                                            intent.putExtra("goToFragment", "request")
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Hata: ${it.localizedMessage}!", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Authorities koleksiyonu okunamadı!", Toast.LENGTH_SHORT).show()
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
        finish()
    }
}
