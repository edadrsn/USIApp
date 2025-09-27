package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityStudentLoginBinding
import com.example.usiapp.view.academicianView.MainActivity
import com.example.usiapp.view.academicianView.UpdatePasswordActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        // Eğer kullanıcı daha önceden giriş yaptıysa ve mail doğrulandıysa direkt AcademicianMainActivity’e yönlendir
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
                        finish()

                    } else {
                        // Eğer mail doğrulanmış ama öğrenci değilse
                        Toast.makeText(this, "Bu hesap öğrenci hesabı değil.", Toast.LENGTH_SHORT).show()
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

    // Giriş yap
    fun signInStudent(view: View) {
        val studentEmail = binding.studentMail.text.toString().trim()
        val studentPassword = binding.studentPassword.text.toString()

        // E-posta boş mu?
        if (studentEmail.isEmpty()) {
            Toast.makeText(this, "Mail boş bırakılamaz", Toast.LENGTH_SHORT).show()
            return
        }

        // Mail uzantısı kontrolü
        if (!studentEmail.endsWith("@ogr.ahievran.edu.tr")) {
            Toast.makeText(this, "Sadece @ogr.ahievran.edu.tr uzantılı mail adresi kullanılabilir.", Toast.LENGTH_SHORT).show()
            return
        }

        // Şifre uzunluğu kontrolü
        if (studentPassword.length < 6) {
            Toast.makeText(this, "Yanlış şifre.Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase ile giriş
        auth.signInWithEmailAndPassword(studentEmail, studentPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        //Toast.makeText(this,"Giriş başarılı",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, StudentMainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Lütfen e-postanızın doğru olduğundan emin olun.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Kullanıcı kaydı bulunmamaktadır.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LOGIN_ERROR","Hata: ${e.localizedMessage}")
            }
    }

    //Kayıt ol sayfasına git
    fun gotoSignUp(view: View){
        startActivity(Intent(this@StudentLoginActivity,SignUpEmailStudentActivity::class.java))
    }

    //Geri dön
    fun gotoBack(view:View){
        startActivity(Intent(this@StudentLoginActivity,MainActivity::class.java))
    }
}