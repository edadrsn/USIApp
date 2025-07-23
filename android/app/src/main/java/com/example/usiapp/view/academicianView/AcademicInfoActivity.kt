package com.example.usiapp.view.academicianView

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityAcademicInfoBinding
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AcademicInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicInfoBinding


    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null
    private lateinit var academicInfo: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityAcademicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        academicInfo = binding.academicEditText

        val email=auth.currentUser?.email?: return

        //Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val getAcademicInfo = document.getString("akademikGecmis") ?: ""

                academicInfo.setText(getAcademicInfo)

            },
            onFailure = { }
        )

        //Butona basınca güncellemek istediğine dair soru sor
        binding.btnUpdateAcademicInfo.setOnClickListener {
            AlertDialog.Builder(this@AcademicInfoActivity).apply {
                setTitle("Güncelleme")
                setMessage("Akademik Geçmiş bilgilerinizi güncellemek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->

                    val updateAcademicInfo = binding.academicEditText.text.toString()
                    val updates = hashMapOf<String, Any>(
                        "akademikGecmis" to updateAcademicInfo
                    )
                    GetAndUpdateAcademician.updateAcademicianInfo(
                        db,
                        documentId.toString(),
                        updates,
                        onSuccess = {
                            Toast.makeText(
                                this@AcademicInfoActivity,
                                "Bilgiler başarıyla güncellendi.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onFailure = {
                            Toast.makeText(
                                this@AcademicInfoActivity,
                                "Hata: ${it.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                    dialog.dismiss()
                }
                setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }

    fun goToProfile(view: View){
        val intent= Intent(this@AcademicInfoActivity, AcademicianMainActivity::class.java)
        startActivity(intent)
    }
}