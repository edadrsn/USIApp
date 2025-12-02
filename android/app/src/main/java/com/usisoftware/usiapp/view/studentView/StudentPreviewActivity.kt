package com.usisoftware.usiapp.view.studentView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityStudentPreviewBinding
import com.usisoftware.usiapp.view.repository.StudentInfo
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class StudentPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentPreviewBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStudentPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val uidToFetch: String? = intent.getStringExtra("USER_ID") ?: auth.currentUser?.uid
        if (uidToFetch.isNullOrBlank()) {
            Toast.makeText(this, "Kullanıcı bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Sayfa açılır açılmaz veri çek
        fetchStudentData(uidToFetch)

        // Swipe ile yenile
        binding.swipeRefreshLayout.setOnRefreshListener {
            uidToFetch.let { uid ->
                fetchStudentData(uid)
            }
        }

    }

    //Verileri çek
    private fun fetchStudentData(uid: String) {
        val studentInfo = StudentInfo(db)
        studentInfo.getStudentData(
            uid,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getStudentData

                if (document != null && document.exists()) {
                    val studentName = document.getString("studentName") ?: ""
                    val studentEmail = document.getString("studentEmail") ?: ""
                    val studentPhone = document.getString("studentPhone") ?: ""
                    val universityName = document.getString("universityName") ?: ""
                    val departmentName = document.getString("departmentName") ?: ""
                    val classNum = document.getString("classNumber") ?: ""
                    val getPhoto = document.getString("studentImage")

                    if (!getPhoto.isNullOrEmpty()) {
                        loadImageWithCorrectRotation(
                            this@StudentPreviewActivity,
                            getPhoto,
                            binding.studentImage,
                            R.drawable.person
                        )
                    } else {
                        binding.studentImage.setImageResource(R.drawable.person)
                    }

                    binding.studentName.setText(studentName)
                    binding.studentMail.setText(studentEmail)
                    binding.studentPhone.setText(studentPhone)
                    binding.studentUniversityName.setText(universityName)
                    binding.studentDepartment.setText(departmentName)
                    binding.studentClass.setText(classNum)

                    // Ad soyad ayrımı
                    val nameParts = studentName.trim().split(" ")
                    if (nameParts.size >= 2) {
                        binding.studentFirstName.setText(nameParts.dropLast(1).joinToString(" "))
                        binding.studentSurname.setText(nameParts.last())
                    } else {
                        binding.studentFirstName.setText(studentName)
                        binding.studentSurname.setText("")
                    }
                } else {
                    Toast.makeText(this, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                }
                binding.swipeRefreshLayout.isRefreshing = false
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        )
    }

    //Geri dön
    fun goToBack(view: View) {
        finish()
    }
}