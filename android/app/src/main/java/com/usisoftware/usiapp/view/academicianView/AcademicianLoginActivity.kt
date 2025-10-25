package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityAcademicianLoginBinding

class AcademicianLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianLoginBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Şifre görünürlüğünü değiştirme işlemi
        var isPasswordVisible = false
        val passwordEditText = binding.academicianPassword
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

        if (user != null && user.isEmailVerified) {
            val email = user.email ?: ""
            db.collection("AcademicianInfo") // Akademisyen koleksiyonunda kullanıcı aranıyor
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Akademisyen verisi varsa girişe izin veriliyor
                        startActivity(Intent(this, AcademicianMainActivity::class.java))
                        finish()

                    } else {
                        // Eğer mail doğrulanmış ama akademisyen değilse
                        Toast.makeText(
                            this,
                            "Bu hesap akademisyen hesabı değil.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Firestore'dan veri alınamazsa hata gösterilir
                    Toast.makeText(
                        this,
                        "Sunucu hatası, lütfen tekrar deneyin.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else if (user != null && !user.isEmailVerified) {
            Toast.makeText(this, "Lütfen önce e-posta adresinizi doğrulayın.", Toast.LENGTH_SHORT)
                .show()
            auth.signOut()
        }


        //Şifreyi unuttum sayfasına git
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this,UpdatePasswordActivity::class.java))
        }
    }


    // Kullanıcı giriş butonuna bastığında çalışan metod
    fun signIn(view: View) {
        val academicianMail = binding.academicianMail.text.toString().trim()
        val academicianPassword = binding.academicianPassword.text.toString()

        // E-posta boş mu?
        if (academicianMail.isEmpty()) {
            Toast.makeText(this, "Mail boş bırakılamaz", Toast.LENGTH_SHORT).show()
            return
        }

        // Mail uzantısı kontrolü
        if (!academicianMail.endsWith("")) {
            Toast.makeText(
                this,
                "Sadece @ahievran.edu.tr uzantılı mail adresi kullanılabilir.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Şifre uzunluğu kontrolü
        if (academicianPassword.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase ile giriş
        auth.signInWithEmailAndPassword(academicianMail, academicianPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sharedPreferences=this.getSharedPreferences("UserData", MODE_PRIVATE)
                    sharedPreferences.edit().putString("userType","academician").apply()
                    Log.d("LOGIN_PREF", "userType academician olarak kaydedildi")

                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        startActivity(Intent(this, AcademicianMainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Lütfen e-postanızı doğrulayın.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Kullanıcı kaydı bulunmamaktadır.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LOGIN_ERROR", "Hata: ${e.localizedMessage}")
            }
    }


    //Kayıt olma ekranına git
    fun gotoSignUp(view: View) {
        startActivity(Intent(this, SignUpEmailActivity::class.java))
    }

    //Ana ekrana geri dön (MainActivity)
    fun gotoBack(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}

