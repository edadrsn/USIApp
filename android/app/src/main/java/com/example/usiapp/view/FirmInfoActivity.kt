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
import androidx.core.content.ContextCompat
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

                //Kart
                val cardLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(32, 24, 32, 24)
                    background = ContextCompat.getDrawable(this@FirmInfoActivity, R.drawable.rounded_bg)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(32, 16, 32, 0)
                    }
                    elevation = 8f
                }

                //Metin kutusu
                val textContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val firmNameText = TextView(this).apply {
                    text = firmName
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                    textSize = 17f
                }

                val workAreaText = TextView(this).apply {
                    text = workArea
                    setTextColor(Color.parseColor("#777777"))
                    textSize = 15f
                    setPadding(0, 4, 0, 0)
                }

                textContainer.addView(firmNameText)
                textContainer.addView(workAreaText)

                //Silme butonu
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


                cardLayout.addView(textContainer)
                cardLayout.addView(deleteButton)
                firmContainer.addView(cardLayout)


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
