package com.example.usiapp.view.industryView

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usiapp.databinding.FragmentRequestIndustryBinding
import com.example.usiapp.view.adapter.RequestAdapter
import com.example.usiapp.view.model.Request
import com.example.usiapp.view.repository.RequestFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class RequestIndustryFragment : Fragment() {

    private var _binding: FragmentRequestIndustryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: RequestAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestIndustryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // Firestore'dan kullanıcının taleplerini çek
        RequestFirebase.getUserRequest(
            db,
            userId,
            onSuccess = { documents ->
                // Firestore'dan gelen belgeleri Request nesnelerine dönüştür
                val requestList = documents.map { doc ->
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
                        adminMessage = doc.getString("adminMessage") ?: "",
                        adminDocumentId = doc.getString("adminDocumentId") ?: "",
                        requesterImage = doc.getString("requesterImage") ?: ""
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
                            .setMessage("Silmek istediğinize emin misiniz ?")
                            .setPositiveButton("Evet") { _, _ ->

                                db.collection("Requests").document(requestToDelete.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Talep bilgisi silindi",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        mutableRequests.remove(requestToDelete)
                                        adapter.notifyDataSetChanged()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Silme başarısız",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .setNegativeButton("Hayır", null)
                            .show()
                    },
                    onItemClick = { clickedRequest ->

                        // Talep kartına tıklanınca detay sayfasına geç ve request id yi gönder
                        val intent =
                            Intent(requireContext(), RequestDetailActivity::class.java).apply {
                                putExtra("request", clickedRequest)
                            }
                        startActivity(intent)
                    }
                )

                // RecyclerView’a adapter ve layout manager atanır
                binding.requestRecyclerView.adapter = adapter
                binding.requestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            },
            onFailure = {
                Toast.makeText(requireContext(), "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        // "Yeni Talep Oluştur" butonuna tıkla
        binding.createRequest.setOnClickListener {
            val intent = Intent(requireContext(), CreateRequestActivity::class.java)
            startActivity(intent)
        }
    }

    // View yok edilirken bellekte sızıntı olmaması için binding sıfırla
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

