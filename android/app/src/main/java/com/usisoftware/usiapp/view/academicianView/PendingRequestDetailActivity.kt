package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityPendingRequestDetailBinding
import com.usisoftware.usiapp.view.industryView.IndustryPreviewActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation
import com.usisoftware.usiapp.view.studentView.StudentPreviewActivity

class PendingRequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingRequestDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var appointLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ActivityResult launcher
        appointLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }

        val request = try {
            intent.getSerializableExtra("request") as? Request
        } catch (e: Exception) {
            null }

        val requestId = request?.id ?: ""

        if (request == null) {
            Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadRequestDetails(request)
        setupAcceptButton(request, requestId)
        setupRejectButton(requestId)
    }

    private fun loadRequestDetails(request: Request) {
        // Profil resmi
        if (!request.requesterImage.isNullOrEmpty()) {
            try{
            loadImageWithCorrectRotation(
                context = this,
                imageUrl = request.requesterImage,
                imageView = binding.image,
                placeholderRes = R.drawable.baseline_block_24
            )}catch (e:Exception){
                binding.image.setImageResource(R.drawable.baseline_block_24)
            }
        } else {
            binding.image.setImageResource(R.drawable.baseline_block_24)
        }

        binding.pendingRequesterName.text = request.requesterName
        binding.requesterTypeText.text = getUserTypeText(request.requesterType)
        binding.pendingRequesterEmail.text = "Email: ${request.requesterEmail}"
        binding.pendingRequesterTel.text = "Tel: ${request.requesterPhone}"

        // Talep bilgileri
        binding.requestDetailDate.text = request.date
        binding.requestDetailTitle.text = request.title
        binding.requestDetailDescription.text = request.message
        binding.requesterDetailType.text = getUserTypeText(request.requesterType)
        binding.requesterEmail.text = request.requesterEmail
        binding.requesterPhone.text = request.requesterPhone

        // Adres bilgisi
        binding.requesterAddress.text =
            if (request.requesterType == "industry")
                request.requesterAddress ?: "-"
            else "Adres bulunamadı"

        // Kategoriler
        val categories = when (request.requesterType) {
            "student", "academician" -> listOf(request.requestCategory ?: "")
            "industry" -> request.selectedCategories
            else -> emptyList()
        }

        binding.detailCategoryContainer.removeAllViews()
        categories.forEach { category ->
            val chip = TextView(this).apply {
                text = category
                setPadding(22, 10, 22, 10)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
                textSize = 11f
            }
            binding.detailCategoryContainer.addView(chip)
        }

        binding.goToRequesterPreview.setOnClickListener {
            when (request?.requesterType) {
                "academician" -> {
                    val intent = Intent(this, AcademicianPreviewActivity::class.java)
                    intent.putExtra("USER_ID", request?.requesterId)
                    startActivity(intent)
                }
                "industry" -> {
                    val intent = Intent(this, IndustryPreviewActivity::class.java)
                    intent.putExtra("USER_ID", request?.requesterId)
                    startActivity(intent)
                }
                "student" -> {
                    val intent = Intent(this, StudentPreviewActivity::class.java)
                    intent.putExtra("USER_ID", request?.requesterId)
                    startActivity(intent)
                }
            }
        }
    }

    //Talebi kabul et
    private fun setupAcceptButton(request: Request, requestId: String) {
        binding.btnAccept.setOnClickListener {
            val adminMessage = binding.adminMessage.text.toString().trim()

            getAdminUniversity { universityName ->
                if (universityName == null) {
                    Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@getAdminUniversity
                }

                val updates = mapOf(
                    "adminMessage" to adminMessage,
                    "status.$universityName" to "approved"
                )

                try {
                    db.collection("Requests").document(requestId)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Talep onaylandı", Toast.LENGTH_SHORT).show()
                            // Eğer kapalı talep → akademisyen atanacak
                            if (request.requestType == false) {
                                val intent = Intent(this, AppointAcademicianActivity::class.java)
                                intent.putExtra("requestId", requestId)
                                appointLauncher.launch(intent)
                            } else {
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                } catch (e: Exception) {
                    Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Talebi reddet
    private fun setupRejectButton(requestId: String) {
        binding.btnReject.setOnClickListener {

            val adminMessage = binding.adminMessage.text.toString().trim()

            getAdminUniversity { universityName ->
                if (universityName == null) {
                    Toast.makeText(this, "Üniversite bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@getAdminUniversity
                }

                val updates = mapOf(
                    "adminMessage" to adminMessage,
                    "status.$universityName" to "rejected"
                )

                try {
                    db.collection("Requests").document(requestId)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Talep reddedildi", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                            Log.e("PendingRequestDetailActivity","Hata:${it.localizedMessage}")
                        }

                } catch (e: Exception) {
                    Toast.makeText(this, "Hata", Toast.LENGTH_SHORT).show()
                    Log.e("PendingRequestDetailActivity","Hata:",e)
                }
            }
        }
    }

    // Admin'in üniversitesini bulma
    private fun getAdminUniversity(callback: (String?) -> Unit) {

        val domain = auth.currentUser?.email?.substringAfter("@") ?: ""

        db.collection("Authorities")
            .whereEqualTo("academician", domain)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    callback(null)
                    return@addOnSuccessListener
                }

                // Artık üniversite = Authorities document ID
                callback(snapshot.documents.first().id)

            }
            .addOnFailureListener {
                callback(null)
            }
    }



    private fun getUserTypeText(type: String?): String {
        return when (type) {
            "student" -> "Öğrenci"
            "academician" -> "Akademisyen"
            "industry" -> "Sanayici"
            else -> "-"
        }
    }

    //Geri dön
    fun prevPage(view: View) {
        finish()
    }
}
