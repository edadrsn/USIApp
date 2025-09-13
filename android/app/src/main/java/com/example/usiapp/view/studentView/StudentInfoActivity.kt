package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityStudentInfoBinding
import com.example.usiapp.view.repository.StudentInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStudentInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        //Verileri Çek
        StudentInfo(db).getStudentData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("studentName") ?: ""
                    val studentPhone=document.getString("studentPhone") ?: ""

                    // adSoyad alanını boşluğa göre parçala
                    val nameParts = fullName.trim().split(" ")
                    if (nameParts.size >= 2) {
                        val surname = nameParts.last()
                        val name = nameParts.dropLast(1).joinToString(" ")

                        binding.studentName.setText(name)
                        binding.studentSurname.setText(surname)
                        binding.studentPhone.setText(studentPhone)
                    } else {
                        binding.studentName.setText(fullName)
                        binding.studentSurname.setText("")
                    }
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })


        //Verileri kaydet
        binding.saveStudentInfo.setOnClickListener {
            val studentName = binding.studentName.text.toString()
            val studentSurname = binding.studentSurname.text.toString()
            val studentPhone=binding.studentPhone.text.toString()


            if (studentName.isEmpty() || studentSurname.isEmpty() ||studentPhone.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
            }

            val fullName = "$studentName $studentSurname"

            StudentInfo(db).updateStudentData(
                uid,
                data = hashMapOf(
                    "studentName" to fullName,
                    "studentPhone" to studentPhone
                ),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, StudentMainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                })
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }

}