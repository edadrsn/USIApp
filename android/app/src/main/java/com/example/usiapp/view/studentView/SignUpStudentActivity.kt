package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivitySignUpStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpStudentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val passwordEditText = binding.studentPassword
        val passwordAgainEditText = binding.studentPasswordAgain
        val toggleImageView = binding.ivTogglePassword
        val toggleImageView2 = binding.ivTogglePassword2

        // Şifre ve Şifre Tekrar alanları için görünürlük durumlarını kontrol ediyor
        var isPasswordVisible = false
        var isPasswordAgainVisible = false

        // Resme tıklanıdığında şifre görünürlüğünü değiştir
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

        // Şifre Tekrar alanı görünürlüğünü değiştir
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


        binding.btnSignUpStudent.setOnClickListener {
            val studentEmail = binding.studentMail.text.toString().trim()
            val studentPassword = binding.studentPassword.text.toString().trim()
            val studentPasswordAgain = binding.studentPasswordAgain.text.toString().trim()

            // Boş alan var mı
            if (studentEmail.isEmpty() || studentPassword.isEmpty() || studentPasswordAgain.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mail uzantısı doğru mu
            if (!studentEmail.endsWith("@ogr.ahievran.edu.tr")) {
                Toast.makeText(
                    this,
                    "Sadece kurumsal (@ogr.ahievran.edu.tr) mail adresi kullanılabilir",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Şifre uzunluğu
            if (studentPassword.length < 6 && studentPasswordAgain.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakterden oluşmalı!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Şifre eşleşiyor
            if (studentPassword != studentPasswordAgain) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tüm kontroller sağlanırsa kullanıcı kaydı yap
            registerUser(studentEmail, studentPassword)
        }
    }

    // Kullanıcıyı Firebase Authentication ile kaydetme
    fun registerUser(studentMail: String, studentPassword: String) {
        auth.createUserWithEmailAndPassword(studentMail, studentPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf("studentEmail" to studentMail)
                    db.collection("Students")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            startActivity(
                                Intent(
                                    this@SignUpStudentActivity,
                                    StudentMainActivity::class.java
                                )
                            )
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@SignUpStudentActivity,
                                "Firestore'a kayıt başarısız: ${it.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this@SignUpStudentActivity,
                        "Kayıt başarısız: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    //Geri dön
    fun gotoBack(view: View) {
        startActivity(Intent(this@SignUpStudentActivity, StudentLoginActivity::class.java))
    }
}
