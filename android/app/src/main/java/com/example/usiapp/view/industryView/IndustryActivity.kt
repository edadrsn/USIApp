package com.example.usiapp.view.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityIndustryBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class IndustryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndustryBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var industryMail: EditText
    private lateinit var industryPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIndustryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        industryMail = binding.industryMail
        industryPassword = binding.industryPassword

        // Akademisyen hesabı ile bu sayfaya erişimi engelle
        /*val sharedPref = getSharedPreferences("UserType", Context.MODE_PRIVATE)
        val userType = sharedPref.getString("type", null)

        if (userType == "academician") {
            Toast.makeText(
                this,
                "Akademisyen hesabı ile sanayi girişine izin verilmiyor.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }*/

        // Eğer kullanıcı giriş yapmışsa doğrudan IndustryMainActivity'e git
        /*if (auth.currentUser != null) {
            if (userType == "industry") {
                // Doğru kullanıcı tipinde, IndustryMainActivity'e yönlendir
                val intent = Intent(this@IndustryActivity, IndustryMainActivity::class.java)
                startActivity(intent)
                finish()
                return
            } else {
                // Oturum açık ama kullanıcı tipi Industry değil, çıkış yap ve MainActivity'e dön
                auth.signOut()
                sharedPref.edit().clear().apply()
                Toast.makeText(this, "Kullanıcı tipi uyuşmuyor, lütfen tekrar giriş yapın.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@IndustryActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
        }*/


        // Şifre göster/gizle butonu ayarı
        var isPasswordVisible = false
        val passwordEditText = binding.industryPassword
        val toggleImageView = binding.ivTogglePassword

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


        // Google Sign-In yapılandırması
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Her tıklamada önce signOut → sonra signIn ekranı açılır
        binding.signInWithGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    // Google giriş ekranından dönen sonucu işle
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Firebase Authentication ile Google hesabı giriş işlemi
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Giriş başarılı → IndustryMainActivity'e geç
                        val intent = Intent(this@IndustryActivity, IndustryMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_LONG).show()
                }
            }
    }

    // E-posta/şifre ile giriş
    fun signIn(view: View) {
        val industryMail = binding.industryMail.text.toString().trim()
        val industryPassword = binding.industryPassword.text.toString()

        if (industryMail.isEmpty() || industryPassword.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            return
        }

        if (industryPassword.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(industryMail, industryPassword)
            .addOnSuccessListener {
                val intent = Intent(this@IndustryActivity, IndustryMainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Giriş başarısız: ${e.localizedMessage}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    // Kayıt ekranına geçiş
    fun signUpIndustry(view: View) {
        val intent = Intent(this@IndustryActivity, SignUpIndustryActivity::class.java)
        startActivity(intent)
    }

    // Ana ekrana dönüş
    fun gotoBack(view: View) {
        val intent = Intent(this@IndustryActivity, MainActivity::class.java)
        startActivity(intent)
    }

}
