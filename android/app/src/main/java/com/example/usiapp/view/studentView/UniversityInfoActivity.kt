package com.example.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityUniversityInfoBinding
import com.example.usiapp.view.repository.StudentInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray

class UniversityInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUniversityInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var universitelerListe: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUniversityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""

        // assets klasöründen JSON dosyasını oku
        val jsonString = loadJsonFromAsset("universite_isimleri.json")
        val jsonArray = JSONArray(jsonString)
        val tempList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            tempList.add(jsonArray.getString(i))   // JSON'dan stringleri al
        }
        universitelerListe = tempList  // Üniversiteler listesine ata

        // AutoCompleteTextView için adapter oluştur
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            universitelerListe
        )

        // AutoCompleteTextView ayarları
        binding.universityName.apply {
            setAdapter(adapter)
            threshold = 0   // Hiç yazmadan açılabilsin
            keyListener = null  // Klavyeyi kapat
            isCursorVisible = false  // İmleci gizle

            // Tıklanınca açılacak dropdown
            setOnClickListener {
                adapter.filter.filter(null)  // Filtreyi sıfırla
                showDropDown()   // Listeyi göster
            }
        }

        // Firebase’den öğrencinin verisini çek
        StudentInfo(db).getStudentData(
            uid,
            onSuccess = { document ->
                if (document != null && document.exists()) {
                    binding.universityName.setText(document.getString("universityName") ?: "")
                }
            },
            onFailure = {
                Toast.makeText(this, "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            })

        // Kaydet butonuna tıklanınca
        binding.saveUniInfo.setOnClickListener {
            val universityName = binding.universityName.text.toString()
            if (universityName.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase'e güncelle
            StudentInfo(db).updateStudentData(
                uid,
                hashMapOf("universityName" to universityName),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, StudentMainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT).show()
                })
        }
    }

    // JSON dosyasını assets klasöründen okuyan fonksiyon
    private fun loadJsonFromAsset(fileName: String): String {
        val inputStream = assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    // Geri dön butonu
    fun backToProfile(view: View) {
        startActivity(Intent(this, StudentMainActivity::class.java))
    }
}
