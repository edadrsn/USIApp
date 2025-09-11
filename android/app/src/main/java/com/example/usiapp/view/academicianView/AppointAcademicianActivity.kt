package com.example.usiapp.view.academicianView

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAppointAcademicianBinding
import com.example.usiapp.view.adapter.AcademicianSearchAdapter
import com.example.usiapp.view.model.Academician
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

class AppointAcademicianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointAcademicianBinding
    private lateinit var adapter: AcademicianSearchAdapter
    private val db = FirebaseFirestore.getInstance()
    private val selectedAcademicians = mutableListOf<Academician>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAppointAcademicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SearchView yazı rengini ayarla
        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)

        // Arama işlemleri
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

        // Adapter oluştur
        adapter = AcademicianSearchAdapter(emptyList()) { selectedAcademician ->
            addSelectedAcademician(selectedAcademician)
        }

        // RecyclerView yapılandır
        binding.appointAcademicianRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appointAcademicianRecyclerView.adapter = adapter

        // Verileri çek
        fetchAcademiciansFromFirestore()


        //Akademisyen butonuna tıklayınca seçilen akademisyenleri al
        val requestId = intent.getStringExtra("requestId") ?: return

        binding.btnAppointAcademician.setOnClickListener {
            if (selectedAcademicians.isEmpty()) {
                Toast.makeText(this, "Lütfen en az bir akademisyen seçin", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val selectedAcademiciansId = selectedAcademicians.map { it.documentId }

            // AcademicianResponses map'i oluşturuluyor
            val academicianResponses = mutableMapOf<String, String>()
            for (id in selectedAcademiciansId) {
                academicianResponses[id] = "pending"
            }

            // Sadece OldRequests'e kaydet
            moveOldRequestApproved(requestId, selectedAcademiciansId, academicianResponses)
        }


    }


    private fun moveOldRequestApproved(
        requestId: String,
        selectedAcademiciansId: List<String>,
        academicianResponses: Map<String, String>
    ) {
        val sourceRef = db.collection("Requests").document(requestId) // Kaynak
        val targetRef = db.collection("OldRequests").document(requestId) // Hedef

        sourceRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data?.toMutableMap() ?: mutableMapOf()

                    // Yeni alanları ekle
                    data["selectedAcademiciansId"] = selectedAcademiciansId
                    data["academicianResponses"] = academicianResponses

                    // Belgeyi hedef koleksiyona kaydet
                    targetRef.set(data)
                        .addOnSuccessListener {
                            println("Talep eski kayıtlara kopyalandı")
                            setResult(RESULT_OK)
                            finish()
                        }
                        .addOnFailureListener {
                            println("Kopyalama hatası")
                        }
                } else {
                    println("Talep bulunamadı")
                }
            }
            .addOnFailureListener {
                println("Hata")
            }
    }


    // Seçilen akademisyeni listeye ve chip'e ekle
    private fun addSelectedAcademician(academician: Academician) {
        if (selectedAcademicians.any { it.academicianName == academician.academicianName }) {
            Toast.makeText(this, "${academician.academicianName} zaten seçili", Toast.LENGTH_SHORT)
                .show()
            return
        }
        selectedAcademicians.add(academician)
        addChipForAcademician(academician)
    }

    // Chip oluştur
    private fun addChipForAcademician(academician: Academician) {
        val chipGroup = binding.selectedAcademiciansChipGroup
        val chip = Chip(this).apply {
            text = academician.academicianName
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#C8E6C9"))
            setTextColor(Color.parseColor("#2E7D32"))
            chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#A5D6A7"))
            chipStrokeWidth = 1f
            chipCornerRadius = 30f
            textSize = 13f
            setPadding(24, 14, 24, 14)
            closeIcon = ContextCompat.getDrawable(context, R.drawable.baseline_close_24)
            closeIconTint = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
            elevation = 6f
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(12, 4, 12, 4) }
            setOnCloseIconClickListener {
                selectedAcademicians.remove(academician)
                chipGroup.removeView(this)
            }
        }
        chipGroup.addView(chip)
    }

    // Firestore'dan akademisyen verilerini al
    private fun fetchAcademiciansFromFirestore() {
        db.collection("AcademicianInfo")
            .get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<Academician>()
                for (document in result) {
                    val name = document.getString("adSoyad") ?: ""
                    val degree = document.getString("unvan") ?: ""
                    val imageUrl = document.getString("photo") ?: ""
                    val expertAreas =
                        document.get("uzmanlikAlanlari") as? List<String> ?: emptyList()
                    val email = document.getString("email") ?: ""

                    val academician = Academician(
                        academicianName = name,
                        academicianDegree = degree,
                        academicianImageUrl = imageUrl,
                        academicianExpertArea = expertAreas,
                        academicianEmail = email,
                        documentId = document.id
                    )
                    tempList.add(academician)
                }
                adapter.setData(tempList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }

    //Önceki sayfaya dön
    fun prevPage(view: View) {
        startActivity(
            Intent(
                this@AppointAcademicianActivity,
                PendingRequestDetailActivity::class.java
            )
        )
        finish()
    }
}
