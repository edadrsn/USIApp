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
    // UI’da oluşturulan checkbox’ları tutar
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

        // Authorities koleksiyonundan üniversiteleri yükle
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
            val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
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
                    Toast.makeText(this@SelectUniversityActivity, "Arama hatası!", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Talep oluşturulamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createIndustryRequest(userId: String) {

        db.collection("Industry").document(userId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(this, "Firma bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Firma bilgileri
                val firmaAdi = document.getString("firmaAdi") ?: ""
                val firmaCalismaAlanlari = document.getString("calismaAlanlari") ?: ""
                val firmaPhone = document.getString("telefon") ?: ""
                val email = document.getString("email") ?: ""
                val address = document.getString("adres") ?: ""
                val firmImage = document.getString("requesterImage") ?: ""

                // Önceki activity’den gelen veriler
                val requestTitle = intent.getStringExtra("requestTitle") ?: ""
                val requestMessage = intent.getStringExtra("requestMessage") ?: ""
                val requestType = intent.getBooleanExtra("switchRequestType", false)
                val selectedCategories =
                    intent.getStringArrayListExtra("selectedCategories") ?: arrayListOf()

                val currentDate =
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                // Seçilen üniversiteler (authorityId)
                val selectedAuthorityIds = getSelectedAuthorityIds()

                if (selectedAuthorityIds.isEmpty()) {
                    Toast.makeText(this, "En az bir üniversite seçmelisiniz!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Status map → authorityId : pending
                val statusMap = mutableMapOf<String, String>()
                selectedAuthorityIds.forEach { authorityId ->
                    statusMap[authorityId] = "pending"
                }

                // Firestore’a kaydedilecek request objesi
                val requestData = hashMapOf(
                    "createdDate" to currentDate,
                    "requestTitle" to requestTitle,
                    "requestMessage" to requestMessage,
                    "requesterName" to firmaAdi,
                    "requesterID" to userId,
                    "requesterEmail" to email,
                    "requesterPhone" to firmaPhone,
                    "requesterAddress" to address,
                    "requesterCategories" to firmaCalismaAlanlari,
                    "selectedCategories" to selectedCategories,
                    "requesterImage" to firmImage,
                    "requesterType" to "industry",
                    "requestType" to requestType,
                    "status" to statusMap
                )

                db.collection("Requests")
                    .add(requestData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Talep başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(this, IndustryMainActivity::class.java).apply {
                                putExtra("goToFragment", "request")
                            }
                        )
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Talep kaydedilemedi!", Toast.LENGTH_SHORT).show()
                    }
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

    private fun refreshList() {
        checkboxList.clear()
        binding.universityContainer.removeAllViews()
        loadUniversities()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadUniversities() {
        db.collection("Authorities")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val universityName = doc.getString("universityName") ?: continue
                    val authorityId = doc.id
                    addUniversityItem(universityName, authorityId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Üniversiteler alınamadı!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUniversityItem(name: String, authorityId: String) {
        val checkBox = CheckBox(this).apply {
            text = name
            tag = authorityId
            textSize = 16.5f
            setTextColor(Color.BLACK)
            buttonTintList = ColorStateList.valueOf(Color.parseColor("#124090"))
            setPadding(8, 8, 8, 8)
        }

        checkboxList.add(checkBox)
        binding.universityContainer.addView(checkBox)
    }

    private fun getSelectedAuthorityIds(): List<String> {
        return checkboxList
            .filter { it.isChecked }
            .mapNotNull { it.tag as? String }
    }

    fun goBack(view: View) {
        finish()
    }
}
