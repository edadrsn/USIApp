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
import com.example.usiapp.databinding.FragmentProfessionInfoBinding

class ProfessionInfoFragment : Fragment() {

    private var _binding:FragmentProfessionInfoBinding? = null
    private val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentProfessionInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    class ProfessionInfoFragment : Fragment() {

        private var _binding: FragmentProfessionInfoBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            _binding = FragmentProfessionInfoBinding.inflate(inflater, container, false)
            return binding.root
        }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val addButton = binding.btnAddProfessionInfo
            val professionInput = binding.professionOfArea
            val container = binding.professionInfoContainer

            // Ekle
            addButton.setOnClickListener {
                val professionText = professionInput.text.toString().trim()

                if (professionText.isNotEmpty()) {

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

                    // Yazıların LinearLayoutu
                    val textContainer = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    // Girilen metni göster
                    val professionTextView = TextView(requireContext()).apply {
                        text = professionText
                        setTextColor(Color.BLACK)
                        textSize = 17f
                    }


                    textContainer.addView(professionTextView)

                    // Sil
                    val deleteButton = ImageButton(requireContext()).apply {
                        setImageResource(R.drawable.baseline_delete_24)
                        setBackgroundColor(Color.TRANSPARENT)
                        setOnClickListener {
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle("Bilgi Silinsin mi?")
                                setMessage("Bu uzmanlık alanı silinecek. Emin misiniz?")
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
                    professionInput.text.clear()

                } else {
                    Toast.makeText(requireContext(), "📍 Lütfen bir uzmanlık alanı girin.", Toast.LENGTH_SHORT).show()
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
}