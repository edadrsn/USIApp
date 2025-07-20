package com.example.usiapp.view.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityEducationBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EducationActivity : AppCompatActivity() {

    private lateinit var binding:ActivityEducationBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val educationList = mutableListOf<String>()

    private lateinit var educationInput: EditText
    private lateinit var addEducation: Button
    private lateinit var educationContainer: LinearLayout
    private lateinit var txtNoEducation: TextView

    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityEducationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        educationInput = binding.educationOfArea
        addEducation = binding.addEducationInfo
        educationContainer = binding.educationContainer
        txtNoEducation = binding.txtNoEducation

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
                    container = educationContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "verebilecegiEgitimler",
                    itemList = educationList,
                    noDataTextView = txtNoEducation
                )

                //Kart oluştur
                educationList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (educationList.isNotEmpty()) {
                    educationContainer.removeView(txtNoEducation)
                }
            },
            onFailure = {}
        )


        //Butona tıklama
        addEducation.setOnClickListener {
            val newEducation = educationInput.text.toString()
            cardHelper.addItem(newEducation, educationInput)
        }

    }

    fun goToProfile(view: View){
        val intent= Intent(this@EducationActivity,AcademicianMainActivity::class.java)
        startActivity(intent)
    }

}
