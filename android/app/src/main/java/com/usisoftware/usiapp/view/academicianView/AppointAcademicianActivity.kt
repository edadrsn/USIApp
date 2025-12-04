package com.usisoftware.usiapp.view.academicianView

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityAppointAcademicianBinding
import com.usisoftware.usiapp.view.adapter.AcademicianSearchAdapter
import com.usisoftware.usiapp.view.model.Academician

class AppointAcademicianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointAcademicianBinding
    private lateinit var adapter: AcademicianSearchAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val selectedAcademicians = mutableListOf<Academician>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppointAcademicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

            setupSearchView()
            setupRecyclerView()
            binding.btnAppointAcademician.visibility = View.GONE

            loadAcademiciansByAdminDomain()

            val requestId = intent.getStringExtra("requestId")
            if (requestId.isNullOrEmpty()) {
                Toast.makeText(this, "Talep ID bulunamadı!", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            binding.btnAppointAcademician.setOnClickListener {
                if (selectedAcademicians.isEmpty()) {
                    Toast.makeText(this, "Lütfen en az bir akademisyen seçin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedAcademiciansId = selectedAcademicians.mapNotNull { it.documentId }
                val academicianResponses = selectedAcademiciansId.associateWith { "pending" }

                // Requests güncelle
                updateRequestWithAcademicians(requestId, selectedAcademiciansId, academicianResponses)
            }

    }

    //SearchView setup
    private fun setupSearchView() {
        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = AcademicianSearchAdapter(emptyList()) { selectedAcademician ->
            addSelectedAcademician(selectedAcademician)
        }
        binding.appointAcademicianRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appointAcademicianRecyclerView.adapter = adapter
    }

    // Admin mail uzantısına göre akademisyenleri getir
    private fun loadAcademiciansByAdminDomain() {
        val email = auth.currentUser?.email ?: ""
        if (email.isBlank()) {
            Toast.makeText(this, "Email bulunamadı!", Toast.LENGTH_LONG).show()
            return
        }

        val adminDomain = email.substringAfter("@").lowercase().trim()
        fetchAcademiciansFromFirestore(adminDomain)
    }

    //Firestoredan akademisyen verilerini getir
    private fun fetchAcademiciansFromFirestore(mailDomain: String) {
        db.collection("Academician")
            .get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<Academician>()
                for (document in result) {
                    val email = document.getString("email") ?: continue
                    val domain = email.substringAfter("@").lowercase().trim()
                    if (domain == mailDomain) {
                        val academician = Academician(
                            academicianName = document.getString("adSoyad") ?: "",
                            academicianDegree = document.getString("unvan") ?: "",
                            academicianImageUrl = document.getString("photo") ?: "",
                            academicianExpertArea = document.get("uzmanlikAlanlari") as? List<String> ?: emptyList(),
                            academicianEmail = email,
                            documentId = document.id
                        )
                        tempList.add(academician)
                    }
                }
                adapter.setData(tempList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Akademisyenler alınamadı!", Toast.LENGTH_LONG).show()
            }
    }

    //Seçilen akademisyeni ekleme
    private fun addSelectedAcademician(academician: Academician) {
        if (selectedAcademicians.any { it.documentId == academician.documentId }) {
            Toast.makeText(this, "${academician.academicianName} zaten seçili", Toast.LENGTH_SHORT).show()
            return
        }
        selectedAcademicians.add(academician)
        addChipForAcademician(academician)
        updateButtonVisibility()
    }

    //Seçilen akademisyeni chipe ekle
    private fun addChipForAcademician(academician: Academician) {
        val chipGroup = binding.selectedAcademiciansChipGroup
        val chip = Chip(this).apply {
            text = academician.academicianName
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#C8E6C9"))
            setTextColor(Color.parseColor("#2E7D32"))
            closeIcon = ContextCompat.getDrawable(context, R.drawable.baseline_close_24)
            closeIconTint = ColorStateList.valueOf(Color.parseColor("#2E7D32"))

            setOnCloseIconClickListener {
                selectedAcademicians.remove(academician)
                chipGroup.removeView(this)
                updateButtonVisibility()
            }
        }
        chipGroup.addView(chip)
    }

    //Buton görünürlüğünü güncelle
    private fun updateButtonVisibility() {
        binding.btnAppointAcademician.visibility =
            if (selectedAcademicians.isNotEmpty()) View.VISIBLE else View.GONE
    }

    // Requests koleksiyonunu güncelle
    private fun updateRequestWithAcademicians(
        requestId: String,
        selectedAcademiciansId: List<String>,
        academicianResponses: Map<String, String>
    ) {
        getAdminUniversity { universityName ->
            if (universityName == null) {
                Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                return@getAdminUniversity
            }

            val updates = mapOf(
                "selectedAcademiciansId" to selectedAcademiciansId,
                "academicianResponses" to academicianResponses,
                "status.$universityName" to "approved"
            )

            db.collection("Requests").document(requestId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Akademisyenler atandı!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Güncelleme hatası!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Admin üniversitesini Authorities koleksiyonundan bulma
    private fun getAdminUniversity(callback: (String?) -> Unit) {
        val domain = auth.currentUser?.email?.substringAfter("@") ?: ""

        db.collection("Authorities")
            .get()
            .addOnSuccessListener { snapshot ->
                val university = snapshot.documents.firstOrNull { doc ->
                    val a = doc.getString("academician") ?: ""
                    domain == a
                }?.id
                callback(university)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    //Geri dön
    fun prevPage(view: View) {
        finish()
    }
}
