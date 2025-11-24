package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Yeniden veri çekme
            val request = intent.getSerializableExtra("request") as? Request
            val authUid = auth.currentUser?.uid
            val userEmail = auth.currentUser?.email ?: ""

            if (authUid != null && request?.id != null) {
                if (isAcademicianEmail(userEmail)) {
                    getAcademicianDocumentId(userEmail) { docId ->
                        val realUserId = docId ?: authUid
                        getUserApplyMessage(request.id!!, realUserId)
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                } else {
                    getUserApplyMessage(request.id!!, authUid)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Gelen Request modeline göre ilgili verileri ekrana yazdır
        request?.let {
            val getPhoto = it.requesterImage

            if (!getPhoto.isNullOrEmpty()) {
                // loadImageWithCorrectRotation fonksiyonunu çağırıyoruz
                loadImageWithCorrectRotation(
                    context = this@OpenRequestsDetailActivity,
                    imageUrl = getPhoto,
                    imageView =  binding.image,
                    placeholderRes = R.drawable.baseline_block_24
                )
            } else {
                // Eğer URL boş veya null ise varsayılan resmi göster
                binding.image.setImageResource(R.drawable.baseline_block_24)
            }


            binding.requester.text = it.requesterName
            binding.email.text = "Email:" + it.requesterEmail
            binding.phone.text = "Tel:" + it.requesterPhone
            binding.requesterName.text = it.requesterName
            binding.requesterEmail.text = it.requesterEmail
            binding.requesterPhone.text = it.requesterPhone
            binding.requestDate.text = it.date
            binding.requestDescription.text = it.message
            binding.title.text = it.title
            binding.requestApplyCount.text=it.applyUserCount.toString()

            val requestsContainer = binding.requestsContainer
            requestsContainer.removeAllViews()


            if (it.requesterType == "academician") {
                binding.requesterType.text = "Akademisyen"
                binding.requesterAddress.text = "Adres bulunamadı"
                val category = it.requestCategory ?: ""

                val chip = TextView(this).apply {
                    text = category
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
                requestsContainer.addView(chip)

            } else if (it.requesterType == "student") {
                binding.requesterType.text = "Öğrenci"
                binding.requesterAddress.text = "Adres bulunamadı"

                val category = it.requestCategory ?: ""

                val chip = TextView(this).apply {
                    text = category
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
                requestsContainer.addView(chip)

            } else {
                binding.requesterType.text = "Sanayi"
                binding.requesterAddress.text = it.requesterAddress

                // Seçilen kategorileri tag şeklinde oluştur ve ekle
                it.selectedCategories?.forEach { tag ->
                    val chip = TextView(this).apply {
                        text = tag
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
                    requestsContainer.addView(chip)

                }

            }
        }


        //Resme tıklayıp önizleme sayfasına git
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

        //Başvur butonuna tıklama
        binding.btnApply.setOnClickListener {
            //Mesajı
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
                    }
                    .setNegativeButton("İptal", null)
                    .show()
                return@setOnClickListener
            }

            val authUid = currentUser.uid
            val userEmail = currentUser.email ?: ""

            applyToRequest(requestId, authUid, userEmail, message) { success, error ->
                if (success) {
                    Toast.makeText(this, "Başvuru başarıyla gönderildi", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Hata: ${error ?: "Bilinmeyen hata"}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }

    //Akademisyen mi
    fun isAcademicianEmail(email: String = auth.currentUser?.email ?: ""): Boolean {
        val parts = email.split("@")  //e-postayı @ karakterine göre iki parçaya ayır
        if (parts.size < 2) return false   //parts.size < 2 ise, e-postada “@” işareti yok yani geçerli bir mail deil
        val domain = parts.last().lowercase()  //“@” işaretinden sonraki kısmı al
        return domain == "ahievran.edu.tr"  //eğer domain tam ve doğruysa true döner
    }


    //Akademisyen doküman id yi bul
    fun getAcademicianDocumentId(
        userEmail: String = auth.currentUser?.email ?: "",  //giriş yapan kullanıcının mailini al
        onResult: (documentId: String?) -> Unit
    ) {          //Fonksiyonun içinde işlemi bitirdikten sonra, sonucu dışarı “geri göndermek” için kullanırız

        //email boşsa geriye null dönsün
        if (userEmail.isBlank()) {
            onResult(null)
            return
        }

        db.collection("AcademicianInfo")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {  //doküman boş değilse
                    onResult(documents.documents[0].id)  // İlk bulunan dokümanın id'sini döndür
                } else {
                    onResult(null)  //doküman boşsa onResult metodunu çağır geriye null dönsün
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "getAcademicianDocumentId error: ${e.message}")
                onResult(null)
            }
    }

    //Başvuruyu ekle
    fun applyToRequest(
        requestId: String,
        authUid: String,
        userEmail: String = auth.currentUser?.email ?: "",
        message: String,
        onComplete: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        if (message.isBlank()) {
            onComplete(false, "Mesaj boş olamaz")
            return
        }

        // Helper: gerçek userId'yi belirle ve sonra apply işlemini yap
        fun doApply(userId: String) {
            val updateField = "applyUsers.$userId"
            // Önce update dene (doküman varsa bu atomik olarak ekler/günceller)
            db.collection("Requests")
                .document(requestId)
                .update(updateField, message)
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { updateEx ->
                    // Eğer belge yoksa veya update başarısız olduysa, set ile tamamen oluştur (merge ile)
                    db.collection("Requests")
                        .document(requestId)
                        .set(mapOf("applyUsers" to mapOf(userId to message)), SetOptions.merge())
                        .addOnSuccessListener {
                            onComplete(true, null)
                        }
                        .addOnFailureListener { setEx ->
                            Log.e("FirestoreDebug", "applyToRequest set error: ${setEx.message}")
                            onComplete(false, setEx.message)
                        }
                }
        }

        // Eğer user akademisyen domain'ine sahipse, akademisyen doküman id'sini al
        if (isAcademicianEmail(userEmail)) {
            getAcademicianDocumentId(userEmail) { docId ->
                val realUserId = docId ?: authUid // eğer docId yoksa fallback authUid
                doApply(realUserId)
            }
        } else {
            // Öğrenci / sanayi kullanıcıları için authUid kullan
            doApply(authUid)
        }
    }

    //Başvurduysam mesajı çek
    private fun getUserApplyMessage(requestId: String, userId: String) {
        db.collection("Requests")
            .document(requestId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val applyUsers = document.get("applyUsers") as? Map<String, String>
                    val userMessage = applyUsers?.get(userId)

                    if (!userMessage.isNullOrEmpty()) {
                        binding.message.setText(userMessage)
                        binding.message.isEnabled = false
                        binding.btnApply.text = "Başvuruldu"
                        binding.btnApply.isEnabled = false
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "getUserApplyMessage error: ${e.message}")
            }
    }


    // Önceki sayfaya git
    fun previousPage(view: View) {
        finish()
    }
}
