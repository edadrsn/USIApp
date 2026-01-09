package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityRequestContentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestContentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //Verileri al ve sonraki sayfaya gönder
        binding.btnGoForward.setOnClickListener {

            val requestTitle = binding.requestObject.text.toString()
            val requestMessage = binding.requestMessage.text.toString()
            val selectedCategories =
                intent.getStringArrayListExtra("selectedCategories") ?: arrayListOf()
            val switchRequestType = binding.switchRequestType.isChecked
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            //Açık talepse
            if (switchRequestType) {

                db.collection("Industry").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (isFinishing || isDestroyed) return@addOnSuccessListener

                        if (document != null && document.exists()) {
                            //Firebasedeki firma verilerini al oku
                            val firmaAdi = document.getString("firmaAdi") ?: ""
                            val firmaCalismaAlanlari = document.getString("calismaAlanlari") ?: ""
                            val firmaPhone = document.getString("telefon") ?: ""
                            val email = document.getString("email") ?: ""
                            val address = document.getString("adres") ?: ""
                            val firmImage = document.getString("requesterImage") ?: ""

                            //Sayfaya girilen verileri al
                            val requestTitle = binding.requestObject.text.toString()
                            val requestMessage = binding.requestMessage.text.toString()
                            val selectedCategories =
                                intent.getStringArrayListExtra("selectedCategories")
                                    ?: arrayListOf()
                            val currentDate =
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                            val switchRequestType = binding.switchRequestType.isChecked

                            // Bilgileri Firestore’a ekle
                            val requestInfo = hashMapOf(
                                "createdDate" to currentDate,
                                "requestMessage" to requestMessage,
                                "requestTitle" to requestTitle,
                                "requesterCategories" to firmaCalismaAlanlari,
                                "requesterEmail" to email,
                                "requesterID" to userId,
                                "requesterName" to firmaAdi,
                                "requesterPhone" to firmaPhone,
                                "requesterAddress" to address,
                                "selectedCategories" to selectedCategories,
                                "status" to mapOf("p9Wi7bPeyCiAMSVHRVlv" to "pending"),
                                "requesterImage" to firmImage,
                                "requesterType" to "industry",
                                "requestType" to switchRequestType
                            )
                            db.collection("Requests")
                                .add(requestInfo)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Talep başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, IndustryMainActivity::class.java)
                                    intent.putExtra("goToFragment", "request")
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Hata: ${it.localizedMessage}!", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Industry verisi alınamadı", e)
                        Toast.makeText(this, "Veri alınamadı!", Toast.LENGTH_SHORT).show()
                    }

            } else {
                val intent = Intent(this, SelectUniversityActivity::class.java)
                intent.putExtra("requestTitle", requestTitle)
                intent.putExtra("requestMessage", requestMessage)
                intent.putExtra("selectedCategories", selectedCategories)
                intent.putExtra("switchRequestType", switchRequestType)
                startActivity(intent)

                println(requestTitle)
                println(requestMessage)
                println(switchRequestType)
            }
        }
    }

    //Geri dön
    fun goToCreateRequest(view: View) {
        finish()
    }
}
