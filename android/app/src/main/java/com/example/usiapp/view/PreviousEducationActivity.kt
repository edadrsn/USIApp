package com.example.usiapp.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import com.example.usiapp.databinding.ActivityEducationsBinding
import com.example.usiapp.databinding.ActivityPreviousConsultanciesBinding
import com.example.usiapp.databinding.ActivityPreviousEducationsBinding


class PreviousEducationActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPreviousEducationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPreviousEducationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addButton = binding.btnAddPrevEducation
        val educationInput = binding.prevEducationOfArea
        val container = binding.prevEducationContainer

        addButton.setOnClickListener {
            val educationText = educationInput.text.toString().trim()

            if (educationText.isNotEmpty()) {
                // Kart görünümü
                val cardLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(27, 24, 25, 27)
                    background = ContextCompat.getDrawable(this@PreviousEducationActivity, R.drawable.rounded_bg)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(30, 16, 30, 0)
                    }
                    elevation = 7f
                }

                // Yazı kısmı
                val textContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val educationTextView = TextView(this).apply {
                    text = educationText
                    setTextColor(Color.BLACK)
                    textSize = 17f
                }

                textContainer.addView(educationTextView)

                // Silme butonu
                val deleteButton = ImageButton(this).apply {
                    setImageResource(R.drawable.baseline_delete_24)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        AlertDialog.Builder(this@PreviousEducationActivity).apply {
                            setTitle("Eğitim Silinsin mi?")
                            setMessage("Bu önceki eğitim bilgisi silinecek. Emin misiniz?")
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
                educationInput.text.clear()
            } else {
                Toast.makeText(this, "📍 Lütfen önceki bir eğitim girin.", Toast.LENGTH_SHORT).show()
            }
        }



    }


    fun goToBack(view: View) {
        val intent = Intent(this, AcademicianActivity::class.java)
        startActivity(intent)
    }
}