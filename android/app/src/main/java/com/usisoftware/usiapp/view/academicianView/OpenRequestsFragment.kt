package com.usisoftware.usiapp.view.academicianView

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.FragmentOpenRequestsBinding
import com.usisoftware.usiapp.view.adapter.OpenRequestsAdapter
import com.usisoftware.usiapp.view.industryView.CreateRequestActivity
import com.usisoftware.usiapp.view.industryView.RequestDetailActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.studentView.RequestDetailStudentActivity
import com.usisoftware.usiapp.view.studentView.StudentRequestActivity
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

        val isFromOpenRequestsActivity = activity is OpenRequestsActivity
        binding.btnGoToRequests.visibility =
            if (isFromOpenRequestsActivity) View.GONE else View.VISIBLE

        binding.btnGoToRequests.setOnClickListener {
            val currentUser = auth.currentUser

            if (currentUser == null || userType == null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Giriş gerekli")
                    .setMessage("Bu işlemi yapabilmek için lütfen giriş yapın.")
                    .setPositiveButton("Giriş Yap") { _, _ ->
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                    .setNegativeButton("İptal", null)
                    .show()
                return@setOnClickListener
            }

            when (userType) {
                "academician" -> startActivity(Intent(requireContext(), RequestCategoryActivity::class.java))
                "student" -> startActivity(Intent(requireContext(), StudentRequestActivity::class.java))
                "industry" -> startActivity(Intent(requireContext(), CreateRequestActivity::class.java))
                else -> Toast.makeText(requireContext(), "Kullanıcı tipi tanımlanamadı!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Akademisyen id bulma
    fun getAcademicianDocumentId(
        userEmail: String = auth.currentUser?.email ?: "",
        onResult: (documentId: String?) -> Unit
    ) {

        if (userEmail.isBlank()) {
            onResult(null)
            return
        }

        db.collection("AcademicianInfo")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    onResult(documents.documents[0].id)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // Kullanıcı id'si bulma
    fun findId(userEmail: String = auth.currentUser?.email ?: "", onResult: (documentId: String?) -> Unit) {

        if (userEmail.isBlank()) {
            onResult(null)
            return
        }

        val domain = userEmail.substringAfterLast("@")

        when (domain) {
            "ahievran.edu.tr" -> {
                db.collection("AcademicianInfo")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { docs ->
                        onResult(if (!docs.isEmpty) docs.documents[0].id else null)
                    }
                    .addOnFailureListener { onResult(null) }
            }

            "ogr.ahievran.edu.tr" -> {
                db.collection("Students")
                    .whereEqualTo("studentEmail", userEmail)
                    .get()
                    .addOnSuccessListener { docs ->
                        onResult(if (!docs.isEmpty) docs.documents[0].id else null)
                    }
                    .addOnFailureListener { onResult(null) }
            }

            else -> {
                db.collection("Industry")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { docs ->
                        onResult(if (!docs.isEmpty) docs.documents[0].id else null)
                    }
                    .addOnFailureListener { onResult(null) }
            }
        }
    }

    // ENGEL EKLEME – sadece engelleyen kişinin dokümanına
    fun blockUser(blockedUserId: String, blockerUserId: String) {
        val collections = listOf("AcademicianInfo", "Students", "Industry")

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

        val collections = listOf("AcademicianInfo", "Students", "Industry")

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

    fun loadRequests(){
        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        // Kullanıcı ID'sini bul
        findId { currentUserId ->

            if (currentUserId == null) return@findId

            // Engellediklerimi al
            getBlockedUsers(currentUserId) { blockedList ->

                db.collection("Requests")
                    .whereEqualTo("requestType", true)
                    .whereEqualTo("status", "approved")
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val requestList = snapshot.documents.map { doc ->

                            val applyUsersCount = doc.get("applyUsers") as? Map<*, *> ?: emptyMap<Any, Any>()

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

                        // Engellenen kişilerin taleplerini gizle
                        val filteredRequests = requestList.filter { request ->
                            !blockedList.contains(request.requesterId)
                        }

                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))

                        val sortedRequestList = filteredRequests.sortedByDescending { req ->
                            req.date.takeIf { it.isNotEmpty() }?.let {
                                runCatching { dateFormat.parse(it)?.time }.getOrNull()
                            }
                        }

                        val mutableRequests = sortedRequestList.toMutableList()

                        adapter = OpenRequestsAdapter(
                            mutableRequests,

                            onItemClick = { clickedRequest ->

                                if (userType == "academician") {
                                    getAcademicianDocumentId { docId ->
                                        if (docId == clickedRequest.requesterId) {
                                            startActivity(Intent(requireContext(), RequestDetailAcademicianActivity::class.java).putExtra("request", clickedRequest))
                                        } else {
                                            startActivity(Intent(requireContext(), OpenRequestsDetailActivity::class.java).putExtra("request", clickedRequest))
                                        }
                                    }
                                } else {
                                    if (clickedRequest.requesterId == currentUserId) {

                                        when (userType) {
                                            "student" -> { startActivity(Intent(requireContext(), RequestDetailStudentActivity::class.java).putExtra("request", clickedRequest)) }

                                            "industry" -> { startActivity(Intent(requireContext(), RequestDetailActivity::class.java).putExtra("request", clickedRequest)) } }
                                    } else { startActivity(Intent(requireContext(), OpenRequestsDetailActivity::class.java).putExtra("request", clickedRequest)) } }
                            },

                            // Şikayet et
                            onReportClick = { clickedRequest ->
                                startActivity(
                                    Intent(requireContext(), ReportActivity::class.java)
                                        .putExtra("request", clickedRequest)
                                )
                            },

                            // Engelle
                            onBlockClick = { clickedRequest ->
                                findId { blockerUserId ->
                                    if (blockerUserId == null) {
                                        Toast.makeText(requireContext(), "Kimlik alınamadı!", Toast.LENGTH_SHORT).show()
                                        return@findId
                                    }

                                    val blockedUserId = clickedRequest.requesterId
                                    blockUser(blockedUserId, blockerUserId)
                                }
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

}
