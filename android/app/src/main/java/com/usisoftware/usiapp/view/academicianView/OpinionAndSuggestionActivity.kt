package com.usisoftware.usiapp.view.academicianView

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityOpinionAndSuggestionBinding

class OpinionAndSuggestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpinionAndSuggestionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOpinionAndSuggestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnSend.setOnClickListener {
            saveFeedback()
        }
    }

    //Görüş ve öneriyi kaydet
    private fun saveFeedback() {

        val userEmail = auth.currentUser?.email ?: ""
        val feedbackMessage = binding.feedbackMessage.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (feedbackMessage.isEmpty()) {
            Toast.makeText(this, "Lütfen mesajınızı yazın.", Toast.LENGTH_SHORT).show()
            return
        }

        val userMap = hashMapOf(
            "email" to userEmail,
            "feedbackMessage" to feedbackMessage
        )

        db.collection("Feedbacks")
            .document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Görüş ve Önerileriniz iletilmiştir.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gönderilirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
            }
    }

    //Geri dön
    fun back(view: View) {
        finish()
    }
}
