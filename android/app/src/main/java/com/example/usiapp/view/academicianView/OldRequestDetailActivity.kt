package com.example.usiapp.view.academicianView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityOldRequestDetailBinding
import com.example.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class OldRequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOldRequestDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOldRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Gelen request modelini al
        val request = intent.getSerializableExtra("request") as? Request

        // Layout referansları
        val detailContainer = binding.detailContainer
        val categoryContainer = binding.categoryContainer

        request?.let {
            // Firma Bilgileri
            binding.oldFirmName.text = it.requesterName
            binding.oldFirmMail.text = "📧 " + it.requesterEmail
            binding.oldFirmPhone.text = "📞 " + it.requesterPhone

            // Requester tipini göster
            binding.requesterTypeOldReq.text = when (it.requesterType) {
                "student" -> "Öğrenci"
                "academician" -> "Akademisyen"
                "industry" -> "Sanayici"
                else -> "-"
            }

            // Kategoriler
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
                    setTextColor(Color.parseColor("#6f99cb"))
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

            // Talep Bilgileri
            binding.requestTitle.text = it.title
            binding.requestMessage.text = it.message
            binding.requestDate.text = it.date

            // Resmi Picasso ile yükle
            Picasso.get()
                .load(it.requesterImage)
                .placeholder(R.drawable.baseline_block_24)
                .error(R.drawable.baseline_block_24)
                .into(binding.firmImage)

            // Akademisyen kartlarını çek
            loadAcademicianCards(it.id)
        }
    }

    private fun loadAcademicianCards(requestId: String) {
        db.collection("OldRequests").document(requestId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val selectedAcademicianIds =
                        document["selectedAcademiciansId"] as? List<String> ?: emptyList()
                    val responses =
                        document["academicianResponses"] as? Map<String, String> ?: emptyMap()

                    selectedAcademicianIds.forEach { uid ->
                        db.collection("AcademicianInfo").document(uid)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc != null && userDoc.exists()) {
                                    val name = userDoc.getString("adSoyad") ?: ""
                                    val degree = userDoc.getString("unvan") ?: ""
                                    val profileUrl = userDoc.getString("photo") ?: ""
                                    val tags = userDoc.get("uzmanlikAlanlari") as? List<String>
                                        ?: emptyList()
                                    val status = responses[uid] ?: "pending"

                                    val view = layoutInflater.inflate(
                                        R.layout.item_selected_academician, null
                                    )
                                    val params = ViewGroup.MarginLayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(10, 15, 10, 15)
                                    }
                                    view.layoutParams = params

                                    val academicianImg = view.findViewById<ImageView>(R.id.academicianImg)
                                    val academicianName = view.findViewById<TextView>(R.id.selectedName)
                                    val academicianDegree = view.findViewById<TextView>(R.id.selectedDegree)
                                    val academicianContainer = view.findViewById<LinearLayout>(R.id.academicianContainer)
                                    val statusImg = view.findViewById<ImageView>(R.id.statusImg)
                                    val statusMessage = view.findViewById<TextView>(R.id.statusMessage)

                                    if (profileUrl.isNotEmpty()) {
                                        Picasso.get()
                                            .load(profileUrl)
                                            .placeholder(R.drawable.person)
                                            .into(academicianImg)
                                    }

                                    academicianName.text = name
                                    academicianDegree.text = degree

                                    // Durum göstergesi
                                    when (status) {
                                        "approved" -> {
                                            statusImg.setImageResource(R.drawable.baseline_check_circle_24)
                                            statusMessage.text = "Onaylandı"
                                            statusMessage.setTextColor(Color.parseColor("#4BA222"))
                                        }

                                        "pending" -> {
                                            statusImg.setImageResource(R.drawable.baseline_access_time_24)
                                            statusMessage.text = "Bekliyor"
                                            statusMessage.setTextColor(Color.parseColor("#F06E1B"))
                                        }

                                        "rejected" -> {
                                            statusImg.setImageResource(R.drawable.baseline_highlight_off_24)
                                            statusMessage.text = "Reddedildi"
                                            statusMessage.setTextColor(Color.parseColor("#CC1C1C"))
                                        }
                                    }

                                    // Uzmanlık alanlarını ekle
                                    academicianContainer.removeAllViews()
                                    tags.forEach { tag ->
                                        val chip = TextView(this).apply {
                                            text = tag
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
                                        academicianContainer.addView(chip)
                                    }

                                    // Kart tıklanınca profil önizleme
                                    view.setOnClickListener {
                                        val intent = Intent(this, AcademicianPreviewActivity::class.java)
                                        intent.putExtra("source", "oldRequest")
                                        intent.putExtra("academicianId", uid)
                                        startActivity(intent)
                                    }

                                    binding.academicianCardContainer.addView(view)
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Akademisyen bilgisi alınamadı", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veri alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    // Geri Dön
    fun previousPage(view: View) {
        startActivity(Intent(this@OldRequestDetailActivity, OldRequestsActivity::class.java))
    }
}

