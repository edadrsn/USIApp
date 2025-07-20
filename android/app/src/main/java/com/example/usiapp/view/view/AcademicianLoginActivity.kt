package com.example.usiapp.view.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianLoginBinding
import com.google.firebase.auth.FirebaseAuth

class AcademicianLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Şifre görünürlüğünü değiştirme
        var isPasswordVisible = false
        val passwordEditText = binding.academicianPassword
        val toggleImageView = binding.ivTogglePassword

        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        // Eğer kullanıcı zaten giriş yaptıysa ve mail doğrulandıysa direkt fragment sayfasına geç
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this, AcademicianMainActivity::class.java))
            finish()
        } else if (user != null && !user.isEmailVerified) {
            Toast.makeText(this, "Lütfen önce e-posta adresinizi doğrulayın.", Toast.LENGTH_LONG).show()
            FirebaseAuth.getInstance().signOut()
        }
    }

    // Giriş butonuna tıklanınca çağrılır
    fun signIn(view: View) {
        val academicianMail = binding.academicianMail.text.toString().trim()
        val academicianPassword = binding.academicianPassword.text.toString()

        // E-posta doğrulaması
        if (academicianMail.endsWith("")) {
            if (academicianPassword.length >= 6) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(academicianMail, academicianPassword)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null && user.isEmailVerified) {
                            // Eğer mail doğrulanmışsa AcademicianActivity'e geç
                            val intent = Intent(this, AcademicianMainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Lütfen e-postanızı doğrulayın.", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Kullanıcı kaydı bulunmamaktadır: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Sadece @ahievran.edu.tr uzantılı mail adresi kullanılabilir.", Toast.LENGTH_LONG).show()
        }
    }

    // Kayıt ol ekranına git
    fun gotoSignUp(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    // Ana ekrana (MainActivity) geri dön
    fun gotoBack(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
