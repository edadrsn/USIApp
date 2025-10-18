package com.example.usiapp.view.studentView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityStudentPreviewBinding
import com.example.usiapp.view.repository.StudentInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

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
        val userId = intent.getStringExtra("USER_ID")
        val uidToFetch = if (!userId.isNullOrEmpty()) userId else auth.currentUser?.uid

        if (uidToFetch.isNullOrEmpty()) {
            Toast.makeText(this, "Kullanıcı bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val studentInfo = StudentInfo(db)
        studentInfo.getStudentData(
            uidToFetch,
            onSuccess = { document ->
                if (document.exists()) {
                    val studentName = document.getString("studentName") ?: ""
                    val studentEmail = document.getString("studentEmail") ?: ""
                    val studentPhone = document.getString("studentPhone") ?: ""
                    val universityName = document.getString("universityName") ?: ""
                    val departmentName = document.getString("departmentName") ?: ""
                    val classNum = document.getString("classNumber") ?: ""

                    val getPhoto = document.getString("studentImage")
                    if (!getPhoto.isNullOrEmpty()) {
                        Picasso.get()
                            .load(getPhoto)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(binding.studentImage)
                    }

                    // adSoyad alanını boşluğa göre parçala
                    val nameParts = studentName.trim().split(" ")
                    if (nameParts.size >= 2) {
                        val surname = nameParts.last()
                        val name = nameParts.dropLast(1).joinToString(" ")
                        binding.studentFirstName.setText(name)
                        binding.studentSurname.setText(surname)
                    } else {
                        binding.studentFirstName.setText(studentName)
                        binding.studentSurname.setText("")
                    }

                    binding.studentName.setText(studentName)
                    binding.studentEmail.setText(studentEmail)
                    binding.studentMail.setText(studentEmail)
                    binding.studentPhone.setText(studentPhone)
                    binding.studentUniversityName.setText(universityName)
                    binding.studentDepartment.setText(departmentName)
                    binding.studentClass.setText(classNum)
                } else {
                    Toast.makeText(this, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun goToBack(view: View) {
        finish()
    }
}