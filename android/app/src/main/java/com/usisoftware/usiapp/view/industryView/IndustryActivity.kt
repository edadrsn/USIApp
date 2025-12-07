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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityIndustryBinding
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity


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
            val uid = auth.currentUser!!.uid

            db.collection("Industry").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        goToMain()
                    } else {
                        auth.signOut()
                    }
                }
            return
        }
        
        setupPasswordVisibilityToggle()

        //Şifreyi unuttum sayfasına git
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, UpdatePasswordActivity::class.java))
        }
    }

    // Giriş yap metodu
    fun signIn(view: View) {
        val email = binding.industryMail.text.toString().trim()
        val password = binding.industryPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid

                db.collection("Industry").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {

                            // Giriş başarılı → Industry kullanıcısı
                            sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
                            sharedPreferences.edit().putString("userType", "industry").apply()

                            Log.d("LOGIN_DEBUG", "Industry girişi başarılı. UID: $uid")
                            goToMain()

                        } else {
                            // Industry koleksiyonunda yok → çıkış yap
                            Log.e("LOGIN_DEBUG", "Industry koleksiyonunda kullanıcı bulunamadı. UID: $uid")

                            auth.signOut()
                            Toast.makeText(this, "Bu hesap bulunamadı!", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIRESTORE_DEBUG", "Industry kontrolü sırasında hata: ${e.localizedMessage}")
                        Toast.makeText(this, "Bağlantı hatası. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AUTH_DEBUG", "Giriş başarısız: ${e.localizedMessage}")

                val errorMessage = when {
                    e.localizedMessage?.contains("password is invalid") == true ->
                        "Şifre hatalı."

                    e.localizedMessage?.contains("no user record") == true ->
                        "Bu e-posta ile kayıtlı hesap bulunamadı."

                    e.localizedMessage?.contains("network error") == true ->
                        "İnternet bağlantısı yok. Lütfen tekrar deneyin."

                    else ->
                        "Giriş yapılamadı. Bilgilerinizi kontrol edin."
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
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

    //IndustryMain sayfasına git
    private fun goToMain() {
        startActivity(Intent(this@IndustryActivity, IndustryMainActivity::class.java))
        finish()
    }

    //SignUpIndustryEmailActivity sayfasına git
    fun signUpIndustry(view: View) {
        startActivity(Intent(this@IndustryActivity, SignUpIndustryEmailActivity::class.java))
        finish()
    }

    //Geri dön
    fun gotoBack(view:View){
        finish()
    }
}
