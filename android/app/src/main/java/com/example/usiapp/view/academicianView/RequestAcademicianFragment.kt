package com.example.usiapp.view.academicianView

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usiapp.databinding.FragmentRequestAcademicianBinding
import com.example.usiapp.view.adapter.RequestAdapter
import com.example.usiapp.view.model.Request
import com.example.usiapp.view.repository.RequestFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


class RequestAcademicianFragment : Fragment() {

    private var _binding: FragmentRequestAcademicianBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: RequestAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestAcademicianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email ?: return

        // Önce AcademicianInfo'dan documentId bul
        db.collection("AcademicianInfo")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val academicianDocId = querySnapshot.documents[0].id

                    // Şimdi Requests'ten bu academicanDocId ile talepleri getir
                    RequestFirebase.getUserRequest(
                        db,
                        academicianDocId,
                        onSuccess = { documents ->
                            // Firestore'dan gelen belgeleri Request nesnelerine dönüştür
                            val requestList = documents.map { doc ->

                                val categories = doc.getString("requestCategory")
                                    ?.let { listOf(it) } ?: emptyList()

                                Request(
                                    id = doc.id,
                                    title = doc.getString("requestTitle") ?: "",
                                    message = doc.getString("requestMessage") ?: "",
                                    date = doc.getString("createdDate") ?: "",
                                    status = doc.getString("status") ?: "",
                                    requesterId = doc.getString("requesterID") ?: "",
                                    requesterName = doc.getString("requesterName") ?: "",
                                    requesterCategories = doc.getString("requesterCategories") ?: "",
                                    requesterEmail = doc.getString("requesterEmail") ?: "",
                                    requesterPhone = doc.getString("requesterPhone") ?: "",
                                    requesterAddress = doc.getString("requesterAddress") ?: "",
                                    adminMessage = doc.getString("adminMessage") ?: "",
                                    adminDocumentId = doc.getString("adminDocumentId") ?: "",
                                    requesterImage = doc.getString("requesterImage"),
                                    selectedAcademiciansId = (doc.get("selectedAcademiciansId") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    requesterType = doc.getString("requesterType") ?: "",
                                    requestCategory = doc.getString("requestCategory") ?: "",
                                    selectedCategories = categories
                                )
                            }

                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                            val sortedRequestList = requestList.sortedByDescending { request ->
                                try {
                                    dateFormat.parse(request.date)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            val mutableRequests = sortedRequestList.toMutableList()

                            // RecyclerView Adapter'ı tanımlanır
                            adapter = RequestAdapter(
                                mutableRequests,
                                onDeleteClick = { requestToDelete ->

                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Silme İsteği")
                                        .setMessage("Talebi silmek istediğinize emin misiniz ?")
                                        .setPositiveButton("Evet") { _, _ ->

                                            db.collection("Requests").document(requestToDelete.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(requireContext(), "Talep bilgisi silindi", Toast.LENGTH_SHORT).show()
                                                    mutableRequests.remove(requestToDelete)
                                                    adapter.notifyDataSetChanged()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(requireContext(), "Silme başarısız", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .setNegativeButton("Hayır", null)
                                        .show()
                                },
                                onItemClick = { clickedRequest ->

                                    // Talep kartına tıklanınca detay sayfasına geç ve request id yi gönder
                                    val intent = Intent(requireContext(), RequestDetailAcademicianActivity::class.java).apply {
                                        putExtra("request", clickedRequest)
                                    }
                                    startActivity(intent)
                                }
                            )

                            // RecyclerView’a adapter ve layout manager atanır
                            binding.requestAcademicianRecyclerView.adapter = adapter
                            binding.requestAcademicianRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        },
                        onFailure = {
                            Toast.makeText(requireContext(), "Veri alınamadı", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "Akademisyen bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Akademisyen bilgisi alınamadı: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

        // "Yeni Talep Oluştur" butonuna tıkla
        binding.createAcademicianRequest.setOnClickListener {
            val intent = Intent(requireContext(), RequestCategoryActivity::class.java)
            startActivity(intent)
        }
    }
}
