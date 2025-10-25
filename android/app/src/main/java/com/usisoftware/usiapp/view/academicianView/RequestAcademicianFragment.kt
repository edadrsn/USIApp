package com.usisoftware.usiapp.view.academicianView

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.usisoftware.usiapp.databinding.FragmentRequestAcademicianBinding
import com.usisoftware.usiapp.view.adapter.RequestAdapter
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.repository.RequestFirebase
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
    private lateinit var detailLauncher: ActivityResultLauncher<Intent>


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

        //Sayfa açıldığında talepleri yükle
        loadRequests()

        //Launcher bir sayfayı başlatır ve o sayfa kapandığında geriye bir sonuç döner
        detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isDeleted = result.data?.getBooleanExtra("deleted", false) ?: false
                if (isDeleted) {
                    loadRequests()  // Eğer bir talep silindiyse listeyi yeniden yükle
                }
            }
        }



        // "Yeni Talep Oluştur" butonuna tıkla
        binding.createAcademicianRequest.setOnClickListener {
            val intent = Intent(requireContext(), RequestCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRequests(){
        val userEmail = auth.currentUser?.email ?: return
        // Önce AcademicianInfo'dan documentId bul
        db.collection("AcademicianInfo")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val academicianDocId = querySnapshot.documents[0].id

                    // Requests'ten bu academicanDocId ile talepleri getir
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
                                    requestType = doc.getBoolean("requestType") ?: false,
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
                                onItemClick = { clickedRequest ->
                                    // Talep kartına tıklanınca detay sayfasına geç ve request id yi gönder
                                    val intent = Intent(requireContext(), RequestDetailAcademicianActivity::class.java)
                                    intent.putExtra("request", clickedRequest)
                                    detailLauncher.launch(intent)
                                }
                            )

                            // RecyclerView’a adapter ve layout manager atanır
                            binding.requestAcademicianRecyclerView.adapter = adapter
                            binding.requestAcademicianRecyclerView.layoutManager =
                                LinearLayoutManager(requireContext())
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

    }

}