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
import com.example.usiapp.databinding.FragmentEducationBinding


class EducationFragment : Fragment() {

    private var _binding: FragmentEducationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEducationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = binding.btnAddEducationInfo
        val educationInput = binding.educationOfArea
        val container = binding.educationInfoContainer

        // Ekle
        addButton.setOnClickListener {
            val educationText = educationInput.text.toString().trim()

            if (educationText.isNotEmpty()) {

                // Kartın dış kapsayıcısı
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

                // Metnin olduğu alan
                val textContainer = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                // Kullanıcının yazdığı eğitim bilgisi
                val educationTextView = TextView(requireContext()).apply {
                    text = educationText
                    setTextColor(Color.BLACK)
                    textSize = 17f
                }

                // TextView'u container'a ekle
                textContainer.addView(educationTextView)

                // Sil
                val deleteButton = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.baseline_delete_24)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle("Eğitim Silinsin mi?")
                            setMessage("Bu eğitim bilgisi silinecek. Emin misiniz?")
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

                // Karta metin ve silme butonunu ekle
                cardLayout.addView(textContainer)
                cardLayout.addView(deleteButton)

                // Kartı görünümünü container'ına ekle
                container.addView(cardLayout)

                // Inputu temizle
                educationInput.text.clear()

            } else {
                Toast.makeText(requireContext(), "📍 Lütfen bir eğitim girin.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

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
