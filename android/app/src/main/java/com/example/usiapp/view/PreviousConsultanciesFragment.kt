package com.example.usiapp.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentPreviousConsultanciesBinding

class PreviousConsultanciesFragment : Fragment() {

    private var _binding: FragmentPreviousConsultanciesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviousConsultanciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = binding.btnAddPreviousConsultancyInfo
        val previousConsultancyInput = binding.previousConsultancyOfArea
        val container = binding.consultancyInfoContainer

        // Ekle
        addButton.setOnClickListener {
            val consultancyText = previousConsultancyInput.text.toString().trim()
            if (consultancyText.isNotEmpty()) {

                // Kart
                val cardLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(27, 24, 25, 27)
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(30, 16, 30, 0)
                    }
                    elevation = 7f
                }

                // Metinleri taşıyan iç layout
                    val textContainer = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val consultancyTextView = TextView(requireContext()).apply {
                    text = consultancyText
                    setTextColor(Color.BLACK)
                    textSize = 17f
                }

                textContainer.addView(consultancyTextView)

                // Sil
                val deleteButton = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.baseline_delete_24)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        AlertDialog.Builder(requireContext()).apply {
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

                // Kartın içine metin kutusu ve silme butonu ekledim
                cardLayout.addView(textContainer)
                cardLayout.addView(deleteButton)

                container.addView(cardLayout)
                previousConsultancyInput.text.clear()

            } else {
                Toast.makeText(requireContext(), "📍 Lütfen danışmanlık konusu girin.", Toast.LENGTH_SHORT).show()
            }
        }

        // Geri dön
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}