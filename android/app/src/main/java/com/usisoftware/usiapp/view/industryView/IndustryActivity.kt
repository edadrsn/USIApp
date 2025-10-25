package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityIndustryBinding
import com.usisoftware.usiapp.view.academicianView.MainActivity
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class IndustryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setupPasswordVisibilityToggle()

        //Şifreyi unuttum sayfasına git
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, UpdatePasswordActivity::class.java))
        }
    }

    //Şifre gizle/göster fonksiyonu
    private fun setupPasswordVisibilityToggle() {
        var isPasswordVisible = false
        val passwordEditText = binding.industryPassword
        val toggleImageView = binding.ivTogglePassword

        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordEditText.transformationMethod = null
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }
    }


    //Giriş yap metodu
    fun signIn(view: View) {
        val email = binding.industryMail.text.toString().trim()
        val password = binding.industryPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
            return
        }

        if(email.endsWith("@ahievran.edu.tr") || email.endsWith("@ogr.ahievran.edu.tr")){
            Toast.makeText(this,"Lütfen geçerli bir mail adresi giriniz.",Toast.LENGTH_SHORT).show()
        }else{
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    sharedPreferences=this.getSharedPreferences("UserData", MODE_PRIVATE)
                    sharedPreferences.edit().putString("userType","industry").apply()
                    Log.d("LOGIN_PREF", "userType industry olarak kaydedildi")

                    Log.d("AUTH_DEBUG", "Email/password login başarılı, UID: ${it.user?.uid}")
                    goToMain()
                }
                .addOnFailureListener { e ->
                    Log.e("AUTH_DEBUG", "Email/password login başarısız: ${e.localizedMessage}")

                    val errorMessage = when {
                        e.localizedMessage?.contains("password is invalid") == true -> "Şifre hatalı"
                        e.localizedMessage?.contains("no user record") == true -> "Kullanıcı bulunamadı"
                        else -> "Giriş başarısız: ${e.localizedMessage}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }



    }

    private fun goToMain() {
        startActivity(Intent(this@IndustryActivity, IndustryMainActivity::class.java))
        finish()
    }

    fun signUpIndustry(view: View) {
        startActivity(Intent(this@IndustryActivity, SignUpIndustryEmailActivity::class.java))
        finish()
    }

    fun gotoBack(view: View) {
        startActivity(Intent(this@IndustryActivity, MainActivity::class.java))
        finish()
    }
}
