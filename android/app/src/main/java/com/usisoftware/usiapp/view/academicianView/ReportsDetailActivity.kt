package com.usisoftware.usiapp.view.academicianView

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityReportsDetailBinding
import com.usisoftware.usiapp.view.model.Report

class ReportsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsDetailBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val report = intent.getSerializableExtra("report") as? Report
        db = FirebaseFirestore.getInstance()


        binding.reportEmail.text = report?.user
        binding.reportMessage.text = report?.message

        // Şikayet edilen talebi Firestore'dan çek
        report?.requestId?.let { loadRequestData(it) }

        //Şikayeti sil
        binding.deleteReport.setOnClickListener {
            val reportId = report?.id

            if (reportId.isNullOrEmpty()) {
                Toast.makeText(this, "Report ID bulunamadı", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this@ReportsDetailActivity).apply {
                setTitle("Şikayet Silinsin mi?")
                setMessage("Şikayeti silmek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->
                    db.collection("Reports")
                        .document(reportId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@ReportsDetailActivity,
                                "Şikayet silindi",
                                Toast.LENGTH_SHORT
                            ).show()
                            //Şikayet silme işlemi başarılı oldu o zaman result gönder
                            val resultIntent = Intent()
                            resultIntent.putExtra("deleted", true)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@ReportsDetailActivity,
                                "Şikayet silinemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                create().show()

            }


        }

        //Talebi sil
        binding.deleteRequest.setOnClickListener {
            val requestId = report?.requestId

            if (requestId.isNullOrEmpty()) {
                Toast.makeText(
                    this@ReportsDetailActivity,
                    "Talep ID bulunamadı",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this).apply {
                setTitle("Talep silinsin mi ?")
                setMessage("Talebi silmek istediğinize emin misiniz ?")
                setPositiveButton("Evet") { dialog, _ ->

                    //Requests koleksiyonundan sil
                    db.collection("Requests")
                        .document(requestId)
                        .delete()
                        .addOnSuccessListener {

                            //Reports koleksiyonundan sil
                            db.collection("Reports")
                                .document(report?.id!!)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@ReportsDetailActivity,
                                        "Talep silindi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Taleple ilişkili şikayet varsa onu da sil
                                    val resultIntent = Intent()
                                    resultIntent.putExtra("deleted", true)
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()

                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@ReportsDetailActivity,
                                        "Requests silindi fakat Reports silinemedi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@ReportsDetailActivity,
                                "Talep Requests koleksiyonundan silinemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
                setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                create().show()

            }

        }

    }

    //Verileri yükle
    private fun loadRequestData(requestId: String) {
        if (requestId.isEmpty()) {
            Toast.makeText(this, "Talep ID bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Requests")
            .document(requestId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {

                    // UI'ya yaz
                    binding.requester.text = doc.getString("requesterName") ?: ""
                    binding.requesterType.text = doc.getString("requesterType") ?: ""
                    binding.email.text = "Mail: " + doc.getString("requesterEmail") ?: ""
                    binding.requestTitle.text = doc.getString("requestTitle") ?: ""
                    binding.requestMessage.text = doc.getString("requestMessage") ?: ""
                    binding.requestDate.text = doc.getString("createdDate") ?: ""

                    val imageUrl = doc.getString("requesterImage")
                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.person_profile)
                            .error(R.drawable.person_profile)
                            .into(binding.image)   // burada resim gösterilecek
                    } else {
                        binding.image.setImageResource(R.drawable.baseline_block_24)
                    }


                } else {
                    Toast.makeText(this, "Talep bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Talep bilgileri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }


    //Geri dön
    fun back(view: View) {
        finish()
    }
}
