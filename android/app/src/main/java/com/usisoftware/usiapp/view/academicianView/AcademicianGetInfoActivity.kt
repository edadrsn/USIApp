package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityAcademicianGetInfoBinding

class AcademicianGetInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianGetInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianGetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //Kayıt ol
        binding.btnSignUp.setOnClickListener {

            val academicianMail = intent.getStringExtra("email")
            val password = intent.getStringExtra("password")
            val name = binding.nameSurname.text.toString().trim()
            val faculty = binding.faculty.text.toString().trim()
            val department = binding.department.text.toString().trim()

            // NULL CHECK
            if (academicianMail.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(this, "Bir hata oluştu. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(academicianMail).matches()) {
                Toast.makeText(this, "Geçersiz email formatı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // EMPTY CHECKS
            if (name.isEmpty()) {
                Toast.makeText(this, "Lütfen isim alanını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (faculty.isEmpty()) {
                Toast.makeText(this, "Lütfen fakülte adını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (department.isEmpty()) {
                Toast.makeText(this, "Lütfen bölüm adını boş bırakmayınız!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // KULLANICIYI FİREBASE'A KAYDET
            registerUser(academicianMail, password, name, faculty, department)
        }
    }


    // Kullanıcıyı Firestore'a kaydeden fonksiyon
    private fun registerUser(
        academicianMail: String,
        password: String,
        name: String,
        faculty: String,
        department: String
    ) {
        auth.createUserWithEmailAndPassword(academicianMail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener

                    val userMap = hashMapOf(
                        "adSoyad" to name,
                        "bolum" to department,
                        "email" to academicianMail,
                        "program" to faculty
                    )

                    db.collection("Academician")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            startActivity(Intent(this, AcademicianMainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Firestore kayıt hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }

                } else {
                    Toast.makeText(this, "Kayıt başarısız: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    //Bir heasbım var
    fun haveAnAccount(view: View) {
        startActivity(Intent(this, AcademicianLoginActivity::class.java))
        finish()
    }

    //Geri git
    fun gotoBack(view: View){
        finish()
    }
}
