package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityConsultancyFieldsBinding
import com.usisoftware.usiapp.view.repository.CreateCardAndAddData
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class ConsultancyFieldsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultancyFieldsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val consultancyFieldsList = mutableListOf<String>()
    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConsultancyFieldsBinding.inflate(layoutInflater)
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


        //Kayıtlı verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                val consultancies = document.get("verebilecegiDanismanlikKonulari") as? List<String>
                if (!consultancies.isNullOrEmpty()) {
                    consultancyFieldsList.addAll(consultancies)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    this@ConsultancyFieldsActivity,
                    binding.consultancyInfoContainer,
                    db,
                    userId,
                    listKey = "verebilecegiDanismanlikKonulari",
                    consultancyFieldsList,
                    binding.txtNoConsultancy
                )

                //Kart oluştur
                consultancyFieldsList.forEach { cardHelper.createCard(it) }


            },
            onFailure = { e ->
                Log.e("ConsultancyFieldsActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        //Butona tıklama
        binding.addConsultancyInfo.setOnClickListener {
            val newConsultancy = binding.consultancyOfArea.text.toString()
            cardHelper.addItem(newConsultancy, binding.consultancyOfArea)
        }

    }

    //Geri dön
    fun goToProfile(view: View) {
        finish()
    }
}