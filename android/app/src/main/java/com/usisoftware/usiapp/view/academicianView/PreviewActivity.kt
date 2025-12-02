package com.usisoftware.usiapp.view.academicianView

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityPreviewBinding
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPreviewBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Giriş yapan kullanıcı uid
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId=currentUser.uid

        // İlk yükleme
        loadInfo(userId)

        // SwipeRefreshLayout ekle
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (!isFinishing && !isDestroyed) {
                loadInfo(userId)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

    }

    //Akademisyen verilerini çek
    fun loadInfo(userId:String){
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                val getPhoto = document.getString("photo")
                if (!getPhoto.isNullOrEmpty()) {
                    loadImageWithCorrectRotation(
                        this@PreviewActivity,
                        getPhoto,
                        binding.academicianPhoto,
                        R.drawable.person)
                } else {
                    binding.academicianPhoto.setImageResource(R.drawable.person)
                }

                val getName = document.getString("adSoyad") ?: ""
                binding.academicianName.setText(getName)

                val getDegree = document.getString("unvan") ?: ""
                binding.academicianDegree.setText(getDegree)

                val getAcademicInfo = document.getString("akademikGecmis") ?: ""
                binding.academicInfo.setText(getAcademicInfo)


                val projectRequest = document.getString("ortakProjeTalep") ?: "Hayır"
                val isChecked = projectRequest == "Evet"
                binding.switchProject.isChecked = isChecked
                setSwitchUI(isChecked)


                val getPhoneNum = document.getString("personelTel") ?: ""
                binding.phoneNum.setText(getPhoneNum)

                val getCorporateNum=document.getString("kurumsalTel") ?: ""
                binding.corporateNum.setText(getCorporateNum)

                val getEmail = document.getString("email") ?: ""
                binding.email.setText(getEmail)

                val getProvince=document.getString("il") ?: ""
                val getDistrict=document.getString("ilce") ?: ""
                val location=getProvince+" / "+getDistrict
                binding.districtAndProvince.setText(location)


                val getWebsite = document.getString("web") ?: ""
                binding.web.setText(getWebsite)


                val firmContainer = binding.firm
                firmContainer.removeAllViews()

                val firmData = document.get("firmalar") as? List<Map<String, Any>> ?: emptyList()

                firmData.forEach { firmMap ->
                    val firmaAdi = firmMap["firmaAdi"] as? String ?: "Firma adı yok"
                    val calismaAlaniList = firmMap["firmaCalismaAlani"] as? List<String> ?: emptyList()
                    val calismaAlaniText = calismaAlaniList.joinToString(separator = " • ")

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

                    // Araya boşluk için spacer view
                    val spacer = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            20
                        )
                    }

                    // LinearLayout'a sırayla ekle
                    firmContainer.addView(firmNameText)
                    firmContainer.addView(workAreaText)
                    firmContainer.addView(spacer)
                }

                val getProfessions = document.get("uzmanlikAlanlari") as? List<String> ?: emptyList()
                binding.profession.text = getProfessions.joinToString(separator = "\n"){ "• $it" }

                val getConsultancyFields = document.get("verebilecegiDanismanlikKonulari") as? List<String> ?: emptyList()
                binding.consultancyFields.text = getConsultancyFields.joinToString(separator = "\n"){ "• $it" }

                val getPrevConsultancies = document.get("dahaOncekiDanismanliklar") as? List<String> ?: emptyList()
                binding.previousConsultancy.text = getPrevConsultancies.joinToString(separator = "\n"){ "• $it" }

                val getEducations = document.get("verebilecegiEgitimler") as? List<String> ?: emptyList()
                binding.educations.text = getEducations.joinToString(separator = "\n"){ "• $it" }

                val getPrevEducations = document.get("dahaOnceVerdigiEgitimler") as? List<String> ?: emptyList()
                binding.prevEducations.text = getPrevEducations.joinToString(separator = "\n"){ "• $it" }

            },
            onFailure = {
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail
                Toast.makeText(this@PreviewActivity,"Hata:veriler çekilemedi",Toast.LENGTH_SHORT).show()
                Log.e("PreviewActivity","Hata Veri bulunamadı")
            }
        )
    }

    //Geri dön
    fun goToBack(view:View){
        finish()
    }

    // Switch rengini ve track, thumb renklerini ayarla
    fun setSwitchUI(isChecked: Boolean) {
        val colorHex = if (isChecked) "#4EA222" else "#FF0000"
        val color = Color.parseColor(colorHex)
        val colorStateList = ColorStateList.valueOf(color)

        binding.switchProject.thumbTintList = colorStateList
        binding.switchProject.trackTintList = colorStateList
        binding.project.strokeColor = color
    }
}

