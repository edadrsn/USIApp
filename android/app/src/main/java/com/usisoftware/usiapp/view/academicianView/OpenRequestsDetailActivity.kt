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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityOpenRequestsDetailBinding
import com.usisoftware.usiapp.view.industryView.IndustryPreviewActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation
import com.usisoftware.usiapp.view.studentView.StudentPreviewActivity

class OpenRequestsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenRequestsDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOpenRequestsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val request = intent.getSerializableExtra("request") as? Request
        val currentUserId = auth.currentUser?.uid

        // Eğer zaten başvurmuşsan mesajı çek ve butonu güncelle
        if (request != null && currentUserId != null) {
            getUserApplyMessage(request.id!!, currentUserId)
        }

        // SwipeRefreshLayout listener zaten var, onu değiştirmene gerek yok
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (request != null && currentUserId != null) {
                getUserApplyMessage(request.id!!, currentUserId)
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        request?.let {
            val getPhoto = it.requesterImage

            if (!getPhoto.isNullOrEmpty()) {
                loadImageWithCorrectRotation(
                    context = this@OpenRequestsDetailActivity,
                    imageUrl = getPhoto,
                    imageView = binding.image,
                    placeholderRes = R.drawable.baseline_block_24
                )
            } else {
                binding.image.setImageResource(R.drawable.baseline_block_24)
            }

            binding.requester.text = it.requesterName
            binding.email.text = "Email: ${it.requesterEmail}"
            binding.phone.text = "Tel: ${it.requesterPhone}"
            binding.requesterName.text = it.requesterName
            binding.requesterEmail.text = it.requesterEmail
            binding.requesterPhone.text = it.requesterPhone
            binding.requestDate.text = it.date
            binding.requestDescription.text = it.message
            binding.title.text = it.title
            binding.requestApplyCount.text = it.applyUserCount.toString()

            val requestsContainer = binding.requestsContainer
            requestsContainer.removeAllViews()

            if (it.requesterType == "academician") {
                binding.requesterType.text = "Akademisyen"
                binding.requesterAddress.text = "Adres bulunamadı"
                addChip(it.requestCategory ?: "", requestsContainer)

            } else if (it.requesterType == "student") {
                binding.requesterType.text = "Öğrenci"
                binding.requesterAddress.text = "Adres bulunamadı"
                addChip(it.requestCategory ?: "", requestsContainer)

            } else {
                binding.requesterType.text = "Sanayi"
                binding.requesterAddress.text = it.requesterAddress

                it.selectedCategories?.forEach { tag ->
                    addChip(tag, requestsContainer)
                }
            }
        }

        binding.goToRequesterPreview.setOnClickListener {
            when (request?.requesterType) {
                "academician" -> {
                    val intent = Intent(this, AcademicianPreviewActivity::class.java)
                    intent.putExtra("USER_ID", request?.requesterId)
                    startActivity(intent)
                }
                "industry" -> {
                    val intent = Intent(this, IndustryPreviewActivity::class.java)
                    intent.putExtra("USER_ID", request?.requesterId)
                    startActivity(intent)
                }
                "student" -> {
                    val intent = Intent(this, StudentPreviewActivity::class.java)
                    intent.putExtra("USER_ID", request?.requesterId)
                    startActivity(intent)
                }
            }
        }

        binding.btnApply.setOnClickListener {
            val message = binding.message.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "Lütfen bir mesaj girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestId = request?.id
            if (requestId.isNullOrEmpty()) {
                Toast.makeText(this, "Geçersiz talep ID'si", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                AlertDialog.Builder(this)
                    .setTitle("Giriş gerekli")
                    .setMessage("Başvuru yapabilmek için önce giriş yapmalısınız.")
                    .setPositiveButton("Giriş Yap") { _, _ ->
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // activity kapanıyor
                    }
                    .setNegativeButton("İptal") { _, _ -> finish() } // opsiyonel
                    .show()
                return@setOnClickListener
            }

            val userId = currentUser.uid

            applyToRequest(requestId, userId, message) { success, error ->
                if (success) {
                    Toast.makeText(this, "Başvuru başarıyla gönderildi", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Hata: ${error ?: "Bilinmeyen hata"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Kategori çipi
    private fun addChip(text: String, container: ViewGroup) {
        val chip = TextView(this).apply {
            this.text = text
            setPadding(20, 10, 20, 10)
            setBackgroundResource(R.drawable.category_chip_bg)
            setTextColor(Color.parseColor("#000000"))
            setTypeface(null, Typeface.BOLD)
            textSize = 11f
            isSingleLine = true
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5, 10, 10, 10)
            }
        }
        container.addView(chip)
    }


    fun applyToRequest(
        requestId: String,
        userId: String,
        message: String,
        onComplete: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        val updateField = "applyUsers.$userId"

        db.collection("Requests")
            .document(requestId)
            .update(updateField, message)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener {
                // Eğer doküman yoksa merge ile oluşturup applyUsers’a ekle
                db.collection("Requests")
                    .document(requestId)
                    .set(mapOf("applyUsers" to mapOf(userId to message)), SetOptions.merge())
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { ex -> onComplete(false, ex.message) }
            }
    }


    // Mesajı çek
    private fun getUserApplyMessage(requestId: String, userId: String) {
        db.collection("Requests")
            .document(requestId)
            .get()
            .addOnSuccessListener { document ->
                val userMessage =
                    (document.get("applyUsers") as? Map<String, String>)?.get(userId)

                if (!userMessage.isNullOrEmpty()) {
                    binding.message.setText(userMessage)
                    binding.message.isEnabled = false
                    binding.btnApply.text = "Başvuruldu"
                    binding.btnApply.isEnabled = false
                }
            }
    }

    fun previousPage(view: View) {
        finish()
    }

}
