package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityPreviousConsultanciesBinding
import com.usisoftware.usiapp.view.repository.CreateCardAndAddData
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class PreviousConsultanciesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviousConsultanciesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val prevConsultanciesList = mutableListOf<String>()
    private var cardHelper: CreateCardAndAddData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreviousConsultanciesBinding.inflate(layoutInflater)
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
        val userId = currentUser.uid

        // Firestore’dan akademisyen verisini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                // Listeyi doldur
                val prevConsultancy = document.get("dahaOncekiDanismanliklar") as? List<String>
                if (!prevConsultancy.isNullOrEmpty()) {
                    prevConsultanciesList.addAll(prevConsultancy)
                }

                // CardHelper kur
                cardHelper = CreateCardAndAddData(
                     this@PreviousConsultanciesActivity,
                    binding.prevConsultancyContainer,
                    db,
                    userId,
                    listKey = "dahaOncekiDanismanliklar",
                    prevConsultanciesList,
                    binding.txtNoConsultancy
                )

                //Önceki kayıtları kart olarak ekle
                prevConsultanciesList.forEach { item ->
                    cardHelper?.createCard(item)
                }

            },
            onFailure = { e ->
                Log.e("ConsultancyFieldsActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        // Yeni kayıt ekle
        binding.addPrevConsultancyInfo.setOnClickListener {
            val newItem = binding.prevConsultancyOfArea.text.toString().trim()

            if (newItem.isNotEmpty()) {
                cardHelper?.addItem(newItem, binding.prevConsultancyOfArea)
            } else {
                Toast.makeText(this, "Boş bilgi eklenemez!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun goToProfile(view: View) {
        finish()
    }
}
