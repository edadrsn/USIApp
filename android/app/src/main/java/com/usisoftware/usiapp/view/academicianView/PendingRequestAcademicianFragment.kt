package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.usisoftware.usiapp.databinding.FragmentPendingRequestAcademicianBinding
import com.usisoftware.usiapp.view.adapter.IncomingRequestAdapter
import com.usisoftware.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


class PendingRequestAcademicianFragment : Fragment() {

    private var _binding: FragmentPendingRequestAcademicianBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: IncomingRequestAdapter
    private val requestList = mutableListOf<Request>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPendingRequestAcademicianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("AcademicianInfo")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val documentId = document.id  // Akademisyenin Firestore doküman ID'si

                            // Tüm OldRequests koleksiyonunu al
                            db.collection("OldRequests")
                                .get()
                                .addOnSuccessListener { documents ->
                                    // Önce mevcut listeyi temizle
                                    requestList.clear()

                                    for (document in documents) {
                                        // Talebe seçilen akademisyenlerin ID listesini al
                                        val selectedIds = document.get("selectedAcademiciansId") as? List<String>

                                        if (selectedIds != null) {
                                            for (id in selectedIds) {
                                                if (documentId == id) {
                                                    // Talep modelini oluştur
                                                    val request = Request(
                                                        id = document.id,
                                                        title = document.getString("requestTitle") ?: "",
                                                        message = document.getString("requestMessage") ?: "",
                                                        date = document.getString("createdDate") ?: "",
                                                        status = document.getString("status") ?: "",
                                                        requesterId = document.getString("requesterID") ?: "",
                                                        selectedCategories = document.get("selectedCategories") as? List<String> ?: emptyList(),
                                                        requesterName = document.getString("requesterName") ?: "",
                                                        requesterCategories = document.getString("requesterCategories") ?: "",
                                                        requesterEmail = document.getString("requesterEmail") ?: "",
                                                        requesterPhone = document.getString("requesterPhone") ?: "",
                                                        requesterAddress = document.getString("requesterAddress") ?: "",
                                                        adminMessage = document.getString("adminMessage") ?: "",
                                                        adminDocumentId = document.getString("adminDocumentId") ?: "",
                                                        requesterImage = document.getString("requesterImage") ?: "",
                                                        requestCategory = document.getString("requestCategory") ?: "",
                                                        requesterType = document.getString("requesterType") ?: ""
                                                    )
                                                    requestList.add(request)
                                                }
                                            }
                                        }
                                    }

                                    // Listeyi tarihe göre sırala
                                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                    requestList.sortByDescending { req ->
                                        try {
                                            dateFormat.parse(req.date)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }

                                    // Adapter'a veri değiştiğini bildir
                                    adapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("OldRequest", "Veri çekme hatası", e)
                                }
                        }
                    }
                }
        }

        // Adapter'ı oluştur, tıklama fonksiyonu ile detay sayfasına intent atar
        adapter = IncomingRequestAdapter(
            requestList,
            onItemClick = { clickedRequest ->
                val intent = Intent(requireContext(), IncomingRequestDetailActivity::class.java)
                intent.putExtra("request", clickedRequest)
                startActivity(intent)
            }
        )

        // RecyclerView'ı ayarla, linear layout manager ve adapter ata
        val recyclerView = binding.fragmentPendingReqAcadRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

}