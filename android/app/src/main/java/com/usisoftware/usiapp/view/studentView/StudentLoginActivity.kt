package com.usisoftware.usiapp.view.studentView

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityStudentLoginBinding
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity

class StudentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityStudentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Şifre görünürlüğünü değiştirme işlemi
        var isPasswordVisible = false
        val passwordEditText = binding.studentPassword
        val toggleImageView = binding.ivTogglePassword

        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Şifre açık şekilde gösterilir
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                // Şifre gizli şekilde gösterilir
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        // Eğer kullanıcı daha önceden giriş yaptıysa ve mail doğrulandıysa direkt yönlendir
        val user = auth.currentUser

        if (user != null) {
            val email = user.email ?: ""
            db.collection("Students") // Students koleksiyonunda öğrenci aranıyor
                .whereEqualTo("studentEmail", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Student verisi varsa girişe izin veriliyor
                        startActivity(Intent(this, StudentMainActivity::class.java))

                    } else {
                        // Eğer mail doğrulanmış ama öğrenci değilse
                        Toast.makeText(this, "Bu hesap öğrenci hesabı değil!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Firestore'dan veri alınamazsa hata gösterilir
                    Toast.makeText(this, "Sunucu hatası, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                }
        }

        //Şifreyi unuttum sayfasına git
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, UpdatePasswordActivity::class.java))
        }
    }

    fun signInStudent(view: View) {
        val studentEmail = binding.studentMail.text.toString().trim()
        val studentPassword = binding.studentPassword.text.toString()

        if (studentEmail.isEmpty()) {
            Toast.makeText(this, "Mail boş bırakılamaz", Toast.LENGTH_SHORT).show()
            return
        }

        if (studentPassword.length < 6) {
            Toast.makeText(this, "Yanlış şifre. Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
            return
        }

        loginStudent(studentEmail, studentPassword)
    }

    private fun loginStudent(studentEmail: String, studentPassword: String) {

        auth.signInWithEmailAndPassword(studentEmail, studentPassword)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // Giriş başarılı → Students koleksiyonunda var mı kontrol et
                    db.collection("Students")
                        .whereEqualTo("studentEmail", studentEmail)
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {
                                // Öğrenci gerçekten kayıtlı
                                sharedPreferences =
                                    getSharedPreferences("UserData", MODE_PRIVATE)

                                sharedPreferences.edit()
                                    .putString("userType", "student")
                                    .apply()

                                startActivity(Intent(this, StudentMainActivity::class.java))
                                finish()

                            } else {
                                // Auth var ama öğrenci değil
                                auth.signOut()
                                Toast.makeText(this, "Bu e-posta ile kayıtlı öğrenci bulunamadı.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            auth.signOut()
                            Toast.makeText(this, "Sunucu hatası, lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    Toast.makeText(this, "E-posta veya şifre hatalı.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Kayıt ol sayfasına git
    fun gotoSignUp(view: View){
        startActivity(Intent(this@StudentLoginActivity,SignUpEmailStudentActivity::class.java))
    }

    //Geri dön
    fun gotoBack(view:View){
        finish()
    }

}