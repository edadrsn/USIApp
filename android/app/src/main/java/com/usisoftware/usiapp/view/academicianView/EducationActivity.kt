package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivityEducationBinding
import com.usisoftware.usiapp.view.repository.CreateCardAndAddData
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EducationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEducationBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null
    private val educationList = mutableListOf<String>()
    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEducationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val education = document.get("verebilecegiEgitimler") as? List<String>
                if (!education.isNullOrEmpty()) {
                    educationList.addAll(education)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@EducationActivity,
                    container = binding.educationContainer,
                    db = db,
                    documentId = documentId!!,
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
            onFailure = {}
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
