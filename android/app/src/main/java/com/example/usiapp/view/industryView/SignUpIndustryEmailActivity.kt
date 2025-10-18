package com.example.usiapp.view.industryView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivitySignUpIndustryEmailBinding
import com.example.usiapp.view.academicianView.UpdatePasswordActivity

class SignUpIndustryEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpIndustryEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpIndustryEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Devam
        binding.btnForward.setOnClickListener {
            val industryMailSignUp = binding.industryMailSignUp.text.toString()

            if (industryMailSignUp.isEmpty()) {
                Toast.makeText(this@SignUpIndustryEmailActivity, "Lütfen mail alanını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this@SignUpIndustryEmailActivity, SignUpIndustryActivity::class.java)
                intent.putExtra("industryMailSignUp", industryMailSignUp)
                startActivity(intent)
            }
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
}