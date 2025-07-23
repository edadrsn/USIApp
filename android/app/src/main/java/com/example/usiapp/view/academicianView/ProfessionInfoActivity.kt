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
import com.example.usiapp.databinding.ActivityProfessionInfoBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfessionInfoActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfessionInfoBinding

    private lateinit var professionName: EditText
    private lateinit var noTextInfo: TextView
    private lateinit var addProfInfo: Button
    private lateinit var professionContainer: LinearLayout

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

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
        val email = auth.currentUser?.email ?: return

        //Verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val professions = document.get("uzmanlikAlanlari") as? List<String>
                if (!professions.isNullOrEmpty()) {
                    professionList.addAll(professions)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@ProfessionInfoActivity,
                    container = professionContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "uzmanlikAlanlari",
                    itemList = professionList,
                    noDataTextView = noTextInfo
                )

                //Kart oluştur
                professionList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (professionList.isNotEmpty()) {
                    professionContainer.removeView(noTextInfo)
                }
            },
            onFailure = {}
        )

        //Butona tıklama
        addProfInfo.setOnClickListener {
            val newProfession = professionName.text.toString()
            cardHelper.addItem(newProfession, professionName)
        }
    }

    fun goToProfile(view: View){
        val intent= Intent(this@ProfessionInfoActivity, AcademicianMainActivity::class.java)
        startActivity(intent)
    }
}