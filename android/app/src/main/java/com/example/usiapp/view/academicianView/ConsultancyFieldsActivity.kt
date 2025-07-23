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
import com.example.usiapp.databinding.ActivityConsultancyFieldsBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConsultancyFieldsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityConsultancyFieldsBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val consultancyFieldsList = mutableListOf<String>()

    private lateinit var consultancyFieldsInput: EditText
    private lateinit var addConsultancy: Button
    private lateinit var consultancyContainer: LinearLayout
    private lateinit var txtNoConsultancy: TextView

    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
       binding=ActivityConsultancyFieldsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        consultancyFieldsInput = binding.consultancyOfArea
        addConsultancy = binding.addConsultancyInfo
        consultancyContainer = binding.consultancyInfoContainer
        txtNoConsultancy = binding.txtNoConsultancy

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return


        //Kayıtlı verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val consultancies = document.get("verebilecegiDanismanlikKonulari") as? List<String>
                if (!consultancies.isNullOrEmpty()) {
                    consultancyFieldsList.addAll(consultancies)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = this@ConsultancyFieldsActivity,
                    container = consultancyContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "verebilecegiDanismanlikKonulari",
                    itemList = consultancyFieldsList,
                    noDataTextView = txtNoConsultancy
                )

                //Kart oluştur
                consultancyFieldsList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (consultancyFieldsList.isNotEmpty()) {
                    consultancyContainer.removeView(txtNoConsultancy)
                }

            },
            onFailure = {}
        )

        //Butona tıklama
        addConsultancy.setOnClickListener {
            val newConsultancy = consultancyFieldsInput.text.toString()
            cardHelper.addItem(newConsultancy, consultancyFieldsInput)
        }

    }

    fun goToProfile(view: View){
        val intent= Intent(this@ConsultancyFieldsActivity, AcademicianMainActivity::class.java)
        startActivity(intent)
    }
}