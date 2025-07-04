package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        /*val currentUser = auth.currentUser
        if (currentUser != null) { //Güncel kullanıcı varsa
            val intent = Intent(this@SignUpActivity, AcademicianActivity::class.java)
            startActivity(intent)
            finish()
        }*/


        //Göster/Gizle
        var isPasswordVisible = false
        var isPasswordAgainVisible = false

        val passwordEditText = binding.password
        val passwordAgainEditText = binding.passwordAgain
        val toggleImageView = binding.ivTogglePassword
        val toggleImageView2 = binding.ivTogglePassword2

        toggleImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        toggleImageView2.setOnClickListener {
            isPasswordAgainVisible = !isPasswordAgainVisible

            if (isPasswordAgainVisible) {
                passwordAgainEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordAgainEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleImageView2.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            passwordAgainEditText.setSelection(passwordAgainEditText.text?.length ?: 0)
        }





    }

    fun signUp(view:View){
        val uniMail = binding.uniMail.text.toString()
        val password=binding.password.text.toString()
        val passwordAgain=binding.passwordAgain.text.toString()
        if(!uniMail.contains("@") || !uniMail.contains("@ahievran.edu.tr")){
            Toast.makeText(this@SignUpActivity,"📢Geçersiz mail adresi.Sadece @ahievran.edu.tr uzantılı mail kullanılabilir.",
                Toast.LENGTH_LONG).show()
        }
        if(password.length<6){
            Toast.makeText(this@SignUpActivity,"📢Şifre en az 6 karakter olmalıdır.",
                Toast.LENGTH_LONG).show()
        }
        if(password!=passwordAgain){
            Toast.makeText(this@SignUpActivity,"📢Şifreler uyuşmuyor.",
                Toast.LENGTH_LONG).show()
        }

        auth.createUserWithEmailAndPassword(uniMail,password).addOnSuccessListener{
            val intent = Intent(this@SignUpActivity, AcademicianActivity::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(
                this@SignUpActivity,
                it.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    fun gotoLogin(view: View){
        val intent= Intent(this@SignUpActivity,AcademicianLoginActivity::class.java)
        startActivity(intent)
    }
}