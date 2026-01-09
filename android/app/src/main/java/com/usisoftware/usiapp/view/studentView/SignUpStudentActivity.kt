package com.usisoftware.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivitySignUpStudentBinding
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity

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

        //Öğrenciyi kaydet
        binding.btnSignUpStudent.setOnClickListener {

            val studentName = intent.getStringExtra("studentName") ?: ""
            val universityName = intent.getStringExtra("universityName") ?: ""
            val studentEmail = intent.getStringExtra("studentEmail") ?: ""

            val studentPassword = binding.studentPassword.text.toString().trim()
            val studentPasswordAgain = binding.studentPasswordAgain.text.toString().trim()

            if (studentEmail.isEmpty()) {
                Toast.makeText(this, "E-posta alınamadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(studentEmail).matches()) {
                Toast.makeText(this, "Geçersiz email!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (studentName.isEmpty()) {
                Toast.makeText(this, "Ad-Soyad alınamadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (universityName.isEmpty()) {
                Toast.makeText(this, "Üniversite alınamadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (studentPassword.isEmpty() || studentPasswordAgain.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (studentPassword.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (studentPassword != studentPasswordAgain) {
                Toast.makeText(this, "Şifreler uyuşmuyor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(studentEmail, studentPassword, studentName, universityName)

        }

    }

    // Kullanıcıyı Firebase Authentication ile kaydetme
    fun registerUser(
        studentMail: String,
        studentPassword: String,
        studentName: String,
        universityName: String
    ) {
        auth.createUserWithEmailAndPassword(studentMail, studentPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userMap = hashMapOf(
                        "studentEmail" to studentMail,
                        "studentName" to studentName,
                        "universityName" to universityName
                    )

                    db.collection("Students")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            startActivity(Intent(this, StudentMainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreSave", "Kullanıcı Firestore'a kaydedildi ancak bilgiler yazılamadı", e)

                            Toast.makeText(this, "Kayıt tamamlandı ancak bilgiler kaydedilemedi. Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("AuthRegister", "Kullanıcı kaydı başarısız", task.exception)
                    Toast.makeText(this, "Kayıt işlemi başarısız oldu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show()
                }
            }
    }


    //Geri dön
    fun gotoBack(view:View){
        finish()
    }

    //Hesabım var
    fun haveAnAccount(view: View){
        startActivity(Intent(this@SignUpStudentActivity,StudentLoginActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View){
        startActivity(Intent(this@SignUpStudentActivity, UpdatePasswordActivity::class.java))
    }
}