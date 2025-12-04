package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityOldRequestsBinding
import com.usisoftware.usiapp.view.adapter.OldRequestAdapter
import com.usisoftware.usiapp.view.model.Request
import java.text.SimpleDateFormat
import java.util.Locale

class OldRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOldRequestsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: OldRequestAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOldRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadRequests()

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Verileri yeniden yükle
            loadRequests()

            // animasyonu kapat
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }


    //AdminPanelActivity sayfasına geri dön
    fun previousPage(view: View) {
        finish()
    }

    //Verileri çek
    fun loadRequests() {
        // Admin üniversitesini al
        val domain = auth.currentUser?.email?.substringAfter("@") ?: ""

        db.collection("Authorities")
            .get()
            .addOnSuccessListener { snapshot ->
                val universityName = snapshot.documents.firstOrNull { doc ->
                    val a = doc.getString("academician") ?: ""
                    domain == a
                }?.id

                if (universityName == null) {
                    Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Requests koleksiyonunu çek
                db.collection("Requests")
                    .get()
                    .addOnSuccessListener { requestSnapshot ->
                        val requestList = requestSnapshot.documents.mapNotNull { doc ->
                            val statusMap = doc.get("status") as? Map<String, String> ?: emptyMap()
                            val statusForThisUni = statusMap[universityName]

                            // Sadece approved veya rejected olanlar
                            if (statusForThisUni == "approved" || statusForThisUni == "rejected") {
                                Request(
                                    id = doc.id,
                                    title = doc.getString("requestTitle") ?: "",
                                    message = doc.getString("requestMessage") ?: "",
                                    date = doc.getString("createdDate") ?: "",
                                    status = statusMap,
                                    requesterId = doc.getString("requesterID") ?: "",
                                    selectedCategories = doc.get("selectedCategories") as? List<String> ?: emptyList(),
                                    requesterName = doc.getString("requesterName") ?: "",
                                    requesterAddress = doc.getString("requesterAddress") ?: "",
                                    requesterCategories = doc.getString("requesterCategories") ?: "",
                                    requesterEmail = doc.getString("requesterEmail") ?: "",
                                    requesterPhone = doc.getString("requesterPhone") ?: "",
                                    requesterImage = doc.getString("requesterImage"),
                                    requestCategory = doc.getString("requestCategory") ?: "",
                                    requesterType = doc.getString("requesterType") ?: "",
                                    requestType = doc.getBoolean("requestType") ?: false,
                                )
                            } else {
                                null
                            }
                        }

                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val sortedRequestList = requestList.sortedByDescending {
                            try { dateFormat.parse(it.date) } catch (e: Exception) { null }
                        }

                        adapter = OldRequestAdapter(
                            sortedRequestList.toMutableList(),
                            onItemClick = { clickedRequest ->
                                startActivity(Intent(this, OldRequestDetailActivity::class.java).apply {
                                    putExtra("request", clickedRequest)
                                })
                            }
                        )

                        binding.oldRequestRecyclerView.layoutManager = LinearLayoutManager(this)
                        binding.oldRequestRecyclerView.adapter = adapter
                    }
                    .addOnFailureListener { e ->
                        Log.e("OldRequestsActivity", "Requests verileri alınamadı", e)
                        Toast.makeText(this, "Requests verileri alınamadı", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("OldRequestsActivity", "Authorities verileri alınamadı", e)
                Toast.makeText(this, "Authorities verileri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }

}