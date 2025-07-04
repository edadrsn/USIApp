package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianLoginBinding

class AcademicianLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


    fun signIn(view: View) {
        val academicianMail = binding.academicianMail.text.toString()
        val academicianPassword = binding.academicianPassword.text.toString()
        if (academicianMail.contains("@") || academicianPassword.contains("@ahievran.edu.tr")) {
            if (academicianPassword.length >= 6) {
                val intent = Intent(this@AcademicianLoginActivity, AcademicianActivity::class.java)
                intent.putExtra(academicianMail, "academicianMail")
                intent.putExtra(academicianPassword, "academicianPassword")
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@AcademicianLoginActivity, "📢Şifre en az 6 karakter olmalıdır.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this@AcademicianLoginActivity,
                "📢Geçersiz mail adresi.Sadece @ahievran.edu.tr uzantılı mail kullanılabilir.",
                Toast.LENGTH_LONG
            ).show()
        }


    }

    fun gotoSignUp(view: View) {
        val intent = Intent(this@AcademicianLoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    fun gotoBack(view: View) {
        val intent = Intent(this@AcademicianLoginActivity, MainActivity::class.java)
        startActivity(intent)
    }
}