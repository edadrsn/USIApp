package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AcademicianLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianLoginBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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
            if (user.isEmailVerified) {
                Log.d("LOGIN_FLOW", "Giriş yapılmış ve mail doğrulanmış, Firestore kontrolü başlıyor")
                val email = user.email ?: ""
                db.collection("AcademicianInfo") // Akademisyen koleksiyonunda kullanıcı aranıyor
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            // Akademisyen verisi varsa girişe izin veriliyor
                            val intent = Intent(this, AcademicianMainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            // Eğer mail doğrulanmış ama akademisyen değilse
                            Toast.makeText(this, "Bu hesap akademisyen hesabı değil.", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Firestore'dan veri alınamazsa hata gösterilir
                        Log.e("FIREBASE", "Academician kontrolü başarısız: ${e.localizedMessage}")
                        Toast.makeText(this, "Veri alınamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Log.d("LOGIN_FLOW", "Mail doğrulanmamış, kullanıcı çıkış yapılıyor")
                // E-posta doğrulanmamışsa kullanıcı çıkış yapılır
                Toast.makeText(this, "Lütfen önce e-posta adresinizi doğrulayın.", Toast.LENGTH_LONG).show()
                auth.signOut()
            }
        }else {
            Log.d("LOGIN_FLOW", "Kullanıcı null, login ekranı gösteriliyor")
        }
    }

    // Kullanıcı giriş butonuna bastığında çalışan metod
    fun signIn(view: View) {
        val academicianMail = binding.academicianMail.text.toString().trim()
        val academicianPassword = binding.academicianPassword.text.toString()

        // E-posta boş değilse
        if (academicianMail.isNotEmpty()) {
            if (academicianMail.endsWith("@ahievran.edu.tr")) {
                if (academicianPassword.length >= 6) {
                    // Firebase ile giriş yapılır
                    auth.signInWithEmailAndPassword(academicianMail, academicianPassword)
                        .addOnCompleteListener { authResult ->
                            val user =  FirebaseAuth.getInstance().currentUser
                            Log.d("LOGIN_FLOW", "Login başarılı - user: ${user?.email}")
                            if (user != null && user.isEmailVerified) {
                                // Akademisyen ana sayfasına yönlendirilir
                                val intent = Intent(this, AcademicianMainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Giriş başarılı ama mail doğrulanmamış
                                Toast.makeText(this, "Lütfen e-postanızı doğrulayın.", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            // Giriş başarısızsa kullanıcıya göster
                            Toast.makeText(this, "Kullanıcı kaydı bulunmamaktadır: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Şifre uzunluğu yetersizse
                    Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_LONG).show()
                }
            } else {
                // Mail uzantısı kontrolü eksik veya boş
                Toast.makeText(this, "Sadece @ahievran.edu.tr uzantılı mail adresi kullanılabilir.", Toast.LENGTH_LONG).show()
            }
        } else {
            // E-posta boşsa uyarı ver
            Toast.makeText(this, "Mail boş bırakılamaz", Toast.LENGTH_LONG).show()
        }
    }

    //Kayıt olma ekranına git
    fun gotoSignUp(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    //Ana ekrana geri dön (MainActivity)
    fun gotoBack(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}

