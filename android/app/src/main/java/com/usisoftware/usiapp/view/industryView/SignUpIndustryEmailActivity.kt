package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivitySignUpIndustryEmailBinding
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity

class SignUpIndustryEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpIndustryEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpIndustryEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Devam
        binding.btnForward.setOnClickListener {
            val industryMailSignUp = binding.industryMailSignUp.text.toString().trim()

            if (industryMailSignUp.isEmpty()) {
                Toast.makeText(this, "Lütfen mail alanını boş bırakmayınız!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(industryMailSignUp).matches()) {
                Toast.makeText(this, "Geçerli bir mail adresi giriniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (industryMailSignUp.lowercase().endsWith(".edu.tr")) {
                Toast.makeText(this, "Bu mail adresi ile kayıt olunamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this@SignUpIndustryEmailActivity, SignUpIndustryActivity::class.java)
            intent.putExtra("industryMailSignUp", industryMailSignUp)
            startActivity(intent)

        }

    }

    //Hesabım var
    fun haveAnAccount(view: View) {
        startActivity(Intent(this@SignUpIndustryEmailActivity, IndustryActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View) {
        startActivity(Intent(this@SignUpIndustryEmailActivity, UpdatePasswordActivity::class.java))
    }

    //Geri dön
    fun gotoBack(view: View) {
        finish()
    }
}