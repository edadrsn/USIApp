package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityPendingRequestsBinding
import com.usisoftware.usiapp.view.adapter.AdminAdapter
import com.usisoftware.usiapp.view.model.Request
import java.text.SimpleDateFormat
import java.util.Locale

class PendingRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingRequestsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AdminAdapter
    private lateinit var detailLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadRequests()

        //SwipeRefresh: Aşağı çekince yenileme
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRequests()
        }


        detailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Talep reddedilmiş veya onaylanmış -> verileri yeniden çek
                recreate() // onCreate()'i tekrar çalıştırır, verileri günceller
            }
        }

    }

    // AdminPanelActivity'e dön
    fun previousPage(view: View) {
        finish()
    }

    //Verileri çek
    fun loadRequests() {
        binding.swipeRefreshLayout.isRefreshing = true

        val userEmail = auth.currentUser?.email ?: ""
        val domain = userEmail.substringAfter("@", "")


        db.collection("Authorities")
            .get()
            .addOnSuccessListener { snapshot ->

                var universityName: String? = null

                for (doc in snapshot.documents) {
                    val studentDomain = doc.getString("student") ?: ""
                    val academicDomain = doc.getString("academician") ?: ""

                    if (domain == studentDomain || domain == academicDomain) {
                        universityName = doc.id
                        break
                    }
                }

                if (universityName == null) {
                    Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                    return@addOnSuccessListener
                }
                // Sadece belirtilen üniversitede "pending" olan talepleri getir
                db.collection("Requests")
                    .whereEqualTo("status.$universityName", "pending")
                    .get()
                    .addOnSuccessListener { requestSnap ->

                        val requestList = requestSnap.documents.map { doc ->
                            Request(
                                id = doc.id,
                                title = doc.getString("requestTitle") ?: "",
                                message = doc.getString("requestMessage") ?: "",
                                date = doc.getString("createdDate") ?: "",
                                status = doc.get("status") as? Map<String, String> ?: emptyMap(),
                                requesterId = doc.getString("requesterID") ?: "",
                                selectedCategories = doc.get("selectedCategories") as? List<String> ?: emptyList(),
                                requesterName = doc.getString("requesterName") ?: "",
                                requesterAddress = doc.getString("requesterAddress") ?: "",
                                requesterCategories = doc.getString("requesterCategories") ?: "",
                                requesterEmail = doc.getString("requesterEmail") ?: "",
                                requesterPhone = doc.getString("requesterPhone") ?: "",
                                adminMessage = doc.getString("adminMessage") ?: "",
                                adminDocumentId = doc.getString("adminDocumentId") ?: "",
                                requesterImage = doc.getString("requesterImage") ?: "",
                                requestCategory = doc.getString("requestCategory") ?: "",
                                requesterType = doc.getString("requesterType") ?: "",
                                requestType = doc.getBoolean("requestType") ?: false
                            )
                        }

                        // Güvenli tarih sıralama
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
                        val sortedList = requestList.sortedByDescending { req ->
                            try {
                                dateFormat.parse(req.date)?.time
                            } catch (e: Exception) {
                                null
                            }
                        }

                        adapter = AdminAdapter(
                            sortedList.toMutableList(),
                            onItemClick = { clickedRequest ->
                                detailLauncher.launch(
                                        Intent(this, PendingRequestDetailActivity::class.java).apply {
                                            putExtra("request", clickedRequest)
                                        }
                                    )
                            }
                        )

                        binding.adminRecyclerView.adapter = adapter
                        binding.adminRecyclerView.layoutManager = LinearLayoutManager(this)
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Veri alınamadı!", Toast.LENGTH_SHORT).show()
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Yetkili bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }

    }

}
