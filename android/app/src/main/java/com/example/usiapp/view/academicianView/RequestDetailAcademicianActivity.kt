package com.example.usiapp.view.academicianView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityRequestDetailAcademicianBinding
import com.example.usiapp.view.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
            binding.detailDate.text = "📆 " + it.date

            val categoryContainer = binding.detailCategoryContainer
            categoryContainer.removeAllViews()

            val category = it.requestCategory ?: ""

            val chip = TextView(this).apply {
                text = category
                setPadding(22, 10, 22, 10)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.parseColor("#6f99cb"))
                setTypeface(null, Typeface.BOLD)
                textSize = 11f
                isSingleLine = true
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(10, 10, 10, 10)
                }
            }

            categoryContainer.addView(chip)
            val status = it.status
            val adminMessage = it.adminMessage

            when (status) {
                "pending" -> {
                    binding.requestStatus.text = "Beklemede"
                    binding.requestStatus.setTextColor(Color.parseColor("#F06E1B"))
                    binding.requestStatusIcon.setImageResource(R.drawable.baseline_access_time_24)
                }

                "approved" -> {
                    binding.requestStatus.text = "Onaylandı"
                    binding.requestStatus.setTextColor(Color.parseColor("#4BA222"))
                    binding.requestStatusIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
                    binding.requestInfo.text = "Mesaj: ${adminMessage}"
                }

                "rejected" -> {
                    binding.requestStatus.text = "Reddedildi"
                    binding.requestStatus.setTextColor(Color.parseColor("#CC1C1C"))
                    binding.requestStatusIcon.setImageResource(R.drawable.baseline_highlight_off_24)
                    binding.requestInfo.text = "Nedeni: ${adminMessage}"
                }
            }
        }
    }

    //Geri dön
    fun previousPage(view: View) {
        val intent = Intent(this@RequestDetailAcademicianActivity, AcademicianMainActivity::class.java)
        intent.putExtra("goToFragment", "request")
        startActivity(intent)
    }
}