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

    private lateinit var binding:ActivityAcademicianLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityAcademicianLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


    fun signIn(view: View){
        val sicilText = binding.sicilNo.text.toString()
        if(sicilText!=null && sicilText.length==5){
                val intent = Intent(this@AcademicianLoginActivity, AcademicianActivity::class.java)
                startActivity(intent)
        }else{
            Toast.makeText(this@AcademicianLoginActivity,"📢 Lütfen Sicil Numaranızı girin",Toast.LENGTH_SHORT).show()
        }


    }

    fun gotoBack(view:View){
        val intent=Intent(this@AcademicianLoginActivity,MainActivity::class.java)
        startActivity(intent)
    }
}