package com.usisoftware.usiapp.view.academicianView

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityAcademicInfoBinding
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class AcademicInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var academicInfo: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityAcademicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        academicInfo = binding.academicEditText

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId=currentUser.uid

        //Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                if (document == null || !document.exists()) {
                    Toast.makeText(this, "Bilgi bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@getAcademicianInfoByEmail
                }

                val getAcademicInfo = document.getString("akademikGecmis") ?: ""
                academicInfo.setText(getAcademicInfo)

            },
            onFailure = { e ->
                Log.e("AcademicInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
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
                       userId,
                        updates,
                        onSuccess = {
                            Toast.makeText(this@AcademicInfoActivity, "Bilgiler başarıyla güncellendi.", Toast.LENGTH_LONG).show()
                            finish()
                        },
                        onFailure = {
                            Toast.makeText(this@AcademicInfoActivity, "Bilgileri güncellerken sorun oluştu!", Toast.LENGTH_LONG).show()
                            Log.e("Hata",": ${it.localizedMessage}")
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

    //Geri dön
    fun goToProfile(view: View){
        finish()
    }
}