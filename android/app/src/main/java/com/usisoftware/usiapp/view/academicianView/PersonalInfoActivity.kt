package com.usisoftware.usiapp.view.academicianView

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityPersonalInfoBinding
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        // Giriş yapan kullanıcı uid
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId=currentUser.uid

        // Ünvan seçeneklerini tanımlıyoruz
        val unvanlar = listOf(
            "Prof. Dr.",
            "Doç. Dr.",
            "Dr. Öğr. Üyesi",
            "Dr.",
            "Öğr. Gör. Dr.",
            "Öğr. Gör.",
            "Arş. Gör."
        )

        // DropDown için adapter tanımladım
        val adapter = ArrayAdapter(
            this@PersonalInfoActivity,
            android.R.layout.simple_dropdown_item_1line,
            unvanlar
        )
        val dropdown = binding.personDegree
        dropdown.setAdapter(adapter)
        dropdown.setOnClickListener { dropdown.showDropDown() }

        // Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                val fullName = document.getString("adSoyad") ?: ""
                val degree = document.getString("unvan") ?: ""

                // adSoyad alanını boşluğa göre parçala
                val nameParts = fullName.trim().split(" ")
                if (nameParts.size >= 2) {
                    val surname = nameParts.last()
                    val name = nameParts.dropLast(1).joinToString(" ")

                    binding.personName.setText(name)
                    binding.personSurname.setText(surname)
                } else {
                    binding.personName.setText(fullName)
                    binding.personSurname.setText("")
                }

                binding.personDegree.setText(degree, false)
            },
            onFailure = { e ->
                Log.e("PersonalInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        // Güncelleme butonuna tıklanınca AlertDialog göster
        binding.updatePersonalInfo.setOnClickListener {
            AlertDialog.Builder(this@PersonalInfoActivity).apply {
                setTitle("Güncelleme")
                setMessage("Kişisel bilgilerinizi güncellemek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->
                    val updateName = binding.personName.text.toString()
                    val updateSurname = binding.personSurname.text.toString()
                    val updateDegree = binding.personDegree.text.toString()

                    if (updateName.isEmpty() || updateSurname.isEmpty() || updateDegree.isEmpty()) {
                        Toast.makeText(this@PersonalInfoActivity, "Lütfen tüm alanları doldurun!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val fullName = "$updateName $updateSurname"
                    val updates = mapOf(
                        "adSoyad" to fullName,
                        "unvan" to updateDegree
                    )

                    //Güncelleme
                    GetAndUpdateAcademician.updateAcademicianInfo(
                        db,
                        userId,
                        updates,
                        onSuccess = {
                            Toast.makeText(this@PersonalInfoActivity, "Bilgiler başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(this@PersonalInfoActivity, "Bilgileri güncellerken sorun oluştu!", Toast.LENGTH_LONG).show()
                            Log.e("Hata",": ${it.localizedMessage}")
                        })
                    dialog.dismiss()
                }

                setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss()
                }

                create()
                show()
            }
        }
    }

    //Geri dön
    fun goToProfile(view: View) {
        finish()
    }
}