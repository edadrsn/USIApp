package com.usisoftware.usiapp.view.academicianView

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.usisoftware.usiapp.databinding.FragmentOpenRequestsBinding
import com.usisoftware.usiapp.view.adapter.OpenRequestsAdapter
import com.usisoftware.usiapp.view.industryView.CreateRequestActivity
import com.usisoftware.usiapp.view.industryView.RequestDetailActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.studentView.RequestDetailStudentActivity
import com.usisoftware.usiapp.view.studentView.StudentRequestActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


class OpenRequestsFragment : Fragment() {

    private var _binding: FragmentOpenRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: OpenRequestsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOpenRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //Talep oluşturma sayfasına git
        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        binding.btnGoToRequests.setOnClickListener {
            when (userType) {
                "academician" -> {
                    startActivity(Intent(requireContext(), RequestCategoryActivity::class.java))
                }

                "student" -> {
                    startActivity(Intent(requireContext(), StudentRequestActivity::class.java))
                }

                "industry" -> {
                    startActivity(Intent(requireContext(), CreateRequestActivity::class.java))
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Kullanıcı tipi tanımlanamadı!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Firestore'dan sadece "onaylanan" durumundaki açık talepleri çek
        db.collection("Requests")
            .whereEqualTo("requestType", true)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                // Belge verilerini Request modeline dönüştür
                val requestList = snapshot.documents.map { doc ->
                    val applyUsersCount=doc.get("applyUsers") as? Map<*, *> ?: emptyMap<Any, Any>()

                    Request(
                        id = doc.id,
                        title = doc.getString("requestTitle") ?: "",
                        message = doc.getString("requestMessage") ?: "",
                        date = doc.getString("createdDate") ?: "",
                        status = doc.getString("status") ?: "",
                        requesterId = doc.getString("requesterID") ?: "",
                        selectedCategories = doc.get("selectedCategories") as? List<String> ?: emptyList(),
                        requesterAddress = doc.getString("requesterAddress") ?: "",
                        requesterName = doc.getString("requesterName") ?: "",
                        requesterCategories = doc.getString("requesterCategories") ?: "",
                        requesterEmail = doc.getString("requesterEmail") ?: "",
                        requesterPhone = doc.getString("requesterPhone") ?: "",
                        adminMessage = doc.getString("adminMessage") ?: "",
                        adminDocumentId = doc.getString("adminDocumentId") ?: "",
                        requesterImage = doc.getString("requesterImage") ?: "",
                        requestCategory = doc.getString("requestCategory") ?: "",
                        requesterType = doc.getString("requesterType") ?: "",
                        requestType = doc.getBoolean("requestType") ?: false,
                        applyUserCount = applyUsersCount.size
                    )
                }

                snapshot.documents.forEach { doc ->
                    Log.d("PendingRequests", "Doc: ${doc.data}")
                }


                // Tarihleri sıralama (dd.MM.yyyy formatı)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))

                val sortedRequestList = requestList.sortedByDescending { request ->
                    request.date.takeIf { it.isNotEmpty() }?.let { dateStr ->
                        try {
                            dateFormat.parse(dateStr)?.time
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                val mutableRequests = sortedRequestList.toMutableList()

                // Adapter tanımla ve her item'a tıklanınca detay ekranına geç
                adapter = OpenRequestsAdapter(
                    mutableRequests,
                    onItemClick = { clickedRequest ->

                        if(userType=="academician"){
                            getAcademicianDocumentId{docId ->
                                if(docId == clickedRequest.requesterId){
                                    val intent = Intent(requireContext(), RequestDetailAcademicianActivity::class.java)
                                    intent.putExtra("request", clickedRequest)
                                    startActivity(intent)
                                }
                                //Açık talep detay sayfasına git
                                else{
                                    val intent = Intent(requireContext(), OpenRequestsDetailActivity::class.java)
                                    intent.putExtra("request", clickedRequest)
                                    startActivity(intent)
                                }
                            }


                        }
                        else {
                            //Öğrenci ve sanayici için kendi detay sayfasına git
                            if (clickedRequest.requesterId == auth.currentUser?.uid) {
                                when (userType) {
                                    "student" -> {
                                        val intent = Intent(
                                            requireContext(),
                                            RequestDetailStudentActivity::class.java
                                        )
                                        intent.putExtra("request", clickedRequest)
                                        startActivity(intent)
                                    }

                                    "industry" -> {
                                        val intent = Intent(
                                            requireContext(),
                                            RequestDetailActivity::class.java
                                        )
                                        intent.putExtra("request", clickedRequest)
                                        startActivity(intent)
                                    }
                                }
                            }

                            //Açık talep detay sayfasına git
                            else{
                                val intent = Intent(requireContext(), OpenRequestsDetailActivity::class.java)
                                intent.putExtra("request", clickedRequest)
                                startActivity(intent)
                            }
                        }

                    }
                )

                // RecyclerView ayarları
                binding.openRequestRecyclerView.adapter = adapter
                binding.openRequestRecyclerView.layoutManager =
                    LinearLayoutManager(requireContext())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Veri alınamadı", Toast.LENGTH_SHORT).show()
            }

    }

    //Akademisyen doküman id yi bul
    fun getAcademicianDocumentId(
        userEmail: String = auth.currentUser?.email ?: "",  //giriş yapan kullanıcının mailini al
        onResult: (documentId: String?) -> Unit
    ) {          //Fonksiyonun içinde işlemi bitirdikten sonra, sonucu dışarı “geri göndermek” için kullanırız

        //email boşsa geriye null dönsün
        if (userEmail.isBlank()) {
            onResult(null)
            return
        }

        db.collection("AcademicianInfo")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {  //doküman boş değilse
                    onResult(documents.documents[0].id)  // İlk bulunan dokümanın id'sini döndür
                } else {
                    onResult(null)  //doküman boşsa onResult metodunu çağır geriye null dönsün
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "getAcademicianDocumentId error: ${e.message}")
                onResult(null)
            }
    }

}