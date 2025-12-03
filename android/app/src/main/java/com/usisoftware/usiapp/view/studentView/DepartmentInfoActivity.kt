package com.usisoftware.usiapp.view.studentView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityDepartmentInfoBinding
import com.usisoftware.usiapp.view.repository.StudentInfo

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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val uid = currentUser.uid


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
                if (isFinishing || isDestroyed) return@getStudentData

                    if (document != null && document.exists()) {
                        binding.departmentName.setText(document.getString("departmentName") ?: "")
                        val classNum = document.getString("classNumber") ?: ""
                        binding.classNumber.setText(classNum, false)
                    }

            },
            onFailure = { e ->
                Log.e("DepartmentInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })


        //Verileri kaydet
        binding.saveDepartmentInfo.setOnClickListener {
            val departmentName = binding.departmentName.text.toString().trim()
            val classNumber = binding.classNumber.text.toString().trim()

            if (departmentName.isEmpty() || classNumber.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            StudentInfo(db).updateStudentData(
                uid,
                data = hashMapOf(
                    "departmentName" to departmentName,
                    "classNumber" to classNumber
                ),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Log.e("DepartmentInfoActivity", "Firestore error:$it.localizedMessage")
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                })
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }
}