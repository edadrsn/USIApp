package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityVerificationBinding
import com.example.usiapp.view.view.PreviewActivity
import com.google.firebase.auth.FirebaseAuth


class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var btnVerification: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Butona tıklanıldığında mail doğrulamasını kontrol et
        btnVerification = findViewById(R.id.btnVerification)
        btnVerification.setOnClickListener {
            checkEmailVerification()
        }
    }

    // Kullanıcının e-posta adresinin doğrulanıp doğrulanmadığını kontrol et
    private fun checkEmailVerification() {
        val user = auth.currentUser // Mevcut kullanıcıyı al
        // Kullanıcı bilgisini güncelle
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Eğer kullanıcı e-postasını doğrulamışsa
                if (user.isEmailVerified) {
                    Toast.makeText(this, "E-posta doğrulandı. Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show()

                    // Doğrulama başarılıysa
                    val intent = Intent(this, PreviewActivity::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Kullanıcı henüz mail doğrulamamışsa
                    Toast.makeText(this, "E-postanızı henüz doğrulamadınız.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Eğer kullanıcı bilgisi güncellenemezse toast mesajını göster
                Toast.makeText(this, "Doğrulama kontrolü başarısız oldu.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
