package com.example.usiapp.view.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivitySignUpIndustryBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUpIndustryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpIndustryBinding

    // Google Sign-In için gerekli değişkenler
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1

    // Firebase Firestore ve Authentication
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // E-posta ve şifre alanları
    private lateinit var industryMailSignUp: EditText
    private lateinit var industryPasswordSignUp: EditText
    private lateinit var industryPasswordSignUp2: EditText
    private lateinit var btnSignUpIndustry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpIndustryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase başlatılıyor
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // View'lar tanımlanıyor
        btnSignUpIndustry = binding.btnSignUpIndustry
        industryMailSignUp = binding.industryMailSignUp
        industryPasswordSignUp = binding.industryPasswordSignUp
        industryPasswordSignUp2 = binding.industryPasswordSignUp2
        val toggleImageView = binding.ivTogglePassword
        val toggleImageView2 = binding.ivTogglePassword2

        // Şifre görünürlük kontrolleri
        var isPasswordVisible = false
        var isPasswordAgainVisible = false

        // Şifre görünürlüğünü değiştir
        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                industryPasswordSignUp.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                industryPasswordSignUp.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            // İmleci sona taşı
            industryPasswordSignUp.setSelection(industryPasswordSignUp.text?.length ?: 0)
        }

        toggleImageView2.setOnClickListener {
            isPasswordAgainVisible = !isPasswordAgainVisible

            if (isPasswordAgainVisible) {
                industryPasswordSignUp2.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                industryPasswordSignUp2.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            industryPasswordSignUp2.setSelection(industryPasswordSignUp2.text?.length ?: 0)
        }

        // Daha önce giriş yapılmışsa Google hesabından çık
        if (auth.currentUser != null) {
            googleSignInClient.signOut() // Otomatik giriş engellenir
        }

        // Google Sign-In yapılandırması
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Firebase web client ID
            .requestEmail()
            .build()

        // GoogleSignInClient oluşturuluyor
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google ile kayıt butonuna tıklandığında giriş ekranını başlat
        binding.signUpGoToGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    // Google Sign-In sonrası sonuç burada alınır
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Google hesabıyla Firebase Authentication işlemi
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener

                    // Firestore'a kullanıcı e-posta bilgisini kaydet
                    val userMap = hashMapOf("email" to user.email)

                    db.collection("Industry").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {

                            // Ana sayfaya yönlendir
                            val intent = Intent(this, IndustryMainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Kayıt başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Firebase ile giriş başarısız", Toast.LENGTH_LONG).show()
                }
            }
    }


    // E-posta/şifre ile kayıt butonuna tıklandığında çağrılır
    fun signUp(view: View) {
        val email = binding.industryMailSignUp.text.toString()
        val password = binding.industryPasswordSignUp.text.toString()
        val passwordAgain = binding.industryPasswordSignUp2.text.toString()

        if (email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
            Toast.makeText(this@SignUpIndustryActivity, "Lütfen tüm alanları doldurun !", Toast.LENGTH_LONG).show()
        }
        if (password != passwordAgain) {
            Toast.makeText(this@SignUpIndustryActivity, "Şifreler uyuşmuyor !", Toast.LENGTH_LONG)
                .show()
        }

        // Her şey doğruysa kullanıcı kaydı yapılır
        registerUser(email, password)
    }

    // E-posta ve şifre ile Firebase Authentication ve Firestore'a kayıt
    fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener

                    val userMap = hashMapOf("email" to email)

                    db.collection("Industry")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            val intent = Intent(this@SignUpIndustryActivity, IndustryMainActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@SignUpIndustryActivity, "Firestore'a kayıt başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this@SignUpIndustryActivity, "Kayıt başarısız: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }



    // Geri butonuna basıldığında MainActivity'e dön
    fun gotoBack(view: View) {
        val intent = Intent(this@SignUpIndustryActivity, IndustryActivity::class.java)
        startActivity(intent)
    }
}
