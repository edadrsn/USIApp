package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Göster/Gizle
        var isPasswordVisible = false
        var isPasswordAgainVisible = false

        val passwordEditText = binding.password
        val passwordAgainEditText = binding.passwordAgain
        val toggleImageView = binding.ivTogglePassword
        val toggleImageView2 = binding.ivTogglePassword2

        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        toggleImageView2.setOnClickListener {
            isPasswordAgainVisible = !isPasswordAgainVisible

            if (isPasswordAgainVisible) {
                passwordAgainEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordAgainEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            passwordAgainEditText.setSelection(passwordAgainEditText.text?.length ?: 0)
        }


        auth = Firebase.auth
        binding.btnSignUp.setOnClickListener {
            val uniMail = binding.uniMail.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val passwordAgain = binding.passwordAgain.text.toString().trim()

            // 1. Boş alan kontrolü
            if (uniMail.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Mail uzantısı kontrolü (kurumsal mail)
            if (!uniMail.endsWith("@gmail.com")) {
                Toast.makeText(
                    this,
                    "Sadece kurumsal (@ahievran.edu.tr) mail adresi kullanılabilir",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 3. Şifre eşleşme kontrolü
            if (password != passwordAgain) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kayıt işlemi
            registerUser(uniMail, password)
        }


    }

    private fun registerUser(uniMail: String, password: String) {
        auth.createUserWithEmailAndPassword(uniMail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Doğrulama maili gönder
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Doğrulama maili gönderildi: $uniMail",
                                Toast.LENGTH_LONG
                            ).show()

                            // Doğrulama ekranına yönlendir
                            val intent = Intent(this, VerificationActivity::class.java)
                            intent.putExtra("email", uniMail) // Gerekirse e-posta gönder
                            startActivity(intent)
                            finish()

                        } else {
                            Toast.makeText(
                                this,
                                "Doğrulama maili gönderilemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Kayıt başarısız: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    fun gotoLogin(view: View) {
        val intent = Intent(this@SignUpActivity, AcademicianLoginActivity::class.java)
        startActivity(intent)
    }
}