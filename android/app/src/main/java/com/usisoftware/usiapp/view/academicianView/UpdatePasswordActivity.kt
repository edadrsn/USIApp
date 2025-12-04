package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityUpdatePasswordBinding

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
            val email = binding.userMail.text.toString().trim()

            if (email.isBlank()) {
                Toast.makeText(this, "Lütfen mailinizi boş bırakmayınız!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnChangePassword.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnChangePassword.isEnabled = true

                    if (task.isSuccessful) {
                        Toast.makeText(this, "Şifre sıfırlama e-postası gönderildi", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }


    }


    //Geri dön
    fun back(view: View) {
        finish()
    }
}
