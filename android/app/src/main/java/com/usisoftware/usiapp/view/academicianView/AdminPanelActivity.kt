package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityAdminPanelBinding

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // -------------------- KULLANICI İSTATİSTİKLERİ --------------------
        // Tüm akademisyenleri getir
        db.collection("AcademicianInfo").get()
            .addOnSuccessListener { academicianDocs ->
                val totalAcademicians = academicianDocs.size() // Tüm akademisyenlerin sayısı
                binding.cardSummary.academicianCountText.text =
                    "$totalAcademicians" // Karttaki sayı
                binding.totalAcademician.text = "Toplam: $totalAcademicians" // Alt yazı

                // Tüm domain kayıtlarını getir
                db.collection("UserDomains").get()
                    .addOnSuccessListener { allDomainDocs ->
                        val totalDomainCount =
                            allDomainDocs.size() // Tüm kullanıcı domain kayıt sayısı

                        // Sadece "ahievran.edu.tr" domainine sahip kullanıcılar (giriş yapan akademisyenler)
                        db.collection("UserDomains")
                            .whereEqualTo("domain", "ahievran.edu.tr")
                            .get()
                            .addOnSuccessListener { ahievranDocs ->
                                val academicianCount =
                                    ahievranDocs.size() // Giriş yapan akademisyen sayısı

                                db.collection("UserDomains")
                                    .whereEqualTo("domain", "ogr.ahievran.edu.tr")
                                    .get()
                                    .addOnSuccessListener { studentDocs ->
                                        val studentCount = studentDocs.size()

                                        val industryCount =
                                            totalDomainCount - (academicianCount + studentCount) // Sanayici sayısı

                                        // Sanayici sayısını yazdır
                                        binding.totalIndustry.text = "Toplam: $industryCount"
                                        binding.cardSummary.industryCountText.text =
                                            "$industryCount"


                                        // Akademisyen yüzdesi (giriş yapan / toplam akademisyen)
                                        val academicianPercentage = if (totalAcademicians > 0)
                                            academicianCount.toFloat() / totalAcademicians.toFloat()
                                        else 0f

                                        binding.progressCircleAcademician.apply {
                                            percentage = academicianPercentage
                                            setBaseColor("#e5f3fa")
                                            setProgressColor("#1A9AAF")
                                            animatePercentage(percentage, 500)
                                            numerator = academicianCount
                                            denominator = totalAcademicians
                                        }

                                        // Sanayici yüzdesi (sanayici / toplam kullanıcı)
                                        val industryPercentage = if (totalDomainCount > 0)
                                            industryCount.toFloat() / totalDomainCount.toFloat()
                                        else 0f

                                        binding.progressCircleIndustry.apply {
                                            percentage = industryPercentage
                                            setBaseColor("#fdf0e6")
                                            setProgressColor("#f26b1a")
                                            animatePercentage(percentage, 500)
                                        }

                                        //Öğrenci yüzdesi (öğrenci / toplam kullanıcı)
                                        val studentPercantage = if (totalDomainCount > 0)
                                            studentCount.toFloat() / totalDomainCount.toFloat()
                                        else 0f

                                        binding.progressCircleStudent.apply {
                                            percentage = studentPercantage
                                            setBaseColor("#fbb1fc")
                                            setProgressColor("#741d75")
                                            animatePercentage(percentage, 500)
                                        }


                                        // Öğrenci sayısını yazdır
                                        binding.totalStudent.text = "Toplam: $studentCount"


                                        // Ortak Proje Talebi "Hayır" olan akademisyenleri getir
                                        db.collection("AcademicianInfo")
                                            .whereEqualTo("ortakProjeTalep", "Hayır")
                                            .get()
                                            .addOnSuccessListener { noDocs ->
                                                val noCount =
                                                    noDocs.size() // Hayır diyen akademisyen sayısı
                                                val projectJoinedCount =
                                                    academicianCount - noCount // Evet diyen sayısı

                                                // Projeye katılım yüzdesi ((Giriş yapan - Hayır) / Giriş yapan)
                                                val projectPercentage = if (academicianCount > 0) {
                                                    projectJoinedCount.toFloat() / academicianCount.toFloat()
                                                } else {
                                                    0f
                                                }

                                                binding.totalJoinedProject.text =
                                                    "Toplam: $academicianCount"
                                                binding.progressCircleJoinedProject.apply {
                                                    percentage = projectPercentage
                                                    setBaseColor("#f9edfb")
                                                    setProgressColor("#ac4fde") // DİKKAT: Çift # hatalı olabilir
                                                    animatePercentage(projectPercentage, 500)
                                                    numerator = projectJoinedCount
                                                    denominator = academicianCount
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "Firestore",
                                                    "Rejected talepler alınırken hata oluştu",
                                                    e
                                                )
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "Firestore",
                                            "ogr.ahievran.edu.tr domaini çekilirken hata oluştu",
                                            e
                                        )
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "Firestore",
                                    "ahievran.edu.tr domaini çekilirken hata oluştu",
                                    e
                                )
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Tüm domainleri çekerken hata oluştu", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Akademisyen sayısı alınırken hata oluştu", e)
            }

        // -------------------- TALEP İSTATİSTİKLERİ --------------------
        db.collection("Requests")
            .get()
            .addOnSuccessListener { requestsDoc ->
                val totalRequests = requestsDoc.size() // Toplam talep sayısı
                binding.cardSummary.requestCountText.text = "$totalRequests"
                binding.totalApproved.text = "Toplam: $totalRequests"
                binding.totalRejected.text = "Toplam: $totalRequests"

                // Onaylanmış ve reddedilmiş eski talepleri getir
                db.collection("OldRequests")
                    .get()
                    .addOnSuccessListener { oldRequestDocs ->
                        var approvedCount = 0
                        var rejectedCount = 0

                        for (document in oldRequestDocs) {
                            val status = document.getString("status")
                            when (status) {
                                "approved" -> approvedCount++
                                "rejected" -> rejectedCount++
                            }
                        }

                        binding.cardSummary.approvedCountText.text = "$approvedCount"
                        binding.cardSummary.rejectedCountText.text = "$rejectedCount"

                        // Progress yüzdeleri
                        val totalOld = approvedCount + rejectedCount
                        val approvedPercent =
                            if (totalOld > 0) approvedCount.toFloat() / totalRequests else 0f
                        val rejectedPercent =
                            if (totalOld > 0) rejectedCount.toFloat() / totalRequests else 0f

                        // Onaylanan yüzde
                        binding.progressCircleApproved.apply {
                            percentage = approvedPercent
                            setBaseColor("#e8faec")
                            setProgressColor("#32c85c")
                            animatePercentage(percentage, 500)
                            numerator = approvedCount
                            denominator = totalRequests
                        }

                        // Reddedilen yüzde
                        binding.progressCircleRejected.apply {
                            percentage = rejectedPercent
                            setBaseColor("#ffeaeb")
                            setProgressColor("#fc3c30")
                            animatePercentage(percentage, 500)
                            numerator = rejectedCount
                            denominator = totalRequests
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "OldRequests okunurken hata oluştu", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Talep sayısı alınırken hata oluştu", e)
            }

        // -------------------- ATANAN AKADEMİSYENLER --------------------
        // Giriş yapan akademisyenleri getir
        db.collection("UserDomains")
            .whereEqualTo("domain", "ahievran.edu.tr")
            .get()
            .addOnSuccessListener { ahievranDocs ->
                val academicianCount = ahievranDocs.size()

                // "approved" durumundaki eski talepleri al
                db.collection("OldRequests")
                    .whereEqualTo("status", "approved")
                    .get()
                    .addOnSuccessListener { approvedOldDocs ->
                        val uniqueAcademicians =
                            mutableSetOf<String>() // Tekrarsız akademisyen ID listesi

                        for (doc in approvedOldDocs) {
                            val academiciansList =
                                doc.get("selectedAcademiciansId") as? List<String> ?: emptyList()
                            uniqueAcademicians.addAll(academiciansList)
                        }

                        val uniqueCount = uniqueAcademicians.size
                        val percentageAppoint = if (academicianCount > 0) {
                            uniqueCount.toFloat() / academicianCount.toFloat()
                        } else {
                            0f
                        }

                        binding.totalAppointed.text = "Toplam: ${academicianCount}"
                        binding.progressCircleAppointed.apply {
                            percentage = percentageAppoint
                            setBaseColor("#e5f9f8")
                            setProgressColor("#00c6be")
                            animatePercentage(percentage, 500)
                            numerator = uniqueCount
                            denominator = academicianCount
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Approved talepler alınırken hata oluştu", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Akademisyen sayısı alınırken hata oluştu", e)
            }
    }


    //Bekleyen talepler sayfasına git
    fun pendingRequests(view: View) {
        startActivity(Intent(this@AdminPanelActivity, PendingRequestsActivity::class.java))
    }

    //Eski talepler sayfasına git
    fun oldRequests(view: View) {
        startActivity(Intent(this@AdminPanelActivity, OldRequestsActivity::class.java))
    }

    //Yönetici Ekleme sayfasına git
    fun adminUser(view: View) {
        startActivity(Intent(this@AdminPanelActivity, AddAdminUserActivity::class.java))
    }

    //Raporlar ve Şikayetler sayfasına git
    fun reportsAndComplaints(view:View){
        startActivity(Intent(this@AdminPanelActivity,ReportsAndComplaintsActivity::class.java))
    }

    //AcademicianMainActivity e geri dön
    fun goToMain(view: View) {
        finish()
    }
}