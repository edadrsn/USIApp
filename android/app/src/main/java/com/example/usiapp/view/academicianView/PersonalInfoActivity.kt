package com.example.usiapp.view.academicianView

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityPersonalInfoBinding
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPersonalInfoBinding

    private lateinit var personName: EditText
    private lateinit var personSurname: EditText
    private lateinit var personDegree: AutoCompleteTextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


        personName = binding.personName
        personSurname = binding.personSurname
        personDegree = binding.personDegree

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Giriş yapan kullanıcının e-posta adresi
        val email = auth.currentUser?.email ?: return

        // Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id

                val fullName = document.getString("adSoyad") ?: ""
                val degree = document.getString("unvan") ?: ""

                // adSoyad alanını boşluğa göre parçala
                val nameParts = fullName.trim().split(" ")
                if (nameParts.size >= 2) {
                    val surname = nameParts.last()
                    val name = nameParts.dropLast(1).joinToString(" ")

                    personName.setText(name)
                    personSurname.setText(surname)
                } else {
                    personName.setText(fullName)
                    personSurname.setText("")
                }

                personDegree.setText(degree, false)
                personName.isEnabled = false
                personSurname.isEnabled = false
            },
            onFailure = {}
        )

        // Güncelleme butonuna tıklanınca AlertDialog göster
        binding.updatePersonalInfo.setOnClickListener {
            AlertDialog.Builder(this@PersonalInfoActivity).apply {
                setTitle("Güncelleme")
                setMessage("Kişisel bilgilerinizi güncellemek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->
                    val updateName = personName.text.toString()
                    val updateSurname = personSurname.text.toString()
                    val updateDegree = personDegree.text.toString()

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
                        documentId.toString(),
                        updates,
                        onSuccess = {
                            Toast.makeText(
                                this@PersonalInfoActivity,
                                "Bilgiler başarıyla güncellendi",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = {
                            Toast.makeText(
                                this@PersonalInfoActivity,
                                "Hata: ${it.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
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

    fun goToProfile(view: View){
        val intent= Intent(this@PersonalInfoActivity, AcademicianMainActivity::class.java)
        startActivity(intent)
    }
}