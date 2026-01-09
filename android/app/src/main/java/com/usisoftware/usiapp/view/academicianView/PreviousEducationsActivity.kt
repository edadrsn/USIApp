package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityPreviousEducationsBinding
import com.usisoftware.usiapp.view.repository.CreateCardAndAddData
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class PreviousEducationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviousEducationsBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val prevEducationList = mutableListOf<String>()
    private lateinit var prevEducationInfo: EditText
    private lateinit var addPrevEdu: Button
    private lateinit var prevEduContainer: LinearLayout
    private lateinit var txtNo: TextView
    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreviousEducationsBinding.inflate(layoutInflater)
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

        prevEducationInfo = binding.prevEducationOfArea
        addPrevEdu = binding.btnAddPrevEducation
        prevEduContainer = binding.prevEducationContainer
        txtNo = binding.txtNoEducation

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                val prevEducation = document.get("dahaOnceVerdigiEgitimler") as? List<String>
                if (!prevEducation.isNullOrEmpty()) {
                    prevEducationList.addAll(prevEducation)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    this@PreviousEducationsActivity,
                    prevEduContainer,
                    db,
                    userId,
                    listKey = "dahaOnceVerdigiEgitimler",
                    prevEducationList,
                    txtNo
                )

                //Kart oluştur
                prevEducationList.forEach { cardHelper.createCard(it) }

            },
            onFailure = { e ->
                Log.e("PreviousEducationsActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        //Butona tıklama
        addPrevEdu.setOnClickListener {
            val newPrevEducation = prevEducationInfo.text.toString()
            cardHelper.addItem(newPrevEducation, prevEducationInfo)
        }
    }

    //Geri dön
    fun goToProfile(view: View) {
        finish()
    }
}