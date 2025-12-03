package com.usisoftware.usiapp.view.academicianView

import android.app.Activity
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
import com.usisoftware.usiapp.databinding.ActivityReportsAndComplaintsBinding
import com.usisoftware.usiapp.view.adapter.ReportsAdapter
import com.usisoftware.usiapp.view.model.Report

class ReportsAndComplaintsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsAndComplaintsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ReportsAdapter
    private lateinit var detailLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportsAndComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //Sayfa açıldığında talepleri yükle
        loadReports()

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Verileri yeniden yükle
            loadReports()
        }

        //Launcher bir sayfayı başlatır ve o sayfa kapandığında geriye bir sonuç döner
        detailLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val isDeleted = result.data?.getBooleanExtra("deleted", false) ?: false
                    if (isDeleted) {
                        loadReports()  // Eğer bir şikayet silindiyse listeyi yeniden yükle
                    }
                }
            }

    }

    //Şikayetleri göster
    private fun loadReports() {
        db.collection("Reports")
            .get()
            .addOnSuccessListener { snapshot ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                // Belge verilerini Request modeline dönüştür
                val reportList = snapshot.documents.map { doc ->
                    Report(
                        id = doc.id,
                        message = doc.getString("message") ?: "",
                        requestId = doc.getString("requestId") ?: "",
                        user = doc.getString("user") ?: ""
                    )
                }


                // Adapter tanımla ve her item'a tıklanınca detay ekranına geç
                adapter = ReportsAdapter(
                    reportList.toMutableList(),
                    onItemClick = { clickedReport ->
                        detailLauncher.launch(
                            Intent(this, ReportsDetailActivity::class.java).apply {
                                putExtra("report", clickedReport)
                            })

                    })

                // RecyclerView ayarları yapılır
                binding.reportRequestRecyclerView.adapter = adapter
                binding.reportRequestRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.swipeRefreshLayout.isRefreshing = false

            }
            .addOnFailureListener {
                binding.swipeRefreshLayout.isRefreshing = false

                Toast.makeText(this, "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }


    //Geri dön
    fun back(view: View) {
        finish()
    }

}
