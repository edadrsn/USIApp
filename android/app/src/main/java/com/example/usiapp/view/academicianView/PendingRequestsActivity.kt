package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usiapp.databinding.ActivityPendingRequestsBinding
import com.example.usiapp.view.adapter.AdminAdapter
import com.example.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        detailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Talep reddedilmiş veya onaylanmış -> verileri yeniden çek
                recreate() // onCreate()'i tekrar çalıştırır, verileri günceller
            }
        }


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Firestore'dan sadece "pending" durumundaki talepleri çek
        db.collection("Requests")
            .whereEqualTo("status", "pending").get()
            .addOnSuccessListener { snapshot ->
                // Belge verilerini Request modeline dönüştür
                val requestList = snapshot.documents.map { doc ->
                    Request(
                        id = doc.id,
                        title = doc.getString("requestTitle") ?: "",
                        message = doc.getString("requestMessage") ?: "",
                        date = doc.getString("createdDate") ?: "",
                        status = doc.getString("status") ?: "",
                        requesterId = doc.getString("requesterID") ?: "",
                        selectedCategories = doc.get("selectedCategories") as? List<String>
                            ?: emptyList(),
                        requesterName = doc.getString("requesterName") ?: "",
                        requesterCategories = doc.getString("requesterCategories") ?: "",
                        requesterEmail = doc.getString("requesterEmail") ?: "",
                        requesterPhone = doc.getString("requesterPhone") ?: "",
                        adminMessage = doc.getString("adminMessage") ?: "",
                        adminDocumentId = doc.getString("adminDocumentId") ?: "",
                        requesterImage = doc.getString("requesterImage") ?: "",
                        requestCategory = doc.getString("requestCategory") ?: "",
                        requesterType = doc.getString("requesterType") ?: "",
                    )
                }


                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                //Tarihe göre sırala
                val sortedRequestList = requestList.sortedByDescending { request ->
                    try {
                        dateFormat.parse(request.date)
                    } catch (e: Exception) {
                        null
                    }
                }
                val mutableRequests = sortedRequestList.toMutableList()

                // Adapter tanımla ve her item'a tıklanınca detay ekranına geç
                adapter = AdminAdapter(
                    mutableRequests,
                    onItemClick = { clickedRequest ->
                        detailLauncher.launch(
                            Intent(this, PendingRequestDetailActivity::class.java).apply {
                                putExtra("request", clickedRequest)
                            })
                        finish()
                    }
                )

                // RecyclerView ayarları yapılır
                binding.adminRecyclerView.adapter = adapter
                binding.adminRecyclerView.layoutManager = LinearLayoutManager(this)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }

    // AdminPanelActivity'e dön
    fun previousPage(view: View) {
        val intent = Intent(this@PendingRequestsActivity, AdminPanelActivity::class.java)
        startActivity(intent)
    }
}

