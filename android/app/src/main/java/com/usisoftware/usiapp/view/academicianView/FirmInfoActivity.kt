package com.usisoftware.usiapp.view.academicianView

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityFirmInfoBinding
import com.usisoftware.usiapp.view.model.Firm
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician
import java.util.UUID

class FirmInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirmInfoBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private lateinit var firmNameInput: EditText
    private lateinit var workAreaInput: EditText
    private lateinit var firmContainer: LinearLayout
    private lateinit var workAreaTagContainer: FlexboxLayout
    private lateinit var emptyMessage: TextView
    private val firmList = mutableListOf<Firm>()
    private val tempWorkAreas = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFirmInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firmNameInput = binding.firmName
        workAreaInput = binding.firmWorkArea
        firmContainer = binding.firmContainer
        workAreaTagContainer = binding.workAreaTagContainer //alanların ekleneceği container
        emptyMessage = binding.txtNoFirm

        val btnAdd = binding.addFirmInfo  //ekle
        val btnAddWorkArea = binding.addFirmWorkArea  //alan oluştur

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        // Giriş yapan kullanıcı uid
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId=currentUser.uid

        // Verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

                try {
                    val firmData = document.get("firmalar") as? List<Map<String, Any>>
                    firmData?.forEach { firmMap ->
                        val firmaAdi = firmMap["firmaAdi"] as? String ?: ""
                        val calismaAlani =
                            firmMap["firmaCalismaAlani"] as? List<String> ?: emptyList()

                        val firm = Firm(firmaAdi, calismaAlani,userId)
                        firmList.add(firm)
                        createFirmCard(firm)
                    }

                    // Firma varsa mesajı gizle, yoksa göster
                    emptyMessage.visibility = if (firmList.isNotEmpty()) View.GONE else View.VISIBLE

                } catch (e: Exception) {
                    Toast.makeText(this@FirmInfoActivity, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            onFailure = { e ->
                Log.e("FirmInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
        )

        // + Butonuna basıldığında çalışma alanlarını geçici olarak biriktir
        btnAddWorkArea.setOnClickListener {
            val area = workAreaInput.text.toString().trim()
            if (area.isNotEmpty()) {
                tempWorkAreas.add(area)
                addTagToContainer(area)
                workAreaInput.text.clear()
            } else {
                Toast.makeText(this@FirmInfoActivity, "Lütfen çalışma alanı girin", Toast.LENGTH_SHORT).show()
            }
        }

        // Firma Ekle butonu
        btnAdd.setOnClickListener {
            val getFirmName = firmNameInput.text.toString().trim()

            if (getFirmName.isEmpty() || tempWorkAreas.isEmpty()) {
                Toast.makeText(this@FirmInfoActivity, "Boş alan bırakmayın", Toast.LENGTH_SHORT)
                    .show()
            } else {

                emptyMessage.visibility = View.GONE

                val newFirm = Firm(
                    getFirmName,
                    tempWorkAreas.toList(),
                    userId,
                    UUID.randomUUID().toString()
                )
                firmList.add(newFirm)
                if (firmList.isNotEmpty()) emptyMessage.visibility = View.GONE
                createFirmCard(newFirm)

                val firmMapList = firmList.map {
                    mapOf(
                        "firmaAdi" to it.firmaAdi,
                        "firmaCalismaAlani" to it.calismaAlani,
                        "id" to it.id
                    )
                }

                // Güncelleme işlemi
                db.collection("Academician").document(userId)
                    .update("firmalar", firmMapList)
                    .addOnSuccessListener {
                        Toast.makeText(this@FirmInfoActivity, "Firma bilgisi eklendi", Toast.LENGTH_SHORT).show()
                        tempWorkAreas.clear()
                        workAreaTagContainer.removeAllViews()
                        firmNameInput.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@FirmInfoActivity, "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

    //Alanları geçici olarak eklediğim container
    private fun addTagToContainer(text: String) {
        val tag = TextView(this@FirmInfoActivity).apply {
            this.text = text
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = ContextCompat.getDrawable(this@FirmInfoActivity, R.drawable.tag_background)
            setMargins(8, 8, 8, 8)
        }
        workAreaTagContainer.addView(tag)
    }

    private fun TextView.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }

    // Kart oluşturma
    private fun createFirmCard(firm: Firm) {
        val cardLayout = LinearLayout(this@FirmInfoActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 10)
            }
            background = ContextCompat.getDrawable(this@FirmInfoActivity, R.drawable.rounded_bg)
            setPadding(24, 24, 24, 24)
        }

        val textLayout = LinearLayout(this@FirmInfoActivity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val firmNameText = TextView(this@FirmInfoActivity).apply {
            text = firm.firmaAdi
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            textSize = 14f
        }

        val workAreaText = TextView(this@FirmInfoActivity).apply {
            text = firm.calismaAlani.joinToString(" • ")
            setTextColor(Color.DKGRAY)
            textSize = 13f
            setPadding(0, 6, 0, 0)
        }

        textLayout.addView(firmNameText)
        textLayout.addView(workAreaText)

        val deleteButton = ImageButton(this@FirmInfoActivity).apply {
            setImageResource(R.drawable.baseline_delete_24)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(70, 70).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setOnClickListener {
                AlertDialog.Builder(this@FirmInfoActivity).apply {
                    setTitle("Bilgi Silinsin mi?")
                    setMessage("Bu firma bilgisini silmek istediğinize emin misiniz?")
                    setPositiveButton("Evet") { dialog, _ ->
                        firmContainer.removeView(cardLayout)
                        firmList.remove(firm)

                        // Silme sonrası boşsa mesajı göster, doluysa gizle
                        emptyMessage.visibility =
                            if (firmList.isEmpty()) View.VISIBLE else View.GONE

                        val updatedFirmList = firmList.map {
                            mapOf(
                                "firmaAdi" to it.firmaAdi,
                                "firmaCalismaAlani" to it.calismaAlani
                            )
                        }
                        db.collection("Academician").document(userId)
                            .update("firmalar", updatedFirmList)
                            .addOnSuccessListener { Toast.makeText(this@FirmInfoActivity, "Firma silindi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@FirmInfoActivity, "Silme başarısız: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                    }
                    setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                    create().show()
                }
            }
        }

        cardLayout.addView(textLayout)
        cardLayout.addView(deleteButton)
        firmContainer.addView(cardLayout)
    }

    //Geri dön
    fun goToProfile(view: View) {
        finish()
    }

}