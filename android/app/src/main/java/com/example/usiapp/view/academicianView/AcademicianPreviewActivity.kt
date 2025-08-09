package com.example.usiapp.view.academicianView

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
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianPreviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AcademicianPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianPreviewBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Kenarlara kadar içerik gösterimi için (muhtemelen full screen görünüm için)

        // ViewBinding ile layout'u bağla
        binding = ActivityAcademicianPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firestore ve FirebaseAuth nesnelerini başlat
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Intent'ten akademisyen ID ve kaynak bilgisi al
        val academicianId = intent.getStringExtra("academicianId")
        val source = intent.getStringExtra("source") ?: "default"

        // Kaynağa göre başlık ve geri butonunun işlevini ayarla
        when (source) {
            "oldRequest" -> {
                binding.previewHeaderTitle.text = "Talep Detayı"
                binding.previewBackBtn.setOnClickListener {
                    startActivity(Intent(this@AcademicianPreviewActivity, OldRequestsActivity::class.java))
                    finish()
                }
            }

            "appoint" -> {
                binding.previewHeaderTitle.text = "Akademisyen Ata"
                binding.previewBackBtn.setOnClickListener {
                    startActivity(Intent(this@AcademicianPreviewActivity, AppointAcademicianActivity::class.java))
                    finish()
                }
            }

            else -> {
                binding.previewHeaderTitle.text = "Önizleme"
                binding.previewBackBtn.setOnClickListener {
                    finish()
                }
            }
        }

        // Eğer akademisyen ID null değilse Firestore'dan bilgileri çek
        if (academicianId != null) {
            db.collection("AcademicianInfo")
                .document(academicianId)
                .get()
                .addOnSuccessListener { document ->
                    // Fotoğraf URL'sini al, yoksa boş string
                    val getPhoto = document.getString("photo") ?: ""

                    if (document.exists()) {
                        // Picasso ile fotoğrafı yükle, yoksa placeholder kullan
                        Picasso.get()
                            .load(getPhoto)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(binding.previewAcademicianPhoto)

                        // Akademisyenin adını, unvanını ve diğer metin bilgilerini çek ve göster
                        val getName = document.getString("adSoyad") ?: ""
                        binding.previewAcademicianName.text = getName

                        val getDegree = document.getString("unvan") ?: ""
                        binding.previewAcademicianDegree.text = getDegree

                        val getAcademicInfo = document.getString("akademikGecmis") ?: ""
                        binding.previewAcademicInfo.text = getAcademicInfo

                        // Ortak proje talebini kontrol et ve switch durumunu ayarla
                        val getProjectRequest = document.getString("ortakProjeTalep") ?: "Hayır"
                        val isChecked = getProjectRequest == "Evet"
                        binding.previewSwitchProject.isChecked = isChecked
                        setSwitchUI(isChecked)

                        // Telefon, kurumsal telefon, mail ve lokasyon bilgileri
                        val getPhoneNum = document.getString("personelTel") ?: ""
                        binding.previewPhoneNum.text = getPhoneNum

                        val getCorporateNum = document.getString("kurumsalTel") ?: ""
                        binding.previewCorporateNum.text = getCorporateNum

                        val getMail = document.getString("email") ?: ""
                        binding.previewEmail.text = getMail

                        val getProvince = document.getString("il") ?: ""
                        val getDistrict = document.getString("ilce") ?: ""
                        val location = "$getProvince / $getDistrict"
                        binding.previewDistrictAndProvince.text = location

                        val website = document.getString("web") ?: ""
                        binding.previewWeb.text = website

                        // Firma bilgilerini dinamik olarak ekle
                        val firmContainer = binding.previewFirm
                        firmContainer.removeAllViews() // Önce temizle

                        val firmData = document.get("firmalar") as? List<Map<String, Any>> ?: emptyList()

                        firmData.forEach { firmMap ->
                            val firmaAdi = firmMap["firmaAdi"] as? String ?: "Firma adı yok"
                            val calismaAlaniList = firmMap["firmaCalismaAlani"] as? List<String> ?: emptyList()
                            val calismaAlaniText = calismaAlaniList.joinToString(separator = " • ")

                            // Firma adı TextView
                            val firmNameText = TextView(this).apply {
                                text = "💻 $firmaAdi"
                                setTextColor(Color.BLACK)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                setTypeface(null, Typeface.BOLD)
                            }

                            // Firma çalışma alanı TextView
                            val workAreaText = TextView(this).apply {
                                text = "📍 $calismaAlaniText"
                                setTextColor(Color.DKGRAY)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                            }

                            // Araya boşluk koymak için spacer view
                            val spacer = View(this).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    20
                                )
                            }

                            // Firma bilgilerini sırayla ekle
                            firmContainer.addView(firmNameText)
                            firmContainer.addView(workAreaText)
                            firmContainer.addView(spacer)
                        }

                        // Uzmanlık alanları, danışmanlık konuları ve eğitim bilgilerini liste halinde göster
                        val getProfessions = document.get("uzmanlikAlanlari") as? List<String> ?: emptyList()
                        binding.previewProfession.text = getProfessions.joinToString(separator = "\n") { "• $it" }

                        val getConsultancyFields = document.get("verebilecegiDanismanlikKonulari") as? List<String> ?: emptyList()
                        binding.previewConsultancyFields.text = getConsultancyFields.joinToString(separator = "\n") { "• $it" }

                        val getPrevConsultancies = document.get("dahaOncekiDanismanliklar") as? List<String> ?: emptyList()
                        binding.previewPrevConsultancy.text = getPrevConsultancies.joinToString(separator = "\n") { "• $it" }

                        val getEducations = document.get("verebilecegiEgitimler") as? List<String> ?: emptyList()
                        binding.previewEducations.text = getEducations.joinToString(separator = "\n") { "• $it" }

                        val getPrevEducations = document.get("dahaOnceVerdigiEgitimler") as? List<String> ?: emptyList()
                        binding.previewPrevEducations.text = getPrevEducations.joinToString(separator = "\n") { "• $it" }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Akademisyen bulunamadı!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Switch rengini ve track, thumb renklerini ayarlar
    fun setSwitchUI(isChecked: Boolean) {
        val colorHex = if (isChecked) "#4EA222" else "#FF0000"
        val color = Color.parseColor(colorHex)
        val colorStateList = ColorStateList.valueOf(color)

        binding.previewSwitchProject.thumbTintList = colorStateList
        binding.previewSwitchProject.trackTintList = colorStateList
        binding.project.strokeColor = color
    }
}
