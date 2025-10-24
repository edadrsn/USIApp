package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivitySignUpEmailBinding

class SignUpEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Devam et
        binding.btnForward.setOnClickListener {
            val uniMail = binding.uniMail.text.toString()

            // Boş kontrolü
            if (uniMail.isEmpty()) {

                Toast.makeText(this@SignUpEmailActivity, "Lütfen mail alanını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
            }
            else if (!uniMail.endsWith("")) {
                Toast.makeText(this, "Sadece kurumsal (@ahievran.edu.tr) mail adresi kullanılabilir", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this@SignUpEmailActivity, SignUpActivity::class.java)
                intent.putExtra("uniMail", uniMail)
                startActivity(intent)
            }
        }
    }

    //Hesabım var
    fun haveAnAccount(view: View) {
        startActivity(Intent(this@SignUpEmailActivity, AcademicianLoginActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View) {
        startActivity(Intent(this@SignUpEmailActivity, UpdatePasswordActivity::class.java))
    }

}