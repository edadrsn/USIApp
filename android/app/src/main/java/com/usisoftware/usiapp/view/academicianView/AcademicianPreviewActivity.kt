package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityAcademicianPreviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AcademicianPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianPreviewBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Kullanıcı ID'si belirle (karttan veya kendi giriş)
        val academicianIdFromIntent = intent.getStringExtra("USER_ID")
        val academicianId = if (!academicianIdFromIntent.isNullOrEmpty()) {
            academicianIdFromIntent
        } else {
            auth.currentUser?.uid
        }

        if (academicianId.isNullOrEmpty()) {
            Toast.makeText(this, "Akademisyen bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2️⃣ Source mantığını ayarla (header ve geri buton)
        val source = intent.getStringExtra("source") ?: "default"
        when (source) {
            "oldRequest" -> {
                binding.previewHeaderTitle.text = "Talep Detayı"
                binding.previewBackBtn.setOnClickListener {
                    startActivity(Intent(this, OldRequestsActivity::class.java))
                    finish()
                }
            }
            "appoint" -> {
                binding.previewHeaderTitle.text = "Akademisyen Ata"
                binding.previewBackBtn.setOnClickListener {
                    startActivity(Intent(this, AppointAcademicianActivity::class.java))
                    finish()
                }
            }
            else -> {
                binding.previewHeaderTitle.text = "Önizleme"
                binding.previewBackBtn.setOnClickListener { finish() }
            }
        }

        //Firestore'dan veriyi çek
        db.collection("AcademicianInfo")
            .document(academicianId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val getPhoto = document.getString("photo") ?: ""
                    Picasso.get()
                        .load(getPhoto)
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(binding.previewAcademicianPhoto)

                    binding.previewAcademicianName.text = document.getString("adSoyad") ?: ""
                    binding.previewAcademicianDegree.text = document.getString("unvan") ?: ""
                    binding.previewAcademicInfo.text = document.getString("akademikGecmis") ?: ""

                    val isChecked = document.getString("ortakProjeTalep") == "Evet"
                    binding.previewSwitchProject.isChecked = isChecked
                    setSwitchUI(isChecked)

                    binding.previewPhoneNum.text = document.getString("personelTel") ?: ""
                    binding.previewCorporateNum.text = document.getString("kurumsalTel") ?: ""
                    binding.previewEmail.text = document.getString("email") ?: ""

                    val location = "${document.getString("il") ?: ""} / ${document.getString("ilce") ?: ""}"
                    binding.previewDistrictAndProvince.text = location
                    binding.previewWeb.text = document.getString("web") ?: ""

                    // Firma bilgileri
                    val firmContainer = binding.previewFirm
                    firmContainer.removeAllViews()
                    val firmData = document.get("firmalar") as? List<Map<String, Any>> ?: emptyList()
                    firmData.forEach { firmMap ->
                        val firmaAdi = firmMap["firmaAdi"] as? String ?: "Firma adı yok"
                        val calismaAlaniList = firmMap["firmaCalismaAlani"] as? List<String> ?: emptyList()
                        val calismaAlaniText = calismaAlaniList.joinToString(" • ")

                        val firmNameText = TextView(this).apply {
                            text = "💻 $firmaAdi"
                            setTextColor(Color.BLACK)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                            setTypeface(null, Typeface.BOLD)
                        }
                        val workAreaText = TextView(this).apply {
                            text = "📍 $calismaAlaniText"
                            setTextColor(Color.DKGRAY)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                        }
                        val spacer = View(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 20
                            )
                        }
                        firmContainer.addView(firmNameText)
                        firmContainer.addView(workAreaText)
                        firmContainer.addView(spacer)
                    }

                    // Uzmanlık, danışmanlık, eğitim alanları
                    binding.previewProfession.text = (document.get("uzmanlikAlanlari") as? List<String> ?: emptyList())
                        .joinToString("\n") { "• $it" }
                    binding.previewConsultancyFields.text = (document.get("verebilecegiDanismanlikKonulari") as? List<String> ?: emptyList())
                        .joinToString("\n") { "• $it" }
                    binding.previewPrevConsultancy.text = (document.get("dahaOncekiDanismanliklar") as? List<String> ?: emptyList())
                        .joinToString("\n") { "• $it" }
                    binding.previewEducations.text = (document.get("verebilecegiEgitimler") as? List<String> ?: emptyList())
                        .joinToString("\n") { "• $it" }
                    binding.previewPrevEducations.text = (document.get("dahaOnceVerdigiEgitimler") as? List<String> ?: emptyList())
                        .joinToString("\n") { "• $it" }

                } else {
                    Toast.makeText(this, "Akademisyen bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Akademisyen bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
            }
    }

    fun setSwitchUI(isChecked: Boolean) {
        val color = Color.parseColor(if (isChecked) "#4EA222" else "#FF0000")
        val colorStateList = ColorStateList.valueOf(color)
        binding.previewSwitchProject.thumbTintList = colorStateList
        binding.previewSwitchProject.trackTintList = colorStateList
        binding.project.strokeColor = color
    }
}

