package com.example.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivitySignUpIndustryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SignUpIndustryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpIndustryBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var industryMailSignUp: EditText
    private lateinit var industryPasswordSignUp: EditText
    private lateinit var industryPasswordSignUp2: EditText
    private lateinit var btnSignUpIndustry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpIndustryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        btnSignUpIndustry = binding.btnSignUpIndustry
        industryMailSignUp = binding.industryMailSignUp
        industryPasswordSignUp = binding.industryPasswordSignUp
        industryPasswordSignUp2 = binding.industryPasswordSignUp2
        val toggleImageView = binding.ivTogglePassword
        val toggleImageView2 = binding.ivTogglePassword2

        var isPasswordVisible = false
        var isPasswordAgainVisible = false

        // Şifre görünürlüğünü değiştir
        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                industryPasswordSignUp.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                industryPasswordSignUp.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            // İmleci sona taşı
            industryPasswordSignUp.setSelection(industryPasswordSignUp.text?.length ?: 0)
        }

        toggleImageView2.setOnClickListener {
            isPasswordAgainVisible = !isPasswordAgainVisible

            if (isPasswordAgainVisible) {
                industryPasswordSignUp2.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                industryPasswordSignUp2.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            industryPasswordSignUp2.setSelection(industryPasswordSignUp2.text?.length ?: 0)
        }
    }

    // E-posta/şifre ile kayıt butonuna tıklandığında çağrılır
    fun signUp(view: View) {
        val email = binding.industryMailSignUp.text.toString()
        val password = binding.industryPasswordSignUp.text.toString()
        val passwordAgain = binding.industryPasswordSignUp2.text.toString()

        if (email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_LONG).show()
            return
        }

        if (password != passwordAgain) {
            Toast.makeText(this, "Şifreler uyuşmuyor!", Toast.LENGTH_LONG).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalı!", Toast.LENGTH_LONG).show()
            return
        }

        // Her şey doğruysa kullanıcı kaydı yapılır
        registerUser(email, password)
    }

    // E-posta ve şifre ile Firebase Authentication ve Firestore'a kayıt
    fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf("email" to email)
                    db.collection("Industry")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            val intent = Intent(this@SignUpIndustryActivity, IndustryMainActivity::class.java)
                            startActivity(intent)
                            finish()

                            user?.let {
                                val email = it.email ?: ""
                                val emailDomain = email.substringAfterLast("@")
                                val userDoc = hashMapOf(
                                    "uid" to user.uid,
                                    "email" to email,
                                    "domain" to emailDomain
                                )
                                db.collection("UserDomains").document(user.uid).set(userDoc)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@SignUpIndustryActivity, "Firestore'a kayıt başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this@SignUpIndustryActivity, "Kayıt başarısız: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Geri dön
    fun gotoBack(view: View) {
        val intent = Intent(this@SignUpIndustryActivity, IndustryActivity::class.java)
        startActivity(intent)
    }
}
