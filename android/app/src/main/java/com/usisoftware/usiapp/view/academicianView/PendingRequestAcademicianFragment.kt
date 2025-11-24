package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.FragmentPendingRequestAcademicianBinding
import com.usisoftware.usiapp.view.adapter.IncomingRequestAdapter
import com.usisoftware.usiapp.view.model.Request
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

        adapter = IncomingRequestAdapter(
            requestList,
            onItemClick = { clickedRequest ->
                val intent = Intent(requireContext(), IncomingRequestDetailActivity::class.java)
                intent.putExtra("request", clickedRequest)
                startActivity(intent)
            }
        )

        binding.fragmentPendingReqAcadRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.fragmentPendingReqAcadRecyclerView.adapter = adapter

        // Swipe Refresh Listener
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPendingRequests()
        }

        // İlk yükleme
        loadPendingRequests()
    }

    private fun loadPendingRequests() {

        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("AcademicianInfo")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val documentId = document.id  // Akademisyenin Firestore doküman ID'si

                            db.collection("OldRequests")
                                .get()
                                .addOnSuccessListener { docs ->
                                    requestList.clear()

                                    for (doc in docs) {
                                        val selectedIds = doc.get("selectedAcademiciansId") as? List<String>

                                        if (selectedIds?.contains(documentId) == true) {
                                            val request = Request(
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
                                                requesterAddress = doc.getString("requesterAddress") ?: "",
                                                adminMessage = doc.getString("adminMessage") ?: "",
                                                adminDocumentId = doc.getString("adminDocumentId") ?: "",
                                                requesterImage = doc.getString("requesterImage") ?: "",
                                                requestCategory = doc.getString("requestCategory") ?: "",
                                                requesterType = doc.getString("requesterType") ?: ""
                                            )
                                            requestList.add(request)
                                        }
                                    }

                                    // Tarihe göre sırala
                                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                    requestList.sortByDescending { req ->
                                        try { dateFormat.parse(req.date) } catch (_: Exception) { null }
                                    }

                                    adapter.notifyDataSetChanged()
                                    binding.swipeRefreshLayout.isRefreshing = false
                                }
                        }
                    }
                }
        }
    }

}