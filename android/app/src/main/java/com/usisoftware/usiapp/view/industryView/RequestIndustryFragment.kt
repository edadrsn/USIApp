package com.usisoftware.usiapp.view.industryView

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.FragmentRequestIndustryBinding
import com.usisoftware.usiapp.view.adapter.RequestAdapter
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.repository.RequestFirebase
import java.text.SimpleDateFormat
import java.util.Locale

class RequestIndustryFragment : Fragment() {

    private var _binding: FragmentRequestIndustryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: RequestAdapter
    private lateinit var detailLauncher:ActivityResultLauncher<Intent>


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

        //Sayfa açıldığında talepleri yükle
        loadRequests()

        //SwipeRefresh: Aşağı çekince yenileme
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRequests() // Verileri yeniden yükle
            binding.swipeRefreshLayout.isRefreshing = false // Animasyonu kapat
        }

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
        binding.createRequest.setOnClickListener {
            val intent = Intent(requireContext(), CreateRequestActivity::class.java)
            startActivity(intent)
        }
    }

    //Talepleri Yükle
    fun loadRequests(){
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }
        val userId = currentUser.uid

        // Firestore'dan kullanıcının taleplerini çek
        RequestFirebase.getUserRequest(
            db,
            userId,
            onSuccess = { documents ->
                if (!isAdded || view == null) return@getUserRequest

                // Firestore'dan gelen belgeleri Request nesnelerine dönüştür
                val requestList = documents.map { doc ->
                    Request(
                        id = doc.id,
                        title = doc.getString("requestTitle") ?: "",
                        message = doc.getString("requestMessage") ?: "",
                        date = doc.getString("createdDate") ?: "",
                        status = doc.get("status") as? Map<String, String> ?: emptyMap(),
                        requesterId = doc.getString("requesterID") ?: "",
                        selectedCategories = doc.get("selectedCategories") as? List<String> ?: emptyList(),
                        requesterName = doc.getString("requesterName") ?: "",
                        requesterCategories = doc.getString("requesterCategories") ?: "",
                        requesterEmail = doc.getString("requesterEmail") ?: "",
                        requesterPhone = doc.getString("requesterPhone") ?: "",
                        adminMessage = doc.getString("adminMessage") ?: "",
                        adminDocumentId = doc.getString("adminDocumentId") ?: "",
                        requesterImage = doc.getString("requesterImage") ?: "",
                        requestType = doc.getBoolean("requestType") ?: false
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
                        val intent = Intent(requireContext(), RequestDetailActivity::class.java)
                        intent.putExtra("request", clickedRequest)
                        detailLauncher.launch(intent)
                    }
                )

                // RecyclerView’a adapter ve layout manager atanır
                binding.requestRecyclerView.adapter = adapter
                binding.requestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            },
            onFailure = { e ->
                Toast.makeText(requireContext(), "Veri alınamadı", Toast.LENGTH_SHORT).show()
                Log.e("RequestIndustryFragment","Hata: ${e.localizedMessage}")
            }
        )

    }

    // View yok edilirken bellekte sızıntı olmaması için binding sıfırla
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
