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
import com.usisoftware.usiapp.databinding.ActivityProfessionInfoBinding
import com.usisoftware.usiapp.view.repository.CreateCardAndAddData
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class ProfessionInfoActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfessionInfoBinding
    private lateinit var professionName: EditText
    private lateinit var noTextInfo: TextView
    private lateinit var addProfInfo: Button
    private lateinit var professionContainer: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val professionList = mutableListOf<String>()
    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityProfessionInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        professionName = binding.professionText
        professionContainer = binding.professionInfoContainer
        noTextInfo = binding.txtNoProfession
        addProfInfo = binding.addInfo

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

        //Verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                val professions = document.get("uzmanlikAlanlari") as? List<String>
                if (!professions.isNullOrEmpty()) {
                    professionList.addAll(professions)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    this@ProfessionInfoActivity,
                    professionContainer,
                    db,
                    userId,
                    "uzmanlikAlanlari",
                    professionList,
                    noTextInfo
                )

                //Kart oluştur
                professionList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (professionList.isNotEmpty()) {
                    professionContainer.removeView(noTextInfo)
                }
            },
            onFailure = { e ->
                Log.e("ProfessionInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        //Butona tıklama
        addProfInfo.setOnClickListener {
            val newProfession = professionName.text.toString()
            cardHelper.addItem(newProfession, professionName)
        }
    }

    //Geri dön
    fun goToProfile(view: View){
       finish()
    }
}