package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityAdminPanelBinding

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var authorityId: String
    private lateinit var universityName: String
    private var academicianDomain = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        loadAuthorityDomains()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadAuthorityDomains()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }


    // Admin domaini Authoritiesden bul
    private fun loadAuthorityDomains() {

        val adminEmail = auth.currentUser?.email ?: return
        val adminDomain = adminEmail.substringAfter("@")

        db.collection("Authorities")
            .get()
            .addOnSuccessListener { snapshot ->

                var found = false

                for (doc in snapshot.documents) {

                    val docAcademician = doc.getString("academician") ?: continue

                    if (adminDomain == docAcademician) {
                        authorityId = doc.id
                        universityName = doc.getString("universityName") ?: ""
                        academicianDomain = docAcademician

                        binding.universityNameTextview.text = universityName
                        found = true
                        break
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Admin yetkisi bulunamadı!", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                loadAcademicianCount()
                loadStudentCount()
                loadIndustryCount()
                loadRequestStatistics()
            }
    }

    // Akademisyen sayısı
    private fun loadAcademicianCount() {

        db.collection("Academician")
            .get()
            .addOnSuccessListener { docs ->

                val totalCountAcademicians = docs.size()

                binding.cardSummary.academicianCountText.text = "$totalCountAcademicians"

                var count = 0

                for (doc in docs) {
                    val email = doc.getString("email") ?: continue
                    val domain = email.substringAfter("@")
                    if (domain == academicianDomain) count++
                }

                val safeDenom = if (count == 0) 1 else count

                binding.totalAcademician.text = "Toplam: $count"

                binding.progressCircleAcademician.apply {
                    mode = ProgressCircleView.CircleMode.TOTAL
                    numerator = count
                    denominator = safeDenom
                    percentage = if (count == 0) 0f else 1f
                    setBaseColor("#e5f3fa")
                    setProgressColor("#1A9AAF")
                    animatePercentage(percentage, 500)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Akademisyenler alınırken hata oluştu!", Toast.LENGTH_LONG)
                    .show()
            }
    }

    //Öğrenci sayısı
    private fun loadStudentCount() {

        db.collection("Students")
            .whereEqualTo("universityName", universityName)
            .get()
            .addOnSuccessListener { docs ->

                val count = docs.size()
                val safeDenom = if (count == 0) 1 else count

                binding.cardSummary.studentCountText.text = "$count"
                binding.totalStudent.text = "Toplam: $count"

                binding.progressCircleStudent.apply {
                    mode = ProgressCircleView.CircleMode.TOTAL
                    numerator = count
                    denominator = safeDenom
                    percentage = if (count == 0) 0f else 1f
                    setBaseColor("#fbb1fc")
                    setProgressColor("#741d75")
                    animatePercentage(percentage, 500)
                }
            }
    }


    //Sanayici sayısı
    private fun loadIndustryCount() {

        db.collection("Industry")
            .get()
            .addOnSuccessListener { docs ->

                val count = docs.size()
                val safeDenom = if (count == 0) 1 else count

                binding.cardSummary.industryCountText.text = "$count"
                binding.totalIndustry.text = "Toplam: $count"

                binding.progressCircleIndustry.apply {
                    mode = ProgressCircleView.CircleMode.TOTAL
                    numerator = count
                    denominator = safeDenom
                    percentage = if (count == 0) 0f else 1f
                    setBaseColor("#fdf0e6")
                    setProgressColor("#f26b1a")
                    animatePercentage(percentage, 500)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Sanayici/Girişimci verileri alınırken hata oluştu!", Toast.LENGTH_LONG)
                    .show()
            }
    }

    //Talep sayısı
    private fun loadRequestStatistics() {

        db.collection("Requests")
            .get()
            .addOnSuccessListener { docs ->

                var total = 0
                var approvedCount = 0

                for (doc in docs) {
                    val statusMap = doc.get("status") as? Map<*, *> ?: continue

                    val status = statusMap[authorityId]
                    if (status != null) {
                        total++
                        if (status == "approved") approvedCount++
                    }
                }

                val safeDenom = if (total == 0) 1 else total
                val approvedPercent = if (total > 0) approvedCount.toFloat() / total else 0f

                binding.cardSummary.requestCountText.text = "$total"
                binding.totalRequest.text = "Toplam: $total"
                binding.totalApproved.text="Toplam: $approvedCount"

                binding.progressCircleTotalRequest.apply {
                    mode = ProgressCircleView.CircleMode.TOTAL
                    numerator = total
                    denominator = safeDenom
                    percentage = if (total == 0) 0f else 1f
                    setBaseColor("#f9edfb")
                    setProgressColor("#ac4fde")
                    animatePercentage(percentage, 500)
                }

                binding.cardSummary.approvedCountText.text = "$approvedCount"

                binding.progressCircleApproved.apply {
                    mode = ProgressCircleView.CircleMode.APPROVED
                    numerator = approvedCount
                    denominator = safeDenom
                    percentage = approvedPercent
                    setBaseColor("#e8faec")
                    setProgressColor("#32c85c")
                    animatePercentage(approvedPercent, 500)
                }
            }
    }


    //Bekleyen taleplere git
    fun pendingRequests(view: View) {
        startActivity(Intent(this, PendingRequestsActivity::class.java))
    }

    //Eski taleplere git
    fun oldRequests(view: View) {
        startActivity(Intent(this, OldRequestsActivity::class.java))
    }

    //Admin kullanıcı ekle sayfasına git
    fun adminUser(view: View) {
        startActivity(Intent(this, AddAdminUserActivity::class.java))
    }

    //Şikayetler sayfasına git
    fun reportsAndComplaints(view: View) {
        startActivity(Intent(this, ReportsAndComplaintsActivity::class.java))
    }

    //Geri dön
    fun goToMain(view: View) {
        finish()
    }
}