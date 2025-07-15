package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianLoginBinding
import com.example.usiapp.view.view.AcademicianActivity
import com.example.usiapp.view.view.MainActivity
import com.google.firebase.auth.FirebaseAuth

class AcademicianLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Şifre görünürlüğünü değiştirmek için kullandım
        var isPasswordVisible = false
        val passwordEditText = binding.academicianPassword
        val toggleImageView = binding.ivTogglePassword

        // Toggle butonuna tıklanınca şifre görünürlüğü değişsin
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



        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            if (!user.isEmailVerified) {
                Toast.makeText(this, "Lütfen önce e-posta adresinizi doğrulayın.", Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
            } else {
                // Mail doğrulanmışsa direkt anasayfaya geç
                val intent = Intent(this, AcademicianActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    // Giriş yap
    fun signIn(view: View) {
        val academicianMail = binding.academicianMail.text.toString().trim()
        val academicianPassword = binding.academicianPassword.text.toString()


        if (academicianMail.endsWith("")) {
            if (academicianPassword.length >= 6) {
                // Firebase giriş yap
                FirebaseAuth.getInstance().signInWithEmailAndPassword(academicianMail, academicianPassword)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null && user.isEmailVerified) {
                            val intent = Intent(this, AcademicianActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Lütfen e-postanızı doğrulayın.", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Giriş hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }

            } else {
                Toast.makeText(this, "📢 Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                this,
                "📢 Geçersiz mail adresi. Sadece @ahievran.edu.tr uzantılı mail kullanılabilir.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // Kayıt ol ekranına git
    fun gotoSignUp(view: View) {
        val intent = Intent(this@AcademicianLoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }


    // Ana ekrana geri dön
    fun gotoBack(view: View) {
        val intent = Intent(this@AcademicianLoginActivity, MainActivity::class.java)
        startActivity(intent)
    }
}
