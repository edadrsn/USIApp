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
import com.example.usiapp.databinding.FragmentPreviousEducationsBinding

class PreviousEducationsFragment : Fragment() {

    private var _binding:FragmentEducationBinding? = null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentEducationBinding.inflate(inflater,container,false)
        return binding.root
    }


    class PreviousEducationsFragment : Fragment() {

        private var _binding: FragmentPreviousEducationsBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentPreviousEducationsBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)


            val addButton = binding.btnAddPrevEducation
            val educationInput = binding.prevEducationOfArea
            val container = binding.prevEducationContainer

            // Ekle
            addButton.setOnClickListener {
                val educationText = educationInput.text.toString().trim()
                if (educationText.isNotEmpty()) {

                    // Kartın dış container'ı
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

                    val textContainer = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val educationTextView = TextView(requireContext()).apply {
                        text = educationText
                        setTextColor(Color.BLACK)
                        textSize = 17f
                    }

                    textContainer.addView(educationTextView)

                    // Sil
                    val deleteButton = ImageButton(requireContext()).apply {
                        setImageResource(R.drawable.baseline_delete_24)
                        setBackgroundColor(Color.TRANSPARENT)
                        setOnClickListener {
                            AlertDialog.Builder(requireContext()).apply {
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
                    Toast.makeText(requireContext(), "📍 Lütfen önceki bir eğitim girin.", Toast.LENGTH_SHORT).show()
                }
            }

            // Geri
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