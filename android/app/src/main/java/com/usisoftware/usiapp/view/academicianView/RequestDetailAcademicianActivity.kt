package com.usisoftware.usiapp.view.academicianView

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityRequestDetailAcademicianBinding
import com.usisoftware.usiapp.view.industryView.IndustryPreviewActivity
import com.usisoftware.usiapp.view.model.Request
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation
import com.usisoftware.usiapp.view.studentView.StudentPreviewActivity

class RequestDetailAcademicianActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRequestDetailAcademicianBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityRequestDetailAcademicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db=FirebaseFirestore.getInstance()
        auth=FirebaseAuth.getInstance()
        val request = intent.getSerializableExtra("request") as? Request

        request?.let {
            //Akademisyen Talep Bilgileri
            binding.detailTitle.text = it.title
            binding.detailMessage.text = it.message
            binding.detailDate.text = it.date

            val categoryContainer = binding.detailCategoryContainer
            categoryContainer.removeAllViews()

            val category = try { it.requestCategory ?: "" } catch (e: Exception) { "" }

            val chip = TextView(this).apply {
                text = category
                setPadding(22, 10, 22, 10)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.parseColor("#000000"))
                setTypeface(null, Typeface.BOLD)
                textSize = 11f
                isSingleLine = true
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(1, 10, 10, 10)
                }
            }

            categoryContainer.addView(chip)
            val status = it.status.values.firstOrNull() ?: ""
            val adminMessage = it.adminMessage

            when (status) {
                "pending" -> {
                    binding.requestStatus.text = "Beklemede"
                    binding.requestStatus.setTextColor(Color.parseColor("#F06E1B"))
                    binding.requestStatusIcon.setImageResource(R.drawable.baseline_access_time_24)
                }

                "approved" -> {
                    binding.requestInfo.visibility=View.VISIBLE
                    binding.usiContainer.visibility=View.VISIBLE
                    binding.view2.visibility=View.VISIBLE
                    binding.view3.visibility=View.VISIBLE
                    binding.requestStatus.text = "Onaylandı"
                    binding.requestStatus.setTextColor(Color.parseColor("#4BA222"))
                    binding.requestStatusIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
                    binding.requestInfo.text = "Mesaj: ${adminMessage}"
                    if(request.requestType == true) {
                        binding.isPublished.visibility = View.VISIBLE
                    }else{
                        binding.appointLabel.visibility=View.GONE
                        binding.appointCardContainer.visibility=View.GONE
                    }
                }

                "rejected" -> {
                    binding.requestInfo.visibility=View.VISIBLE
                    binding.usiContainer.visibility=View.VISIBLE
                    binding.view2.visibility=View.VISIBLE
                    binding.view3.visibility=View.VISIBLE
                    binding.requestStatus.text = "Reddedildi"
                    binding.requestStatus.setTextColor(Color.parseColor("#CC1C1C"))
                    binding.requestStatusIcon.setImageResource(R.drawable.baseline_highlight_off_24)
                    binding.requestInfo.text = "Nedeni: ${adminMessage}"
                }
            }

            loadUsersCard(it.id)

        }

        //Talebi silme
        val deleteAction=View.OnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Silme İsteği")
                .setMessage("Talebi silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet"){_ , _ ->
                    request?.let { req ->
                        db.collection("Requests")
                            .document(req.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this,"Talep başarıyla silindi",Toast.LENGTH_SHORT).show()
                                val resultIntent= Intent()
                                resultIntent.putExtra("deleted",true)
                                setResult(Activity.RESULT_OK,resultIntent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,"Silme isteği başarısız oldu",Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Hayır",null)
                .show()
        }

        binding.deleteIcon.setOnClickListener (deleteAction)
        binding.deleteText.setOnClickListener (deleteAction)
    }


    private fun loadUsersCard(requestId: String) {
        db.collection("Requests").document(requestId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val applyUsers = document["applyUsers"] as? Map<String, String> ?: emptyMap()

                    if (applyUsers.isNotEmpty()) {
                        binding.isApply.visibility=View.GONE
                        applyUsers.forEach { (userId, messageText) ->
                            // Students koleksiyonunda ara
                            db.collection("Students").document(userId)
                                .get()
                                .addOnSuccessListener { studentDoc ->
                                    if (studentDoc.exists()) {
                                        addUserCard(studentDoc, messageText, "student")
                                    } else {
                                        // AcademicianInfo koleksiyonunda ara
                                        db.collection("Academician").document(userId)
                                            .get()
                                            .addOnSuccessListener { academicianDoc ->
                                                if (academicianDoc.exists()) {
                                                    addUserCard(academicianDoc, messageText, "academician")
                                                } else {
                                                    // IndustryInfo koleksiyonunda ara
                                                    db.collection("Industry").document(userId)
                                                        .get()
                                                        .addOnSuccessListener { industryDoc ->
                                                            if (industryDoc.exists()) {
                                                                addUserCard(industryDoc, messageText, "industry")
                                                            } else {
                                                                Log.e("RequestDetail", "Kullanıcı bulunamadı: $userId")
                                                            }
                                                        }
                                                }
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("RequestDetail", "Kullanıcı bilgisi alınamadı: ${it.message}")
                                }
                        }
                    }
                } else {
                    Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veri alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUserCard(userDoc: DocumentSnapshot, messageText: String, userType: String) {
        val (name, typeText, profileUrl) = when (userType) {
            "student" -> Triple(
                userDoc.getString("studentName") ?: "Bilinmiyor",
                "Öğrenci",
                userDoc.getString("studentImage") ?: ""
            )

            "academician" -> Triple(
                userDoc.getString("adSoyad") ?: "Bilinmiyor",
                "Akademisyen",
                userDoc.getString("photo") ?: ""
            )

            "industry" -> Triple(
                userDoc.getString("firmaAdi") ?: userDoc.getString("requesterName") ?: "Bilinmiyor",
                "Sanayici",
                userDoc.getString("requesterImage") ?: userDoc.getString("requesterImage") ?: ""
            )

            else -> Triple("Bilinmiyor", "-", "")
        }

        val view = layoutInflater.inflate(R.layout.item_apply_users, null)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(10, 15, 10, 15)
        }
        view.layoutParams = params

        val applyImage = view.findViewById<ImageView>(R.id.applyImage)
        val applyName = view.findViewById<TextView>(R.id.applyName)
        val applyType = view.findViewById<TextView>(R.id.applyType)
        val applyMessage = view.findViewById<TextView>(R.id.applyMessage)

        if (!profileUrl.isNullOrEmpty()) {
            loadImageWithCorrectRotation(
                context = this,
                imageUrl = profileUrl,
                imageView = applyImage,
                placeholderRes = R.drawable.person
            )
        } else {
            applyImage.setImageResource(R.drawable.person)
        }

        applyName.text = name
        applyType.text = typeText
        applyMessage.text = messageText

        //Kart tıklama olayı
        view.setOnClickListener {
            val context = view.context
            when (userType) {
                "student" -> {
                    val intent = Intent(context, StudentPreviewActivity::class.java)
                    intent.putExtra("USER_ID", userDoc.id)
                    context.startActivity(intent)
                }
                "academician" -> {
                    val intent = Intent(context, AcademicianPreviewActivity::class.java)
                    intent.putExtra("USER_ID", userDoc.id)
                    context.startActivity(intent)
                }
                "industry" -> {
                    val intent = Intent(context, IndustryPreviewActivity::class.java)
                    intent.putExtra("USER_ID", userDoc.id)
                    context.startActivity(intent)
                }
            }
        }

        binding.appointCardContainer.addView(view)
    }

    //Geri dön
    fun previousPage(view: View) {
        finish()
    }
}