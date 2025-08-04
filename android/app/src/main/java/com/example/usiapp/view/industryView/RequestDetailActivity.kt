package com.example.usiapp.view.industryView

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityRequestDetailBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val requestId = intent.getStringExtra("requestId") ?: return

        // İlgili talep dökümanı Firestore'dan çekilir
        db.collection("Requests").document(requestId)
            .get()
            .addOnSuccessListener { document ->
                // Döküman varsa ve bulunmuşsa
                if (document != null && document.exists()) {

                    // Firestore'dan talep bilgilerini al
                    val title = document.getString("requestTitle") ?: ""
                    val message = document.getString("requestMessage") ?: ""
                    val date = document.getString("createdDate") ?: ""
                    val categories = document["selectedCategories"] as? List<String> ?: listOf()
                    val status = document.getString("status") ?: ""
                    val adminMessage = document.getString("adminMessage") ?: ""


                    // Alınan firma bilgiler ekranda gösterilir
                    binding.detailTitle.setText(title)
                    binding.detailMessage.setText(message)
                    binding.detailDate.setText("📆 " + date)

                    // Kategoriler için container önce temizlenir
                    val categoryContainer = binding.detailCategoryContainer
                    categoryContainer.removeAllViews()

                    // Her bir kategori için dinamik olarak chip (etiket) oluşturulup eklenir
                    categories.forEach { category ->
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
                            ).apply {
                                setMargins(10, 10, 10, 10)
                            }
                        }
                        categoryContainer.addView(chip)
                    }

                    // Talebin durumuna göre uygun metin ve simgeleri ayarla
                    when (status) {
                        "pending" -> {
                            binding.requestStatus.text = "Beklemede"
                            binding.requestStatus.setTextColor(Color.parseColor("#F06E1B"))
                            binding.requestStatusIcon.setImageResource(R.drawable.baseline_access_time_24)
                        }

                        "approved" -> {
                            binding.requestStatus.text = "Onaylandı"
                            binding.requestStatus.setTextColor(Color.parseColor("#4BA222"))
                            binding.requestStatusIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
                            binding.requestInfo.text = "Mesaj: ${adminMessage}"
                        }

                        "rejected" -> {
                            binding.requestStatus.text = "Reddedildi"
                            binding.requestStatus.setTextColor(Color.parseColor("#CC1C1C"))
                            binding.requestStatusIcon.setImageResource(R.drawable.baseline_highlight_off_24)
                            binding.requestInfo.text = "Nedeni: ${adminMessage}"
                        }
                    }
                } else { // Eğer talep bulunamazsa kullanıcıyı bilgilendir
                    Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                // Veri çekme hatası durumunda kullanıcıya mesaj gösterilir
                Toast.makeText(this, "Veri alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }


    //IndustryMainActivity e geri dön
    fun previousPage(view: View) {
        startActivity(Intent(this, IndustryMainActivity::class.java))
        intent.putExtra("goToFragment", "request")
        finish()
    }
}