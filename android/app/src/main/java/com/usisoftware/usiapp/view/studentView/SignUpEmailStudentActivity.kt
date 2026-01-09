package com.usisoftware.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivitySignUpEmailStudentBinding
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity

class SignUpEmailStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpEmailStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Devam et
        binding.btnForward.setOnClickListener {

            val studentEmail = binding.studentMail.text.toString().trim()

            if (studentEmail.isEmpty()) {
                Toast.makeText(this, "Lütfen mail alanını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(studentEmail).matches()) {
                Toast.makeText(
                    this,
                    "Geçersiz email!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Geçerliyse direkt ilerle
            val intent = Intent(this@SignUpEmailStudentActivity, StudentLoginInfoActivity::class.java)
            intent.putExtra("studentEmail", studentEmail)
            startActivity(intent)
        }
    }

    // Hesabım var
    fun haveAnAccount(view: View) {
        startActivity(Intent(this@SignUpEmailStudentActivity, StudentLoginActivity::class.java))
    }

    // Şifremi unuttum
    fun forgotPassword(view: View) {
        startActivity(Intent(this@SignUpEmailStudentActivity, UpdatePasswordActivity::class.java))
    }

    // Geri dön
    fun gotoBack(view: View) {
        finish()
    }
}
