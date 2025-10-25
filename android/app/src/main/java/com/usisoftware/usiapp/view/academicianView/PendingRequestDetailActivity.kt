package com.usisoftware.usiapp.view.academicianView

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
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityPendingRequestDetailBinding
import com.usisoftware.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

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
        val categoryContainer = binding.detailCategoryContainer

        // ActivityResultLauncher ile akademisyen atama aktivitesinden dönen sonucu yakala
        appointLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }

        val request = intent.getSerializableExtra("request") as? Request
        val requestId = request?.id ?: ""

        request?.let {
            binding.pendingFirmName.text = it.requesterName
            binding.pendingFirmMail.text = "📧 ${it.requesterEmail}"
            binding.pendingFirmTel.text = "📞 ${it.requesterPhone}"

            // Requester tipini göster
            binding.requesterTypeText.text = when (it.requesterType) {
                "student" -> "Öğrenci"
                "academician" -> "Akademisyen"
                "industry" -> "Sanayici"
                else -> "-"
            }

            // Kategorileri göster
            categoryContainer.removeAllViews()
            val categoriesToShow = when (it.requesterType) {
                "student", "academician" -> listOf(it.requestCategory ?: "")
                "industry" -> it.selectedCategories
                else -> emptyList()
            }

            categoriesToShow.forEach { category ->
                val chip = TextView(this).apply {
                    text = category
                    setPadding(22, 10, 22, 10)
                    setBackgroundResource(R.drawable.category_chip_bg)
                    setTextColor(Color.parseColor("#000000"))
                    setTypeface(null, Typeface.BOLD)
                    textSize = 11f
                    isSingleLine = true
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(7, 7, 10, 10) }
                }
                categoryContainer.addView(chip)
            }

            // Talep detaylarını göster
            binding.detailTitle.text = it.title
            binding.detailMessage.text = it.message
            binding.detailDate.text = it.date

            // Talep edenin resmini yükle
            Picasso.get()
                .load(it.requesterImage)
                .placeholder(R.drawable.baseline_block_24)
                .error(R.drawable.baseline_block_24)
                .into(binding.firmImage)
        }

        // Talebi kabul et butonu
        binding.btnAccept.setOnClickListener {
            val adminMessage = binding.adminMessage.text.toString().trim()
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
            val updates = mapOf(
                "adminMessage" to adminMessage,
                "status" to "rejected"
            )

            db.collection("Requests").document(requestId)
                .update(updates)
                .addOnSuccessListener {
                    moveOldRequestReject(requestId)
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun moveOldRequestReject(requestId: String) {
        val sourceRef = db.collection("Requests").document(requestId)
        val targetRef = db.collection("OldRequests").document(requestId)

        sourceRef.get()
            .addOnSuccessListener { document ->
                document.data?.let { data ->
                    targetRef.set(data)
                        .addOnSuccessListener { println("Talep eski kayıtlara taşındı") }
                        .addOnFailureListener { println("Taşıma hatası") }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //Geri dön
    fun prevPage(view: View) {
        finish()
    }
}



