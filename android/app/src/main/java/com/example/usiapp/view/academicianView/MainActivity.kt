package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityMainBinding
import com.example.usiapp.view.industryView.IndustryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // View binding kullanılarak layout öğelerine erişim sağlanır
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Sistem UI ile uyumlu hale getirir (örn. tam ekran)

        // Layout dosyasını bağla
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Uygulama açıldığında kullanıcı daha önce giriş yaptıysa otomatik yönlendirme yap
        checkLoggedInUser()
    }

    /**
     * Kullanıcının oturumunun açık olup olmadığını kontrol eder.
     * Eğer kullanıcı giriş yaptıysa ve mail doğrulandıysa,
     * Firestore'da "AcademicianInfo" koleksiyonunda varlığına göre
     * AcademicianMainActivity sayfasına yönlendirir.
     */
    private fun checkLoggedInUser() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser // Şu anki oturumdaki kullanıcı alınır

        // Kullanıcı varsa ve e-posta doğrulandıysa
        if (user != null && user.isEmailVerified) {
            val db = FirebaseFirestore.getInstance()
            val email = user.email ?: return

            // Kullanıcının akademisyen olup olmadığını kontrol et
            db.collection("AcademicianInfo")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Kullanıcı akademisyense direkt ana sayfaya yönlendir
                        val intent = Intent(this, AcademicianMainActivity::class.java)
                        startActivity(intent)
                        finish() // MainActivity’yi kapat ki geri dönülemesin
                    }
                }
                .addOnFailureListener { e ->
                    // Firestore'dan veri alınamazsa hata logla
                    Log.e("LOGIN_FLOW", "Firestore kontrol hatası: ${e.localizedMessage}")
                }
        }
    }


    //Sanayi kullanıcıları için buton

    fun clickIndustry(view: View) {
        val intent = Intent(this@MainActivity, IndustryActivity::class.java)
        startActivity(intent)
    }

    //Akademisyen kullanıcıları için buton

    fun clickAcademician(view: View) {
        val intent = Intent(this@MainActivity, AcademicianLoginActivity::class.java)
        startActivity(intent)
    }
}
