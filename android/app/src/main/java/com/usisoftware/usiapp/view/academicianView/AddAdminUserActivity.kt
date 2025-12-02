package com.usisoftware.usiapp.view.academicianView

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityAddAdminUserBinding

class AddAdminUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAdminUserBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddAdminUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadAdmins()

        // Yöneticileri kaydet
        binding.addAdminUser.setOnClickListener {
            val adminEmail = binding.adminEmail.text.toString().trim()

            if (adminEmail.isEmpty()) {
                Toast.makeText(this, "Lütfen boş mail girmeyin!", Toast.LENGTH_SHORT).show()
            } else {
                // Yeni bir doküman referansı oluştur
                val newDocRef = db.collection("Admins").document()

                // Admin verisi
                val adminData = hashMapOf(
                    "email" to adminEmail,
                    "id" to newDocRef.id
                )

                // Verileri Firestore'a kaydet
                newDocRef.set(adminData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Admin eklendi", Toast.LENGTH_SHORT).show()
                        binding.adminEmail.setText("")
                        loadAdmins()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Hata", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Firestore'dan adminleri yükle
    private fun loadAdmins() {
        db.collection("Admins")
            .get()
            .addOnSuccessListener { result ->
                // Önceki kartları temizle
                binding.adminsContainer.removeAllViews()

                for (document in result) {
                    val email = document.getString("email") ?: continue
                    val docId = document.id

                    // Kart görünümü oluştur
                    val cardView = MaterialCardView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(10, 15, 10, 15)
                        }
                        radius = 24f
                        cardElevation = 7f
                        setCardBackgroundColor(Color.WHITE)
                        strokeColor = Color.TRANSPARENT
                        strokeWidth = 0
                        setContentPadding(24, 24, 24, 24)
                    }

                    //  e-mail
                    val emailText = TextView(this).apply {
                        text = email
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        typeface = Typeface.DEFAULT_BOLD
                        layoutParams =
                            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    // Kart içindeki yatay düzen
                    val innerLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    // Silme butonu
                    val deleteButton = ImageView(this).apply {
                        setImageResource(R.drawable.baseline_delete_24)
                        background = ContextCompat.getDrawable(
                            this@AddAdminUserActivity,
                            R.drawable.bg_delete_shadow
                        )
                        layoutParams = LinearLayout.LayoutParams(96, 96).apply {
                            marginStart = 16
                        }
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                        setPadding(20, 20, 20, 20)


                        setOnClickListener {
                            AlertDialog.Builder(this@AddAdminUserActivity).apply {
                                setTitle("Yönetici Silme")
                                setMessage("Bu yöneticiyi silmek istediğinize emin misiniz?")

                                setPositiveButton("Evet") { dialog, _ ->
                                    db.collection("Admins").document(docId)
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@AddAdminUserActivity,
                                                "Admin silindi",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            loadAdmins() // Liste güncelleniyor
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                this@AddAdminUserActivity,
                                                "Silme başarısız",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    dialog.dismiss()
                                }

                                setNegativeButton("Hayır") { dialog, _ ->
                                    dialog.dismiss()
                                }
                            }.create().show()
                        }
                    }

                    // Layout'a elemanları ekle
                    innerLayout.addView(emailText)
                    innerLayout.addView(deleteButton)
                    cardView.addView(innerLayout)
                    binding.adminsContainer.addView(cardView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Admin listesi alınamadı", Toast.LENGTH_SHORT).show()
            }
    }

    // Geri butonu ile AdminPanelActivity'ye dön
    fun goToBack(view: View) {
        finish()
    }
}

