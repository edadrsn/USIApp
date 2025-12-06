package com.usisoftware.usiapp.view.academicianView

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

        if (user != null) {

            // Firestore’da bu UID ile bir akademisyen belgesi var mı?
            db.collection("Academician")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Akademisyen hesabı
                        startActivity(Intent(this, AcademicianMainActivity::class.java))
                        finish()
                    } else {
                        // UID var ama bu kullanıcı akademisyen değil
                        Toast.makeText(this, "Bu hesap akademisyen hesabı değil.", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Sunucu hatası, tekrar deneyin.", Toast.LENGTH_SHORT).show()
                }
        }

    }

    fun signIn(view: View) {

        val mail = binding.academicianMail.text.toString().trim()
        val password = binding.academicianPassword.text.toString()

        if (mail.isEmpty()) {
            Toast.makeText(this, "Mail boş bırakılamaz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
            return
        }

        // Domain kontrol + giriş
        checkAcademicianDomainAndLogin(mail, password)
    }

    private fun checkAcademicianDomainAndLogin(email: String, password: String) {

        val domain = email.substringAfterLast("@")

        db.collection("Authorities")
            .get()
            .addOnSuccessListener { result ->

                var isValidDomain = false

                for (doc in result.documents) {
                    val allowedDomain = doc.getString("academician") ?: continue
                    if (domain == allowedDomain) {
                        isValidDomain = true
                        break
                    }
                }

                if (!isValidDomain) {
                    Toast.makeText(this, "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Domain doğru ise giriş yap
                loginAcademician(email, password)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Sunucu hatası!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loginAcademician(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Kullanıcı tipi kaydediliyor
                    val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
                    sharedPreferences.edit().putString("userType", "academician").apply()

                    startActivity(Intent(this, AcademicianMainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "E-posta veya şifre hatalı.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    //Kayıt olma ekranına git
    fun gotoSignUp(view: View) {
        startActivity(Intent(this, SignUpEmailActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View) {
        startActivity(Intent(this@AcademicianLoginActivity, UpdatePasswordActivity::class.java))
    }

    //Ana ekrana geri dön (MainActivity)
    fun gotoBack(view: View) {
       finish()
    }

}

