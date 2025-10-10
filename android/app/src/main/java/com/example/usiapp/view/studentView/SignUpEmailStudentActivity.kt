package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivitySignUpEmailStudentBinding
import com.example.usiapp.view.academicianView.UpdatePasswordActivity

class SignUpEmailStudentActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySignUpEmailStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySignUpEmailStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Devam et
        binding.btnForward.setOnClickListener {
            val studentEmail=binding.studentMail.text.toString()

            // Boş kontrolü
            if(studentEmail.isEmpty()){
                Toast.makeText(this@SignUpEmailStudentActivity,"Lütfen mail alanını boş bırakmayınız!",
                    Toast.LENGTH_SHORT).show()
            }

            // Mail uzantısı doğru mu
            if (!studentEmail.endsWith("@ogr.ahievran.edu.tr")) {
                Toast.makeText(this, "Sadece kurumsal (@ogr.ahievran.edu.tr) mail adresi kullanılabilir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent= Intent(this@SignUpEmailStudentActivity, SignUpStudentActivity::class.java)
            intent.putExtra("studentEmail",studentEmail)
            startActivity(intent)
        }
    }

    //Hesabım var
    fun haveAnAccount(view: View){
        startActivity(Intent(this@SignUpEmailStudentActivity,StudentLoginActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View){
        startActivity(Intent(this@SignUpEmailStudentActivity,UpdatePasswordActivity::class.java))
    }
}