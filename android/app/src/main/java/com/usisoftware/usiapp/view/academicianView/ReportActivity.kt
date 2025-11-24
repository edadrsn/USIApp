package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityReportBinding
import com.usisoftware.usiapp.view.model.Request

class ReportActivity : AppCompatActivity() {

    private lateinit var binding:ActivityReportBinding
    private lateinit var db:FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db=FirebaseFirestore.getInstance()
        auth=FirebaseAuth.getInstance()

        val request = intent.getSerializableExtra("request") as? Request
        val requestId = request?.id ?: ""

        binding.btnSendReport.setOnClickListener {
            val reportMessage=binding.reportMessage.text.toString()

            if(reportMessage.isEmpty()){
                Toast.makeText(this,"Lütfen şikayetinizi yazınız",Toast.LENGTH_SHORT).show()
            }
            binding.btnSendReport.isEnabled=false

            sendReport(reportMessage,requestId)
        }

    }

    //Şikayeti gönder
    fun sendReport(reportMessage:String, requestId:String){
        val userEmail = auth.currentUser?.email ?: "Bilinmiyor"
        val reportMap = hashMapOf(
            "message" to reportMessage,
            "requestId" to requestId,
            "user" to userEmail
        )
        db.collection("Reports")
            .document()
            .set(reportMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Şikayetiniz iletilmiştir.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Log.e("Report", "Şikayet gönderilemedi")
            }
    }


    //Geri dön
    fun back(view: View){
        finish()
    }
}