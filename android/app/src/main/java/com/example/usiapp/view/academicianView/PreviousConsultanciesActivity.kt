package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityPreviousConsultanciesBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreviousConsultanciesActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPreviousConsultanciesBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val prevConsultanciesList = mutableListOf<String>()

    private lateinit var prevConsultancies: EditText
    private lateinit var addPrevConsultancy: Button
    private lateinit var prevConsultancyContainer: LinearLayout
    private lateinit var txtNoConsultancy: TextView

    private lateinit var cardHelper: CreateCardAndAddData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPreviousConsultanciesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prevConsultancies = binding.prevConsultancyOfArea
        addPrevConsultancy = binding.addPrevConsultancyInfo
        prevConsultancyContainer = binding.prevConsultancyContainer
        txtNoConsultancy = binding.txtNoConsultancy

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val prevConsultancy = document.get("dahaOncekiDanismanliklar") as? List<String>
                if (!prevConsultancy.isNullOrEmpty()) {
                    prevConsultanciesList.addAll(prevConsultancy)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@PreviousConsultanciesActivity,
                    container = prevConsultancyContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "dahaOncekiDanismanliklar",
                    itemList = prevConsultanciesList,
                    noDataTextView = txtNoConsultancy
                )

                //Kart oluştur
                prevConsultanciesList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (prevConsultanciesList.isNotEmpty()) {
                    prevConsultancyContainer.removeView(txtNoConsultancy)
                }

            },
            onFailure = {}
        )

        //Butona tıklama
        addPrevConsultancy.setOnClickListener {
            val newPrevConsultancy = prevConsultancies.text.toString()
            cardHelper.addItem(newPrevConsultancy, prevConsultancies)
        }
    }

    fun goToProfile(view: View){
        val intent= Intent(this@PreviousConsultanciesActivity, AcademicianMainActivity::class.java)
        startActivity(intent)
    }
}