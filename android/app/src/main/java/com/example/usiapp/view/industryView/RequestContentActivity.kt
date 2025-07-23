package com.example.usiapp.view.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityRequestContentBinding
import com.example.usiapp.view.repository.RequestFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
        val email=auth.currentUser?.email?: return


        binding.create.setOnClickListener {

            // Diğer Activity'den gelen seçili kategorileri alıyoruz, boşsa boş liste ver
            val selectedCategories = intent.getStringArrayListExtra("selectedCategories") ?: arrayListOf()

            // Kullanıcının girdiği talep başlığı ve açıklamasını al
            val requestObject = binding.requestObject.text.toString()
            val requestMessage = binding.requestMessage.text.toString()

            // Eğer kullanıcı daha önce kayıtlı değilse Industry koleksiyonu için temel bilgileri hazırla
            val saveInfo = mapOf(
                "email" to email,
                "firmaAdi" to "ABC Ltd." // (İstersen bunu dinamik yapabilirsin)
            )

            // Firestore'a kaydedilecek asıl talep bilgilerini oluştur
            val requestInfo = mapOf(
                "createdAt" to FieldValue.serverTimestamp(), // Sunucu zamanı
                "requestMessage" to requestMessage,          // Talep açıklaması
                "requestTitle" to requestObject,             // Talep başlığı
                "selectedCategories" to selectedCategories   // Seçili kategoriler (!!! Firestore'da key doğru yazıldı mı kontrol et)
            )

            // Firestore'a veriyi gönder
            RequestFirebase.addNewRequest(
                db = FirebaseFirestore.getInstance(),
                email = email,
                saveInfo = saveInfo,
                requestInfo = requestInfo,
                onSuccess = {
                    // Başarılıysa kullanıcıya bildirim ver ve diğer sayfaya yönlendir
                    Toast.makeText(this@RequestContentActivity, "Talep başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, IndustryMainActivity::class.java)
                    intent.putExtra("goToFragment", "request")
                    startActivity(intent)
                    finish()
                },
                onFailure = { e ->
                    Toast.makeText(this@RequestContentActivity, "Talep oluşturulamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

        //Geri dön
    fun goToCreateRequest(view: View) {
        val intent = Intent(this@RequestContentActivity, CreateRequestActivity::class.java)
        startActivity(intent)
    }


}