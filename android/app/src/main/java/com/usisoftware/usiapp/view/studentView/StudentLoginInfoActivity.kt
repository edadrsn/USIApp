package com.usisoftware.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityStudentLoginInfoBinding
import com.usisoftware.usiapp.view.academicianView.UpdatePasswordActivity

class StudentLoginInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLoginInfoBinding
    private lateinit var db: FirebaseFirestore

    private val universityList = mutableListOf<String>()
    private lateinit var studentEmail: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityStudentLoginInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        studentEmail = intent.getStringExtra("studentEmail") ?: ""

        fetchUniversities()

        binding.btnForward.setOnClickListener {
            goForward()
        }
    }

    //Firestore'dan üniversiteleri çek

    private fun fetchUniversities() {
        try {
            db.collection("Authorities")
                .get()
                .addOnSuccessListener { result ->
                    universityList.clear()

                    for (doc in result.documents) {
                        val uniName = doc.getString("universityName")
                        if (!uniName.isNullOrEmpty()) {
                            universityList.add(uniName.uppercase())
                        }
                    }

                    Log.d("UniversityFetch", "Çekilen üniversite sayısı: ${universityList.size}")

                    setupAutoComplete()
                }
                .addOnFailureListener { e ->
                    Log.e("UniversityFetch", "Firestore hata", e)
                    Toast.makeText(this, "Üniversiteler yüklenemedi", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("UniversityFetch", "Beklenmeyen hata", e)
        }
    }

    private fun setupAutoComplete() {
        try {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                universityList
            )

            binding.universityName.setAdapter(adapter)
            binding.universityName.threshold = 1

            // Tıklanınca dropdown açılsın
            binding.universityName.setOnClickListener {
                binding.universityName.showDropDown()
            }

            // Küçük harf → büyük harf
            binding.universityName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    binding.universityName.removeTextChangedListener(this)
                    s?.let {
                        val upper = it.toString().uppercase()
                        binding.universityName.setText(upper)
                        binding.universityName.setSelection(upper.length)
                    }
                    binding.universityName.addTextChangedListener(this)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

        } catch (e: Exception) {
            Log.e("AutoCompleteSetup", "Autocomplete kurulamadı", e)
        }
    }

    //Devam butonu
    private fun goForward() {
        try {
            val nameSurname = binding.studentName.text.toString().trim()
            val university = binding.universityName.text.toString().trim().uppercase()

            if (nameSurname.isEmpty() || university.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return
            }

            //Üniversite listede var mı kontrolü
            if (!universityList.contains(university)) {
                Toast.makeText(
                    this,
                    "Lütfen geçerli bir üniversite giriniz",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val intent = Intent(this, SignUpStudentActivity::class.java).apply {
                putExtra("studentName", nameSurname)
                putExtra("universityName", university)
                putExtra("studentEmail", studentEmail)
            }

            Log.d("StudentInfo", "AdSoyad: $nameSurname | Üniversite: $university")

            startActivity(intent)

        } catch (e: Exception) {
            Log.e("GoForward", "Veri gönderilirken hata", e)
        }
    }

    // Geri butonu
    fun gotoBack(view: View) {
        finish()
    }

    //Hesabım var
    fun haveAnAccount(view: View){
        startActivity(Intent(this@StudentLoginInfoActivity,StudentLoginActivity::class.java))
    }

    //Şifremi unuttum
    fun forgotPassword(view: View){
        startActivity(Intent(this@StudentLoginInfoActivity, UpdatePasswordActivity::class.java))
    }
}
