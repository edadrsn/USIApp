package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val passwordEditText = binding.password
        val passwordAgainEditText = binding.passwordAgain
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


        // Devam
        binding.btnGoForward.setOnClickListener {
            val academicianMail = intent.getStringExtra("academicianMailSignUp") ?: ""
            val password = binding.password.text.toString().trim()
            val passwordAgain = binding.passwordAgain.text.toString().trim()

            if (academicianMail.isEmpty()) {
                Toast.makeText(this, "E-posta alınamadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Boş alan var mı
            if (password.isEmpty() || passwordAgain.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Şifre eşleşiyor mu
            if (password != passwordAgain) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Doğrulama sayfasına geç
            val intent = Intent(this, AcademicianGetInfoActivity::class.java)
            intent.putExtra("email", academicianMail)
            intent.putExtra("password",password)
            startActivity(intent)

        }
    }


    //Bir hesabım var
    fun haveAnAccount(view: View) {
        startActivity(Intent(this@SignUpActivity, AcademicianLoginActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View) {
        startActivity(Intent(this@SignUpActivity, UpdatePasswordActivity::class.java))
    }

    //Geri git
    fun gotoBack(view: View){
        finish()
    }

}