package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding:ActivityVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var btnVerification: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        btnVerification = findViewById(R.id.btnVerification)

        btnVerification.setOnClickListener {
            checkEmailVerification()
        }
    }


    private fun checkEmailVerification() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    Toast.makeText(this, "E-posta doğrulandı. Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show()

                    // Doğrulama başarılıysa giriş ekranına veya direkt ana sayfaya yönlendir
                    val intent = Intent(this, PreviewActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    Toast.makeText(this, "E-postanızı henüz doğrulamadınız.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Doğrulama kontrolü başarısız oldu.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
