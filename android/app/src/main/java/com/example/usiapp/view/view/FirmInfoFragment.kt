package com.example.usiapp.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.example.usiapp.databinding.FragmentFirmInfoBinding

class FirmInfoFragment : Fragment() {

    private var _binding:FragmentFirmInfoBinding? = null
    private val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentFirmInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val firmNameInput = binding.firmName
        val workAreaInput = binding.firmWorkArea
        val firmContainer = binding.firmContainer
        val btnAdd = binding.addFirmInfo

        // Ekle
        btnAdd.setOnClickListener {
            val firmName = firmNameInput.text.toString().trim()
            val workArea = workAreaInput.text.toString().trim()

            // Her iki alan da doluysa kart oluştur
            if (firmName.isNotEmpty() && workArea.isNotEmpty()) {

                // Kartın dış görünümü
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

                // Kart içindeki metinlerin yer aldığı container
                val textContainer = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val firmNameText = TextView(requireContext()).apply {
                    text = firmName
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                    textSize = 17f
                }


                val workAreaText = TextView(requireContext()).apply {
                    text = workArea
                    setTextColor(Color.parseColor("#777777"))
                    textSize = 15f
                    setPadding(0, 4, 0, 0)
                }

                // TextView'leri container'a ekleme
                textContainer.addView(firmNameText)
                textContainer.addView(workAreaText)

                // Sil
                val deleteButton = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.baseline_delete_24)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        AlertDialog.Builder(requireContext()).apply {
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
                Toast.makeText(requireContext(), "📍 Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }
    }

    // Fragment yok edildiğinde binding'i bırak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}