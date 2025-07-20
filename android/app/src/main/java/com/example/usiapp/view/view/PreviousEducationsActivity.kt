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
import com.example.usiapp.databinding.ActivityPreviousEducationsBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreviousEducationsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPreviousEducationsBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val prevEducationList = mutableListOf<String>()

    private lateinit var prevEducationInfo: EditText
    private lateinit var addPrevEdu: Button
    private lateinit var prevEduContainer: LinearLayout
    private lateinit var txtNo: TextView

    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPreviousEducationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        prevEducationInfo = binding.prevEducationOfArea
        addPrevEdu = binding.btnAddPrevEducation
        prevEduContainer = binding.prevEducationContainer
        txtNo = binding.txtNoEducation

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id

                val prevEducation = document.get("dahaOnceVerdigiEgitimler") as? List<String>
                if (!prevEducation.isNullOrEmpty()) {
                    prevEducationList.addAll(prevEducation)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@PreviousEducationsActivity,
                    container = prevEduContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "dahaOnceVerdigiEgitimler",
                    itemList = prevEducationList,
                    noDataTextView = txtNo
                )

                //Kart oluştur
                prevEducationList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (prevEducationList.isNotEmpty()) {
                    prevEduContainer.removeView(txtNo)
                }

            },
            onFailure = {}
        )


        //Butona tıklama
        addPrevEdu.setOnClickListener {
            val newPrevEducation = prevEducationInfo.text.toString()
            cardHelper.addItem(newPrevEducation, prevEducationInfo)
        }
    }

    fun goToProfile(view: View){
        val intent= Intent(this@PreviousEducationsActivity,AcademicianMainActivity::class.java)
        startActivity(intent)
    }
}