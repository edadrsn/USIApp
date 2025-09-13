package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityDepartmentInfoBinding
import com.example.usiapp.view.repository.StudentInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DepartmentInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepartmentInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDepartmentInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        // Sınıf seçeneklerini tanımla
        val siniflar = listOf(
            "1. Sınıf",
            "2. Sınıf",
            "3. Sınıf",
            "4. Sınıf",
            "Mezun",
        )

        // DropDown için adapter tanımladım
        val adapter = ArrayAdapter(
            this@DepartmentInfoActivity,
            android.R.layout.simple_dropdown_item_1line,
            siniflar
        )
        val dropdown = binding.classNumber
        dropdown.setAdapter(adapter)
        dropdown.setOnClickListener { dropdown.showDropDown() }

        //Verileri Çek
        StudentInfo(db).getStudentData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    binding.departmentName.setText(document.getString("departmentName") ?: "")
                    val classNum = document.getString("classNumber") ?: ""
                    binding.classNumber.setText(classNum, false)
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })


        //Verileri kaydet
        binding.saveDepartmentInfo.setOnClickListener {
            val departmentName = binding.departmentName.text.toString()
            val classNumber = binding.classNumber.text.toString()

            if (departmentName.isEmpty() || classNumber.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
            }

            StudentInfo(db).updateStudentData(
                uid,
                data = hashMapOf(
                    "departmentName" to departmentName,
                    "classNumber" to classNumber
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