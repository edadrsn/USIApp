package com.usisoftware.usiapp.view.academicianView

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.FragmentOpenRequestsBinding
import com.usisoftware.usiapp.view.adapter.OpenRequestsAdapter
import com.usisoftware.usiapp.view.industryView.RequestDetailActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.studentView.RequestDetailStudentActivity
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

        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        // İlk verileri yükle
        loadRequests()

        // SwipeRefreshLayout ekle
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRequests()  // Verileri tekrar çek
            binding.swipeRefreshLayout.isRefreshing = false // Animasyonu kapat
        }


        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            // Kullanıcı giriş yapmadı → geri butonu görünsün
            binding.back.visibility = View.VISIBLE
            binding.back.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        } else {
            // Kullanıcı giriş yaptı → geri butonu gizlensin
            binding.back.visibility = View.GONE
        }
    }


    // ENGEL EKLEME – sadece engelleyen kişinin dokümanına
    fun blockUser(blockedUserId: String, blockerUserId: String) {
        val collections = listOf("Academician", "Students", "Industry")

        for (collection in collections) {

            val userDoc = db.collection(collection).document(blockerUserId)

            userDoc.get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {

                        val current = doc.get("blockedUsers") as? MutableList<String> ?: mutableListOf()

                        if (!current.contains(blockedUserId))
                            current.add(blockedUserId)

                        userDoc.update("blockedUsers", current)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Kullanıcı engellendi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Engelleme hatası!", Toast.LENGTH_SHORT).show()
                            }

                        return@addOnSuccessListener
                    }
                }
        }
    }

    // Engellenenleri gösterme
    fun getBlockedUsers(userId: String, onResult: (List<String>) -> Unit) {

        val collections = listOf("Academician", "Students", "Industry")

        for (collection in collections) {
            db.collection(collection)
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val list = doc.get("blockedUsers") as? List<String> ?: emptyList()
                        onResult(list)
                    }
                }
        }
    }

    // REQUESTS YÜKLEME
    fun loadRequests() {
        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            loadRequestsWithoutBlocking()
            return
        }

        // Önce Authorities koleksiyonundan üniversite adlarını al
        db.collection("Authorities").get()
            .addOnSuccessListener { authSnapshot ->

                val universityList = authSnapshot.documents.map { it.id }

                getBlockedUsers(currentUserId) { blockedList ->

                    db.collection("Requests")
                        .whereEqualTo("requestType", true)
                        .get()
                        .addOnSuccessListener { snapshot ->

                            val requestList = snapshot.documents.mapNotNull { doc ->

                                val statusMap = doc.get("status") as? Map<String, String> ?: emptyMap()

                                // Bu requestte herhangi bir üniversite "approved" mu?
                                val hasApproved = universityList.any { uni ->
                                    statusMap[uni] == "approved"
                                }

                                if (!hasApproved) return@mapNotNull null

                                val applyUsersCount =
                                    doc.get("applyUsers") as? Map<*, *> ?: emptyMap<Any, Any>()

                                Request(
                                    id = doc.id,
                                    title = doc.getString("requestTitle") ?: "",
                                    message = doc.getString("requestMessage") ?: "",
                                    date = doc.getString("createdDate") ?: "",
                                    status = statusMap,
                                    requesterId = doc.getString("requesterID") ?: "",
                                    selectedCategories = doc.get("selectedCategories") as? List<String>
                                        ?: emptyList(),
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

                            // Engellenen kullanıcılara göre filtrele
                            val filteredRequests = requestList.filter { request ->
                                !blockedList.contains(request.requesterId) ||
                                        request.requesterId == currentUserId
                            }

                            // Tarihe göre sırala
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
                            val sortedRequestList = filteredRequests.sortedByDescending { req ->
                                req.date.takeIf { it.isNotEmpty() }?.let {
                                    runCatching { dateFormat.parse(it)?.time }.getOrNull()
                                }
                            }

                            // Boş kontrolü giriş yapan kullanıcılar için
                            if (sortedRequestList.isEmpty()) {
                                binding.openRequestRecyclerView.visibility = View.GONE
                                binding.emptyText.visibility = View.VISIBLE
                            } else {
                                binding.openRequestRecyclerView.visibility = View.VISIBLE
                                binding.emptyText.visibility = View.GONE
                            }

                            adapter = OpenRequestsAdapter(
                                sortedRequestList.toMutableList(),

                                onItemClick = { clickedRequest ->
                                    if (clickedRequest.requesterId == currentUserId) {
                                        when (userType) {
                                            "academician" -> startActivity(
                                                Intent(requireContext(), RequestDetailAcademicianActivity::class.java)
                                                    .putExtra("request", clickedRequest)
                                            )

                                            "student" -> startActivity(
                                                Intent(requireContext(), RequestDetailStudentActivity::class.java)
                                                    .putExtra("request", clickedRequest)
                                            )

                                            "industry" -> startActivity(
                                                Intent(requireContext(), RequestDetailActivity::class.java)
                                                    .putExtra("request", clickedRequest)
                                            )
                                        }
                                    } else {
                                        startActivity(
                                            Intent(requireContext(), OpenRequestsDetailActivity::class.java)
                                                .putExtra("request", clickedRequest)
                                        )
                                    }
                                },

                                onReportClick = { clickedRequest ->
                                    startActivity(
                                        Intent(requireContext(), ReportActivity::class.java)
                                            .putExtra("request", clickedRequest)
                                    )
                                },

                                onBlockClick = { clickedRequest ->
                                    val blockerUserId = currentUserId
                                    val blockedUserId = clickedRequest.requesterId
                                    blockUser(blockedUserId, blockerUserId)
                                }
                            )

                            binding.openRequestRecyclerView.adapter = adapter
                            binding.openRequestRecyclerView.layoutManager =
                                LinearLayoutManager(requireContext())
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Veri alınamadı", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    fun loadRequestsWithoutBlocking() {

        // Önce Authorities koleksiyonundan üniversiteleri al
        db.collection("Authorities").get()
            .addOnSuccessListener { authSnapshot ->

                val universityList = authSnapshot.documents.map { it.id }

                db.collection("Requests")
                    .whereEqualTo("requestType", true)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val requestList = snapshot.documents.mapNotNull { doc ->

                            val statusMap = doc.get("status") as? Map<String, String> ?: emptyMap()

                            // Herhangi bir üniversite onaylamış mı?
                            val hasApproved = universityList.any { uni ->
                                statusMap[uni] == "approved"
                            }

                            if (!hasApproved) return@mapNotNull null

                            val applyUsersCount =
                                doc.get("applyUsers") as? Map<*, *> ?: emptyMap<Any, Any>()

                            Request(
                                id = doc.id,
                                title = doc.getString("requestTitle") ?: "",
                                message = doc.getString("requestMessage") ?: "",
                                date = doc.getString("createdDate") ?: "",
                                status = statusMap,
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

                        // Tarihe göre sırala
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
                        val sortedList = requestList.sortedByDescending { req ->
                            req.date.takeIf { it.isNotEmpty() }?.let {
                                runCatching { dateFormat.parse(it)?.time }.getOrNull()
                            }
                        }

                        // Boş kontrolü giriş yapmayan kullanıcı için
                        if (sortedList.isEmpty()) {
                            binding.openRequestRecyclerView.visibility = View.GONE
                            binding.emptyText.visibility = View.VISIBLE
                        } else {
                            binding.openRequestRecyclerView.visibility = View.VISIBLE
                            binding.emptyText.visibility = View.GONE
                        }


                        adapter = OpenRequestsAdapter(
                            sortedList.toMutableList(),
                            onItemClick = { clickedRequest ->
                                startActivity(
                                    Intent(requireContext(), OpenRequestsDetailActivity::class.java)
                                        .putExtra("request", clickedRequest)
                                )
                            },
                            onReportClick = { clickedRequest ->
                                startActivity(Intent(requireContext(), ReportActivity::class.java).putExtra("request", clickedRequest))
                            },
                            onBlockClick = {
                                Toast.makeText(requireContext(), "Engellemek için giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
                            }
                        )

                        binding.openRequestRecyclerView.adapter = adapter
                        binding.openRequestRecyclerView.layoutManager =
                            LinearLayoutManager(requireContext())
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Veri alınamadı", Toast.LENGTH_SHORT).show()
                    }
            }
    }


}
