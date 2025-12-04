package com.usisoftware.usiapp.view.academicianView

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityIncomingRequestDetailBinding
import com.usisoftware.usiapp.view.model.Request

class IncomingRequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingRequestDetailBinding
    private var isStatusButtonVisible = false
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIncomingRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId=currentUser.uid

        // Intent'ten Request objesini al
        val request = intent.getSerializableExtra("request") as? Request ?: return
        if (request.id.isNullOrEmpty()) return


        // documentId bulunduktan sonra Firestore'dan talebin akademisyen cevabını al
        // Akademisyenin dokümanını direkt UID ile al
        db.collection("Academician")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                if (document != null && document.exists()) {
                    documentId = document.id

                    // Eğer request boş değilse talebin cevabını kontrol et
                    if (request != null) {
                        db.collection("Requests")
                            .document(request.id)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (isFinishing || isDestroyed) return@addOnSuccessListener

                                val responses = snapshot.get("academicianResponses") as? Map<String, String> ?: emptyMap()
                                val status = responses[documentId]


                                // Duruma göre UI öğelerini değiştir
                                when (status) {
                                    "approved" -> {
                                        showStatusButton(
                                            "Kabul ettiniz, değiştirmek için tıklayın",
                                            Color.parseColor("#44D145")
                                        )
                                        binding.requestIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
                                        binding.requestState.text = "Onaylandı"
                                        binding.requestState.setTextColor(Color.parseColor("#4BA222"))
                                    }

                                    "rejected" -> {
                                        showStatusButton(
                                            "Reddettiniz, değiştirmek için tıklayın",
                                            Color.parseColor("#E91E1E")
                                        )
                                        binding.requestIcon.setImageResource(R.drawable.baseline_highlight_off_24)
                                        binding.requestState.text = "Reddedildi"
                                        binding.requestState.setTextColor(Color.parseColor("#CC1C1C"))
                                    }

                                    else -> {
                                        binding.requestIcon.setImageResource(R.drawable.baseline_access_time_24)
                                        binding.requestState.text = "Bekliyor"
                                        binding.requestState.setTextColor(Color.parseColor("#F06E1B"))
                                    }
                                }
                            }
                    }
                }
                else {
                    println("Doküman bulunamadı")
                }
            }
            .addOnFailureListener {
                Log.e("LOGIN_FLOW", "Hata")
            }

        // Gelen Request modeline göre ilgili verileri ekrana yazdır
        request?.let {
            //binding.requesterType.text = it.title
            binding.claimant.text = it.requesterName
            binding.requestEmail.text = it.requesterEmail
            binding.requestPhone.text = it.requesterPhone
            binding.requestDate.text = it.date
            binding.requestDescription.text = it.message

            val requestsContainer = binding.requestsContainer
            requestsContainer.removeAllViews()

            if (it.requesterType == "academician") {
                binding.requesterType.text = "Akademisyen"
                binding.requestAddress.text = "Adres bulunamadı"

                val category = it.requestCategory ?: ""

                val chip = TextView(this).apply {
                    text = category
                    setPadding(22, 10, 22, 10)
                    setBackgroundResource(R.drawable.category_chip_bg)
                    setTextColor(Color.parseColor("#000000"))
                    setTypeface(null, Typeface.BOLD)
                    textSize = 11.5f
                    isSingleLine = true
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(10, 10, 10, 10)
                    }
                }
                requestsContainer.addView(chip)


            } else if (it.requesterType == "student") {
                binding.requesterType.text = "Öğrenci"
                binding.requestAddress.text = "Adres bulunamadı"

                val category = it.requestCategory ?: ""

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
                    ).apply {
                        setMargins(10, 10, 10, 10)
                    }
                }
                requestsContainer.addView(chip)
            } else {
                binding.requesterType.text = "Sanayici"
                binding.requestAddress.text = it.requesterAddress
                // Seçilen kategorileri tag şeklinde oluştur ve ekle
                it.selectedCategories?.forEach { tag ->
                    val chip = TextView(this).apply {
                        text = tag
                        setPadding(24, 12, 24, 12)
                        setBackgroundResource(R.drawable.category_chip_bg)
                        setTextColor(Color.parseColor("#000000"))
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
                    requestsContainer.addView(chip)
                }
            }


        }

        // Talebi kabul et butonu
        binding.acceptRequest.setOnClickListener {
            request?.id?.let { requestId ->
                updateAcademicianResponse(requestId, "approved") // Durumu güncelle
                showStatusButton(
                    message = "Kabul ettiniz, değiştirmek için tıklayın",
                    color = Color.parseColor("#44D145")
                )
            }

            binding.requestIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
            binding.requestState.text = "Onaylandı"
            binding.requestState.setTextColor(Color.parseColor("#4BA222"))
        }

        // Talebi reddet butonu
        binding.rejectRequest.setOnClickListener {
            request?.id?.let { requestId ->
                updateAcademicianResponse(requestId, "rejected") // Durumu güncelle
                showStatusButton(
                    message = "Reddettiniz, değiştirmek için tıklayın",
                    color = Color.parseColor("#E91E1E")
                )
            }

            binding.requestIcon.setImageResource(R.drawable.baseline_highlight_off_24)
            binding.requestState.text = "Reddedildi"
            binding.requestState.setTextColor(Color.parseColor("#CC1C1C"))
        }

        // Durum butonuna tıklanınca eski kabul/reddet butonlarını geri getir
        binding.statusButton.setOnClickListener {
            hideStatusButton()
            binding.requestIcon.setImageResource(R.drawable.baseline_access_time_24)
            binding.requestState.text = "Bekliyor"
            binding.requestState.setTextColor(Color.parseColor("#F06E1B"))
        }

    }

    // Durum butonunu göster, kabul/redet butonlarını gizle
    private fun showStatusButton(message: String, color: Int) {
        isStatusButtonVisible = true

        // Kabul ve reddet butonlarını animasyonla gizle
        val buttons = listOf(binding.acceptRequest, binding.rejectRequest)
        buttons.forEach { button ->
            button.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    button.visibility = View.GONE
                }
                .start()
        }

        // Durum butonunu görünür yap ve animasyon uygula
        binding.statusButton.apply {
            text = message
            setBackgroundColor(color)
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f

            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }

    // Durum butonunu gizle, kabul/redet butonlarını geri getir ve Firestore'daki cevabı sıfırla (pending yap)
    private fun hideStatusButton() {
        isStatusButtonVisible = false

        // Intent'ten gelen request ve documentId'yi kontrol et
        val request = intent.getSerializableExtra("request") as? Request

        if (request != null && documentId != null) {
            val update = mapOf("academicianResponses.$documentId" to "pending")
            db.collection("Requests")
                .document(request.id)
                .update(update)
                .addOnSuccessListener {
                    println("Cevabınız sıfırlandı")
                }
                .addOnFailureListener {
                    println("Hata")
                }
        }

        // Durum butonunu küçülterek ve transparan yaparak gizle
        binding.statusButton.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(250)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                binding.statusButton.visibility = View.GONE
            }
            .start()

        // Kabul ve reddet butonlarını animasyonla geri göster
        val buttons = listOf(binding.acceptRequest, binding.rejectRequest)
        buttons.forEach { button ->
            button.apply {
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
        }
    }

    // Firestore'daki akademisyenin cevabını güncelle
    private fun updateAcademicianResponse(requestId: String, status: String) {
        if (documentId == null) return
        val update = mapOf("academicianResponses.$documentId" to status)

        db.collection("Requests")
            .document(requestId)
            .update(update)
            .addOnSuccessListener {
                println("Cevabınız kaydedildi")
            }
            .addOnFailureListener {
                println("Hata")
            }
    }

    // Önceki sayfaya git
    fun previousPage(view: View) {
        finish()
    }
}
