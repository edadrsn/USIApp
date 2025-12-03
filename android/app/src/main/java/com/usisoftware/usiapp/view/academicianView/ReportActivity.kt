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
        if(requestId.isEmpty()){
            Toast.makeText(this,"Talep bilgisi bulunamadı.",Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Giriş yoksa butonu devre dışı bırak ve uyarı göster
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.btnSendReport.isEnabled = false
            Toast.makeText(this, "Şikayet gönderebilmek için giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSendReport.setOnClickListener {
            val reportMessage=binding.reportMessage.text.toString()

            if(reportMessage.isEmpty()){
                Toast.makeText(this,"Lütfen şikayetinizi yazınız",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnSendReport.isEnabled=false
            sendReport(reportMessage,requestId)
        }

    }

    // Şikayeti gönder
    fun sendReport(reportMessage: String, requestId: String) {
        val userUid = auth.currentUser?.uid ?: "Bilinmeyen kullanıcı"
        val user=auth.currentUser?.email ?: ""

        val reportMap = hashMapOf(
            "message" to reportMessage,
            "requestId" to requestId,
            "user" to user
        )

        db.collection("Reports")
            .document()  // otomatik ID
            .set(reportMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Şikayetiniz iletilmiştir.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"Şikayet gönderilemedi",Toast.LENGTH_SHORT).show()
                Log.e("Report", "Şikayet gönderilemedi:",e)
            }

    }

    //Geri dön
    fun back(view: View){
        finish()
    }
}