package com.usisoftware.usiapp.view.studentView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityStudentInfoBinding
import com.usisoftware.usiapp.view.repository.StudentInfo

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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val uid = currentUser.uid

        //Verileri Çek
        StudentInfo(db).getStudentData(
            uid,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getStudentData

                if (document != null && document.exists()) {
                    try {
                        val fullName = document.getString("studentName") ?: ""
                        val studentPhone = document.getString("studentPhone") ?: ""

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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Veri işlenirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        //Verileri kaydet
        binding.saveStudentInfo.setOnClickListener {
            val studentName = binding.studentName.text.toString().trim()
            val studentSurname = binding.studentSurname.text.toString().trim()
            val studentPhone = binding.studentPhone.text.toString().trim()


            if (studentName.isEmpty() || studentSurname.isEmpty() || studentPhone.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!studentPhone.matches(Regex("^\\d{10,13}$"))) {
                Toast.makeText(this, "Geçerli bir telefon numarası giriniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                    finish()
                },
                onFailure = {
                    Log.e("StudentInfo", "Firestore error", it)
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()

                })
        }

    }

    //Geri dön
    fun backToProfile(view: View) {
        finish()
    }

}