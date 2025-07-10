package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentPersonalInfoBinding

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kullanıcıya seçtirmek istediğim ünvanlar
        val unvanlar = listOf(
            "Profesör Doktor",
            "Doçent Doktor",
            "Doktor Öğretim Üyesi",
            "Araştırma Görevlisi",
            "Öğretim Görevlisi"
        )

        // Dropdown için adapter oluşturdum ve basit bir tek satırlı görünüme sahip liste tanımladım
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            unvanlar
        )

        val dropdown = binding.unvanDropdown
        // Adapter dropdown'a atadım
        dropdown.setAdapter(adapter)

        // Kullanıcı kutuya tıkladığında listeyi göster
        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }
    }
}