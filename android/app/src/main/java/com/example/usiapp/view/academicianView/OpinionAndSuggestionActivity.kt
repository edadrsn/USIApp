package com.example.usiapp.view.academicianView

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityOpinionAndSuggestionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val userEmail = auth.currentUser?.email ?: ""
        val feedbackMessage = binding.feedbackMessage.text.toString()

        //Butona tıklayınca mesajı ve maili veritabanına kaydet
        binding.btnSend.setOnClickListener {
            saveFeedback(userEmail, feedbackMessage)
        }


    }

    //Geri dönüşü veritabanına kaydet
    fun saveFeedback(userEmail: String, feedbackMessage: String) {
        binding.btnSend.setOnClickListener {
            val feedbackMessage = binding.feedbackMessage.text.toString()
            val user = auth.currentUser
            val uid = user?.uid ?: ""
            val userMap = hashMapOf(
                "email" to userEmail,
                "feedbackMessage" to feedbackMessage
            )
            db.collection("Feedbacks")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Görüş ve Önerileriniz iletilmiştir.", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Log.e("Feedback", "Feedback gönderilemedi")
                }


        }
    }

    //Geri dön
    fun back(view: View) {
        finish()
    }
}