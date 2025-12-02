package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityEducationBinding
import com.usisoftware.usiapp.view.repository.CreateCardAndAddData
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class EducationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEducationBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val educationList = mutableListOf<String>()
    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEducationBinding.inflate(layoutInflater)
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

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                val education = document.get("verebilecegiEgitimler") as? List<String>
                if (!education.isNullOrEmpty()) {
                    educationList.addAll(education)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@EducationActivity,
                    container = binding.educationContainer,
                    db = db,
                    userId = userId,
                    listKey = "verebilecegiEgitimler",
                    itemList = educationList,
                    noDataTextView = binding.txtNoEducation
                )

                //Kart oluştur
                educationList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (educationList.isNotEmpty()) {
                    binding.educationContainer.removeView(binding.txtNoEducation)
                }
            },
            onFailure = { e ->
                Log.e("EducationActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        //Butona tıklama
        binding.addEducationInfo.setOnClickListener {
            val newEducation = binding.educationOfArea.text.toString()
            cardHelper.addItem(newEducation, binding.educationOfArea)
        }

    }

    //Geri dön
    fun goToProfile(view: View) {
       finish()
    }

}
