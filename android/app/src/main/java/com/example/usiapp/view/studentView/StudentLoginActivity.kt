package com.example.usiapp.view.studentView

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivitySignInStudentBinding

class SignInStudentActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySignInStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySignInStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}