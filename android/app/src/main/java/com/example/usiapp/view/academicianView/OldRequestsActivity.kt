package com.example.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usiapp.databinding.ActivityOldRequestsBinding
import com.example.usiapp.view.adapter.OldRequestAdapter
import com.example.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class OldRequestsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOldRequestsBinding
    private lateinit var db:FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    private lateinit var adapter: OldRequestAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityOldRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db= FirebaseFirestore.getInstance()
        auth=FirebaseAuth.getInstance()

        db.collection("OldRequests")
            .get()
            .addOnSuccessListener { snapshot ->
                // Belge verilerini OldRequest modeline dönüştür
                val requestList = snapshot.documents.map { doc ->
                    Request(
                        id = doc.id,
                        title = doc.getString("requestTitle") ?: "",
                        message = doc.getString("requestMessage") ?: "",
                        date = doc.getString("createdDate") ?: "",
                        status = doc.getString("status") ?: "",
                        requesterId = doc.getString("requesterID") ?: "",
                        selectedCategories = doc.get("selectedCategories") as? List<String> ?: emptyList(),
                        requesterName = doc.getString("requesterName") ?: "",
                        requesterCategories = doc.getString("requesterCategories") ?: "",
                        requesterEmail = doc.getString("requesterEmail") ?: "",
                        requesterPhone = doc.getString("requesterPhone") ?: "",

                    )
                }

                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                //Tarihe göre sırala - yeniden eskiye
                val sortedRequestList = requestList.sortedByDescending { request ->
                    try {
                        dateFormat.parse(request.date)
                    } catch (e: Exception) {
                        null
                    }
                }

                val mutableRequests = sortedRequestList.toMutableList()


                // Adapter tanımla ve her item'a tıklanınca detay ekranına geç
                adapter = OldRequestAdapter(
                    mutableRequests,
                    onItemClick = { clickedRequest ->
                       startActivity(Intent(this, OldRequestDetailActivity::class.java).apply {
                           putExtra("request", clickedRequest)
                        })

                    }
                )
                // RecyclerView ayarları yapılır
                binding.oldRequestRecyclerView.adapter = adapter
                binding.oldRequestRecyclerView.layoutManager = LinearLayoutManager(this)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }


    //AdminPanelActivity sayfasına geri dön
    fun previousPage(view: View){
        startActivity(Intent(this@OldRequestsActivity,AdminPanelActivity::class.java))
    }

}