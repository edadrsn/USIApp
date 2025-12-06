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

    private var studentDomain = ""
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

        val adminEmail = auth.currentUser?.email

        if (adminEmail.isNullOrEmpty() || !adminEmail.contains("@")) {
            Toast.makeText(this, "Geçersiz admin email!", Toast.LENGTH_LONG).show()
            return
        }

        val adminDomain = adminEmail.substringAfter("@")

        try {
            db.collection("Authorities")
                .get()
                .addOnSuccessListener { snapshot ->

                    var found = false

                    for (doc in snapshot.documents) {
                        val docStudent = doc.getString("student") ?: ""
                        val docAcademician = doc.getString("academician") ?: ""

                        if (docAcademician.isEmpty() || docStudent.isEmpty())
                            continue

                        if (adminDomain == docAcademician) {
                            studentDomain = docStudent
                            academicianDomain = docAcademician
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        Toast.makeText(this, "Admin domainine uygun üniversite bulunamadı!", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    // Domainler bulundu → sayım başlat
                    loadAcademicianCount()
                    loadStudentCount()
                    loadIndustryCount()
                    loadRequestStatistics()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Authorities alınırken hata oluştu!", Toast.LENGTH_LONG).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Authority yükleme hatası: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    // Akademisyen sayısı
    private fun loadAcademicianCount() {

        db.collection("Academician")
            .get()
            .addOnSuccessListener { docs ->

            var count = 0

            for (doc in docs) {
                val email = doc.getString("email") ?: continue
                val domain = email.substringAfter("@")
                if (domain == academicianDomain) count++
            }

            val safeDenom = if (count == 0) 1 else count

            binding.cardSummary.academicianCountText.text = "$count"
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
                Toast.makeText(this, "Akademisyenler alınırken hata oluştu!", Toast.LENGTH_LONG).show()
            }
    }

    //Öğrenci sayısı
    private fun loadStudentCount() {

        db.collection("Students")
            .get()
            .addOnSuccessListener { docs ->

            var count = 0

            for (doc in docs) {
                val email = doc.getString("studentEmail") ?: continue
                val domain = email.substringAfter("@")
                if (domain == studentDomain) count++
            }

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
            .addOnFailureListener {
                Toast.makeText(this, "Öğrenciler alınırken hata oluştu!", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Sanayici verileri alınırken hata oluştu!", Toast.LENGTH_LONG).show()
            }
    }

   //Talep sayısı
    private fun loadRequestStatistics() {

        db.collection("Requests")
            .get()
            .addOnSuccessListener { docs ->

            val total = docs.size()
            val safeDenomTotal = if (total == 0) 1 else total

            binding.cardSummary.requestCountText.text = "$total"
            binding.totalRequest.text = "Toplam: $total"

            binding.progressCircleTotalRequest.apply {
                mode = ProgressCircleView.CircleMode.TOTAL
                numerator = total
                denominator = safeDenomTotal
                percentage = if (total == 0) 0f else 1f
                setBaseColor("#f9edfb")
                setProgressColor("#ac4fde")
                animatePercentage(percentage, 500)
            }

            // --- Onaylanan ---
            var approvedCount = 0

            for (doc in docs) {
                val status = doc.get("status")
                if (status is Map<*, *> && status.values.contains("approved")) {
                    approvedCount++
                }
            }

            val approvedPercent = if (total > 0) approvedCount.toFloat() / total else 0f

            binding.cardSummary.approvedCountText.text = "$approvedCount"
            binding.totalApproved.text = "Toplam: $approvedCount"

            binding.progressCircleApproved.apply {
                mode = ProgressCircleView.CircleMode.APPROVED
                numerator = approvedCount
                denominator = safeDenomTotal
                percentage = approvedPercent
                setBaseColor("#e8faec")
                setProgressColor("#32c85c")
                animatePercentage(approvedPercent, 500)
            }
        }
            .addOnFailureListener {
                Toast.makeText(this, "Talepler alınırken hata oluştu!", Toast.LENGTH_LONG).show()
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
