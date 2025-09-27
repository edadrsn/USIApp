package com.example.usiapp.view.academicianView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityUpdatePasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UpdatePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePasswordBinding

    private lateinit var db:FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db=FirebaseFirestore.getInstance()
        auth=FirebaseAuth.getInstance()

        binding.btnChangePassword.setOnClickListener {
            val email = binding.userMail.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this@UpdatePasswordActivity, "Lütfen mailinizi boş bırakmayınız!", Toast.LENGTH_SHORT).show()
            }else{
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Şifre sıfırlama e-postası gönderildi", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }

            }
        }
    }


    //Geri dön
    fun back(view: View) {
        finish()
    }
}