package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityOldRequestDetailBinding
import com.usisoftware.usiapp.view.industryView.IndustryPreviewActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation
import com.usisoftware.usiapp.view.studentView.StudentPreviewActivity

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
        val categoryContainer = binding.categoryContainer

        request?.let {

            // TALEP SAHİBİ
            val imageUrl = it.requesterImage
            if (!imageUrl.isNullOrEmpty()) {
                loadImageWithCorrectRotation(
                    context = this@OldRequestDetailActivity,
                    imageUrl = imageUrl,
                    imageView = binding.image,
                    placeholderRes = R.drawable.baseline_block_24
                )
            } else {
                binding.image.setImageResource(R.drawable.baseline_block_24)
            }

            binding.oldRequesterName.text = it.requesterName// Requester tipini göster
            binding.oldRequesterType.text = when (it.requesterType) {
                "student" -> "Öğrenci"
                "academician" -> "Akademisyen"
                "industry" -> "Sanayici"
                else -> "-"
            }
            binding.oldRequesterEmail.text = "Email: ${it.requesterEmail}"
            binding.oldRequesterTel.text = "Tel: ${it.requesterPhone}"


            //TALEP BİLGİLERİ
            binding.requesterDetailType.text = when (it.requesterType) {
                "student" -> "Öğrenci"
                "academician" -> "Akademisyen"
                "industry" -> "Sanayici"
                else -> "-"
            }
            binding.requesterEmail.text = "${it.requesterEmail}"
            binding.requesterPhone.text = "${it.requesterPhone}"
            if (it.requesterType == "academician") {
                binding.requesterAddress.text = "Adres bulunamadı"

            } else if (it.requesterType == "student") {
                binding.requesterAddress.text = "Adres bulunamadı"
            } else {
                binding.requesterAddress.text = it.requesterAddress
            }
            binding.requestDate.text = it.date
            binding.requestDetailTitle.text = it.title
            binding.requestDetailDescription.text = it.message

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

            //Açık talepse başvuranları göster
            if (it.requestType == true) {
                binding.cardContainerTitle.text = "Başvuranlar"
                if (it.id.isNullOrEmpty()) {
                    Log.e("OldRequestDetail", "Request id NULL geldi!")
                    return@let
                }
                loadUsersCard(it.id!!)

            }
            //Kapalı talepse atanan akademisyenleri göster
            else {
                binding.cardContainerTitle.text = "Atanan Akademisyenler"
                // Akademisyen kartlarını çek
                loadAcademicianCards(it.id)
            }

            //Önizleme sayfasına git
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

        }
    }

    private fun loadUsersCard(requestId: String) {
        db.collection("Requests").document(requestId).get()
            .addOnSuccessListener { document ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                if (document != null && document.exists()) {
                    val applyUsers = document["applyUsers"] as? Map<String, String> ?: emptyMap()

                    if (applyUsers.isNotEmpty()) {
                        applyUsers.forEach { (userId, messageText) ->
                            // Students koleksiyonunda ara
                            db.collection("Students").document(userId)
                                .get()
                                .addOnSuccessListener { studentDoc ->
                                    if (studentDoc.exists()) {
                                        addUserCard(studentDoc, messageText, "student")
                                    } else {
                                        // AcademicianInfo koleksiyonunda ara
                                        db.collection("Academician").document(userId)
                                            .get()
                                            .addOnSuccessListener { academicianDoc ->
                                                if (academicianDoc.exists()) {
                                                    addUserCard(
                                                        academicianDoc,
                                                        messageText,
                                                        "academician"
                                                    )
                                                } else {
                                                    // IndustryInfo koleksiyonunda ara
                                                    db.collection("Industry").document(userId)
                                                        .get()
                                                        .addOnSuccessListener { industryDoc ->
                                                            if (industryDoc.exists()) {
                                                                addUserCard(
                                                                    industryDoc,
                                                                    messageText,
                                                                    "industry"
                                                                )
                                                            } else {
                                                                Log.e(
                                                                    "RequestDetail",
                                                                    "Kullanıcı bulunamadı: $userId"
                                                                )
                                                            }
                                                        }
                                                }
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "RequestDetail",
                                        "Kullanıcı bilgisi alınamadı: ${it.message}"
                                    )
                                }
                        }
                    }
                } else {
                    Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("OldRequestDetail", "Requests verileri alınamadı:${e.message}")
                Toast.makeText(this, "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }

    }

    private fun addUserCard(userDoc: DocumentSnapshot, messageText: String, userType: String) {
        val (name, typeText, profileUrl) = when (userType) {
            "student" -> Triple(
                userDoc.getString("studentName") ?: "Bilinmiyor",
                "Öğrenci",
                userDoc.getString("studentImage") ?: ""
            )

            "academician" -> Triple(
                userDoc.getString("adSoyad") ?: "Bilinmiyor",
                "Akademisyen",
                userDoc.getString("photo") ?: ""
            )

            "industry" -> Triple(
                userDoc.getString("firmaAdi") ?: "Bilinmiyor",
                "Sanayici",
                userDoc.getString("requesterImage")  ?: ""
            )

            else -> Triple("Bilinmiyor", "-", "")
        }

        val view = layoutInflater.inflate(R.layout.item_apply_users, null)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(10, 15, 10, 15)
        }
        view.layoutParams = params

        val applyImage = view.findViewById<ImageView>(R.id.applyImage)
        val applyName = view.findViewById<TextView>(R.id.applyName)
        val applyType = view.findViewById<TextView>(R.id.applyType)
        val applyMessage = view.findViewById<TextView>(R.id.applyMessage)

        if (!profileUrl.isNullOrEmpty()) {
            try {
                loadImageWithCorrectRotation(
                    applyImage.context,
                    profileUrl,
                    applyImage,
                    R.drawable.person
                )
            } catch (e: Exception) {
                applyImage.setImageResource(R.drawable.person)
            }
        } else {
            applyImage.setImageResource(R.drawable.person)
        }



        applyName.text = name
        applyType.text = typeText
        applyMessage.text = messageText

        //Kart tıklama olayı
        view.setOnClickListener {
            val context = view.context
            when (userType) {
                "student" -> {
                    val intent = Intent(context, StudentPreviewActivity::class.java)
                    intent.putExtra("USER_ID", userDoc.id)
                    context.startActivity(intent)
                }

                "academician" -> {
                    val intent = Intent(context, AcademicianPreviewActivity::class.java)
                    intent.putExtra("USER_ID", userDoc.id)
                    context.startActivity(intent)
                }

                "industry" -> {
                    val intent = Intent(context, IndustryPreviewActivity::class.java)
                    intent.putExtra("USER_ID", userDoc.id)
                    context.startActivity(intent)
                }
            }
        }

        binding.cardContainer.addView(view)
    }

    // Atanan Akademisyen kartlarını çek
    private fun loadAcademicianCards(requestId: String) {
        db.collection("Requests")
            .document(requestId).get()
            .addOnSuccessListener { document ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                if (document != null && document.exists()) {
                    val selectedAcademicianIds =
                        document["selectedAcademiciansId"] as? List<String> ?: emptyList()
                    val responses =
                        document["academicianResponses"] as? Map<String, String> ?: emptyMap()

                    selectedAcademicianIds.forEach { uid ->
                        db.collection("Academician").document(uid)
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

                                    val academicianImg =
                                        view.findViewById<ImageView>(R.id.academicianImg)
                                    val academicianName =
                                        view.findViewById<TextView>(R.id.selectedName)
                                    val academicianDegree =
                                        view.findViewById<TextView>(R.id.selectedDegree)
                                    val academicianContainer =
                                        view.findViewById<LinearLayout>(R.id.academicianContainer)
                                    val statusImg = view.findViewById<ImageView>(R.id.statusImg)
                                    val statusMessage =
                                        view.findViewById<TextView>(R.id.statusMessage)

                                    if (!profileUrl.isNullOrEmpty()) {
                                        Glide.with(this)
                                            .load(profileUrl)
                                            .placeholder(R.drawable.person)
                                            .error(R.drawable.person)
                                            .centerCrop()
                                            .into(academicianImg)
                                    } else {
                                        academicianImg.setImageResource(R.drawable.person)
                                    }


                                    academicianName.text = name
                                    academicianDegree.text = degree

                                    // Durum göstergesi
                                    when (status) {
                                        "approved" -> {
                                            statusImg.setImageResource(R.drawable.baseline_check_circle_outline_24)
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
                                        academicianContainer.addView(chip)
                                    }

                                    // Kart tıklanınca profil önizleme
                                    view.setOnClickListener {
                                        val intent =
                                            Intent(this, AcademicianPreviewActivity::class.java)
                                        intent.putExtra("source", "oldRequest")
                                        intent.putExtra("academicianId", uid)
                                        startActivity(intent)
                                    }

                                    binding.cardContainer.addView(view)
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

    //Üniversite adını bul
    private fun findUserUniversity(callback: (String?) -> Unit) {
        val email = auth.currentUser?.email ?: return callback(null)

        val domain = email.substringAfter("@") // Örn: istanbul.edu.tr

        db.collection("Authorities")
            .get()
            .addOnSuccessListener { result ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                for (doc in result) {
                    val academicians = doc.get("academician") as? List<String> ?: emptyList()

                    if (academicians.contains(domain)) {
                        callback(doc.id) // Üniversite adı
                        return@addOnSuccessListener
                    }
                }

                callback(null)
            }
            .addOnFailureListener { e ->
                Log.e("OldRequestDetail", "Authorities alınamadı", e)
                callback(null)
            }
    }

    // Geri Dön
    fun previousPage(view: View) {
        finish()
    }
}

