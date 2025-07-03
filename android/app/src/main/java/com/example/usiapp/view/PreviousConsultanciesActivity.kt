package com.example.usiapp.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityPreviousConsultanciesBinding

class PreviousConsultanciesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviousConsultanciesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreviousConsultanciesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addButton = binding.btnAddPreviousConsultancyInfo
        val previousConsultancyInput = binding.previousConsultancyOfArea
        val container = binding.consultancyInfoContainer

        addButton.setOnClickListener {
            val consultancyText = previousConsultancyInput.text.toString().trim()

            if (consultancyText.isNotEmpty()) {
                // Kart görünümü
                val cardLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(27, 24, 25, 27)
                    background = ContextCompat.getDrawable(
                        this@PreviousConsultanciesActivity,
                        R.drawable.rounded_bg
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(30, 16, 30, 0)
                    }
                    elevation = 7f
                }

                // Metin kutusu
                val textContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val consultancyTextView = TextView(this).apply {
                    text = consultancyText
                    setTextColor(Color.BLACK)
                    textSize = 17f
                }

                textContainer.addView(consultancyTextView)

                // Silme butonu
                val deleteButton = ImageButton(this).apply {
                    setImageResource(R.drawable.baseline_delete_24)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        AlertDialog.Builder(this@PreviousConsultanciesActivity).apply {
                            setTitle("Bilgi Silinsin mi?")
                            setMessage("Bu danışmanlık konusu silinecek. Emin misiniz?")
                            setPositiveButton("Evet") { dialog, _ ->
                                container.removeView(cardLayout)
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
                container.addView(cardLayout)
                previousConsultancyInput.text.clear()

            } else {
                Toast.makeText(this, "📍 Lütfen danışmanlık konusu girin.", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }

    fun goToBack(view: View) {
        val intent = Intent(this, AcademicianActivity::class.java)
        startActivity(intent)
    }
}