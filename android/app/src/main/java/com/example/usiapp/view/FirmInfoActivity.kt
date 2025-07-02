package com.example.usiapp.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityFirmInfoBinding

class FirmInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirmInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityFirmInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firmNameInput = binding.firmName
        val workAreaInput = binding.companyWorkArea
        val firmContainer = binding.firmContainer
        val btnAdd = binding.btnAdd

        btnAdd.setOnClickListener {
            val firmName = firmNameInput.text.toString().trim()
            val workArea = workAreaInput.text.toString().trim()

            if (firmName.isNotEmpty() && workArea.isNotEmpty()) {

                // Ana kapsayıcı (her firma bilgisi için)
                val cardLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(24, 24, 24, 24)
                    setBackgroundResource(R.drawable.edittext_bg)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(24, 16, 24, 0)
                    }
                }

                // Firma bilgilerini gösteren metin
                val textView = TextView(this).apply {
                    text = "FİRMA: $firmName\nALAN: $workArea"
                    setTextColor(Color.parseColor("#000000"))
                    setTypeface(null, Typeface.BOLD)
                    textSize = 18f
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                // Silme butonu
                // Silme butonu
                val deleteButton = ImageButton(this).apply {
                    setImageResource(R.drawable.baseline_delete_24)
                    setBackgroundColor(Color.TRANSPARENT)

                    setOnClickListener {
                        AlertDialog.Builder(this@FirmInfoActivity).apply {
                            setTitle("Bilgi Silinsin mi?")
                            setMessage("Bu firma bilgisi silinecek. Emin misiniz?")
                            setPositiveButton("Evet") { dialog, _ ->
                                firmContainer.removeView(cardLayout)
                                dialog.dismiss()
                            }
                            setNegativeButton("Hayır") { dialog, _ ->
                                dialog.dismiss()
                            }
                            create()
                            show()
                        }
                    }
                }


                // View'leri birbirine ekle
                cardLayout.addView(textView)
                cardLayout.addView(deleteButton)
                firmContainer.addView(cardLayout)

                // Alanları temizle
                firmNameInput.text.clear()
                workAreaInput.text.clear()

            } else {
                Toast.makeText(this, "📍 Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }



    }

    fun goToBack(view: View) {
        val intent = Intent(this@FirmInfoActivity, AcademicianActivity::class.java)
        startActivity(intent)
    }
}
