package com.example.usiapp.view.academicianView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityPendingRequestDetailBinding
import com.example.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PendingRequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingRequestDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var appointLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ActivityResultLauncher ile akademisyen atama aktivitesinden dönen sonucu yakala
        appointLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Eğer atama başarılıysa bu aktivitenin sonucu da başarılı olarak ayarla ve kapat
                setResult(RESULT_OK)
                finish()
            }
        }

        // Intent'ten Request objesini al
        val request = intent.getSerializableExtra("request") as? Request
        val requestId = request?.id ?: ""

        request?.let {
            // Firma bilgilerini UI'ya yazdır
            binding.pendingFirmName.text = it.requesterName
            binding.pendingFirmWorkArea.text = it.requesterCategories
            binding.pendingFirmMail.text = it.requesterEmail
            binding.pendingFirmTel.text = it.requesterPhone

            // Talep detaylarını UI'ya yazdır
            binding.detailTitle.text = it.title
            binding.detailMessage.text = it.message
            binding.detailDate.text = it.date

            // Kategorileri dinamik olarak TextView şeklinde ekle
            val categoryContainer = binding.detailCategoryContainer
            categoryContainer.removeAllViews()
            it.selectedCategories.forEach { category ->
                val chip = TextView(this).apply {
                    text = category
                    setPadding(24, 12, 24, 12)
                    setBackgroundResource(R.drawable.category_chip_bg)
                    setTextColor(Color.parseColor("#6f99cb"))
                    setTypeface(null, Typeface.BOLD)
                    textSize = 11f
                    isSingleLine = true
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(10, 10, 10, 10)
                    }
                }
                categoryContainer.addView(chip)
            }
        }

        // Talebi kabul et butonu
        binding.btnAccept.setOnClickListener {
            val adminMessage = binding.adminMessage.text.toString().trim()

            // admin message ve statusu güncelle
            val updates = mapOf(
                "adminMessage" to adminMessage,
                "status" to "approved"
            )

            db.collection("Requests").document(requestId)
                .update(updates)
                .addOnSuccessListener {
                    val intent = Intent(this, AppointAcademicianActivity::class.java)
                    intent.putExtra("requestId", requestId)
                    appointLauncher.launch(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Talebi reddet butonu
        binding.btnReject.setOnClickListener {
            val adminMessage = binding.adminMessage.text.toString().trim()

            // admin message ve statusu güncelle
            val updates = mapOf(
                "adminMessage" to adminMessage,
                "status" to "rejected"
            )

            db.collection("Requests").document(requestId)
                .update(updates)
                .addOnSuccessListener {
                    // Talep başarıyla güncellendikten sonra eski kayıtlara taşı
                    moveOldRequestReject(requestId)

                    // Bu aktivitenin sonucunu başarılı olarak ayarla
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Reddedilen talebi "OldRequests" koleksiyonuna taşıyan fonksiyon
    private fun moveOldRequestReject(requestId: String) {
        val sourceRef = db.collection("Requests").document(requestId) // Kaynak
        val targetRef = db.collection("OldRequests").document(requestId) // Hedef

        // Kaynak belgeden veriyi al
        sourceRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        // Veriyi hedef koleksiyona yaz
                        targetRef.set(data)
                            .addOnSuccessListener {
                                println("Talep eski kayıtlara taşındı")
                            }
                            .addOnFailureListener {
                                println("Taşıma hatası")
                            }
                    }
                } else {
                    Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // PendingRequestsActivity'e geri dön
    fun prevPage(view: View) {
        startActivity(Intent(this, PendingRequestsActivity::class.java))
    }
}


