package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivitySelectUniversityBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectUniversityActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUniversityBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val checkboxList = mutableListOf<CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectUniversityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUniversities()

        // Hepsini seç
        binding.btnSelectAll.setOnClickListener {
            try {
                checkboxList.forEach { it.isChecked = true }
            } catch (e: Exception) {
                Toast.makeText(this, "Seçim hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            try {
                refreshList()
            } catch (e: Exception) {
                Toast.makeText(this, "Yenileme sırasında hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }

        // SearchView renk ayarı
        try {
            val searchEditText = binding.searchView.findViewById<EditText>(
                androidx.appcompat.R.id.search_src_text
            )
            searchEditText.setTextColor(Color.BLACK)
            searchEditText.setHintTextColor(Color.parseColor("#808080"))
        } catch (e: Exception) {
            Toast.makeText(this, "Arama çubuğu ayarlanamadı", Toast.LENGTH_SHORT).show()
        }

        // Arama işlemi
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                try {
                    filterUniversities(query)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@SelectUniversityActivity,
                        "Arama hatası!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    filterUniversities(newText)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@SelectUniversityActivity,
                        "Arama hatası!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        })

        // Talebi kaydet
        binding.createIndustryRequest.setOnClickListener {
            try {
                createIndustryRequest(userId)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Talep oluşturulamadı: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createIndustryRequest(userId: String) {
        db.collection("Industry").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    Toast.makeText(this, "Firma bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                try {
                    // Firma bilgileri
                    val firmaAdi = document.getString("firmaAdi") ?: ""
                    val firmaCalismaAlanlari = document.getString("calismaAlanlari") ?: ""
                    val firmaPhone = document.getString("telefon") ?: ""
                    val email = document.getString("email") ?: ""
                    val address = document.getString("adres") ?: ""
                    val firmImage = document.getString("requesterImage") ?: ""

                    // Intent verileri
                    val requestTitle = intent.getStringExtra("requestTitle") ?: ""
                    val requestMessage = intent.getStringExtra("requestMessage") ?: ""
                    val switchRequestType = intent.getBooleanExtra("switchRequestType", false)
                    val selectedCategories =
                        intent.getStringArrayListExtra("selectedCategories") ?: arrayListOf()
                    val currentDate =
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                    // Üniversite seçimleri
                    val selectedNames = getSelectedUniversities()
                    if (selectedNames.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Lütfen en az bir üniversite seçin.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addOnSuccessListener
                    }

                    // Authorities doküman kontrolü
                    db.collection("Authorities").get()
                        .addOnSuccessListener { authDocs ->

                            val statusMap = mutableMapOf<String, String>()

                            for (doc in authDocs) {
                                val uniName = doc.getString("universityName")
                                val uniId = doc.id
                                if (uniName != null && selectedNames.contains(uniName)) {
                                    statusMap[uniId] = "pending"
                                }
                            }

                            if (statusMap.isEmpty()) {
                                Toast.makeText(
                                    this,
                                    "Seçilen üniversiteler bulunamadı.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@addOnSuccessListener
                            }

                            // Kaydedilecek veri
                            val categoryInfo = hashMapOf(
                                "createdDate" to currentDate,
                                "requestMessage" to requestMessage,
                                "requestTitle" to requestTitle,
                                "requesterCategories" to firmaCalismaAlanlari,
                                "requesterEmail" to email,
                                "requesterID" to userId,
                                "requesterName" to firmaAdi,
                                "requesterPhone" to firmaPhone,
                                "requesterAddress" to address,
                                "selectedCategories" to selectedCategories,
                                "status" to statusMap,
                                "requesterImage" to firmImage,
                                "requesterType" to "industry",
                                "requestType" to switchRequestType
                            )

                            // Firestore'a kaydet
                            db.collection("Requests")
                                .add(categoryInfo)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Talep başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                                    startActivity(
                                        Intent(this, IndustryMainActivity::class.java).apply {
                                            putExtra("goToFragment", "request")
                                        })
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Kaydetme hatası: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Authorities alınamadı: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }

                } catch (e: Exception) {
                    Toast.makeText(this, "Talep hazırlanırken hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Firma bilgisi alınamadı: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterUniversities(query: String?) {
        val searchText = query?.lowercase(Locale.getDefault()) ?: ""
        checkboxList.forEach { cb ->
            cb.visibility =
                if (cb.text.toString().lowercase(Locale.getDefault()).contains(searchText)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun getSelectedUniversities(): List<String> {
        return checkboxList.filter { it.isChecked }.map { it.text.toString() }
    }

    private fun refreshList() {
        checkboxList.clear()
        binding.universityContainer.removeAllViews()
        loadUniversities()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadUniversities() {
        db.collection("Authorities").get()
            .addOnSuccessListener { documents ->
                try {
                    if (documents.isEmpty) {
                        Toast.makeText(this, "Henüz üniversite eklenmemiş!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    for (doc in documents) {
                        val name = doc.getString("universityName") ?: continue
                        addUniversityItem(name)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Üniversite eklenirken hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Üniversiteler alınamadı: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUniversityItem(name: String) {
        try {
            val checkBox = CheckBox(this)
            checkBox.text = name
            checkBox.textSize = 16.5f
            checkBox.setTextColor(Color.BLACK)
            checkBox.buttonTintList = ColorStateList.valueOf(Color.parseColor("#124090"))
            checkBox.setPadding(8, 8, 8, 8)

            checkboxList.add(checkBox)
            binding.universityContainer.addView(checkBox)

        } catch (e: Exception) {
            Toast.makeText(this, "Checkbox oluşturulamadı", Toast.LENGTH_SHORT).show()
        }
    }

    fun goBack(view: View) {
        finish()
    }
}
