package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivitySignUpEmailBinding


class SignUpEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailBinding
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Devam
        binding.btnGoForward.setOnClickListener {

            val uniMail = binding.uniMail.text.toString().trim()

            if (uniMail.isEmpty()) {
                Toast.makeText(this, "Lütfen mail alanını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(uniMail).matches()) {
                Toast.makeText(this, "Geçersiz email!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val domain = uniMail.substringAfterLast("@")

            // Authorities academician domain kontrolü
            db.collection("Authorities")
                .get()
                .addOnSuccessListener { result ->

                    var isValidDomain = false

                    for (doc in result.documents) {
                        val academicianDomain = doc.getString("academician") ?: continue
                        if (academicianDomain == domain) {
                            isValidDomain = true
                            break
                        }
                    }

                    if (!isValidDomain) {
                        Toast.makeText(this, "Bu mail adresi geçerli değil!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Geçerli ise kayıt sayfasına gönder
                    val intent = Intent(this, SignUpActivity::class.java)
                    intent.putExtra("academicianMailSignUp", uniMail)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Sunucu hatası!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //Bir hesabım var
    fun haveAnAccount(view: View) {
        startActivity(Intent(this@SignUpEmailActivity, AcademicianLoginActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View) {
        startActivity(Intent(this@SignUpEmailActivity, UpdatePasswordActivity::class.java))
    }

    //Geri git
    fun gotoBack(view: View) {
        finish()
    }
}
