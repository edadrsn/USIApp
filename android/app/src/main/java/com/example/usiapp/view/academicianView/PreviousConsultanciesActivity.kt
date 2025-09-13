package com.example.usiapp.view.academicianView

import android.os.Bundle
import android.view.View
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
    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPreviousConsultanciesBinding.inflate(layoutInflater)
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
                val prevConsultancy = document.get("dahaOncekiDanismanliklar") as? List<String>
                if (!prevConsultancy.isNullOrEmpty()) {
                    prevConsultanciesList.addAll(prevConsultancy)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@PreviousConsultanciesActivity,
                    container = binding.prevConsultancyContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "dahaOncekiDanismanliklar",
                    itemList = prevConsultanciesList,
                    noDataTextView = binding.txtNoConsultancy
                )

                //Kart oluştur
                prevConsultanciesList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (prevConsultanciesList.isNotEmpty()) {
                    binding.prevConsultancyContainer.removeView(binding.txtNoConsultancy)
                }

            },
            onFailure = {}
        )

        //Butona tıklama
        binding.addPrevConsultancyInfo.setOnClickListener {
            val newPrevConsultancy = binding.prevConsultancyOfArea.text.toString()
            cardHelper.addItem(newPrevConsultancy, binding.prevConsultancyOfArea)
        }
    }

    //Geri dön
    fun goToProfile(view: View){
        finish()
    }
}