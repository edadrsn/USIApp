package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentContactInfoBinding
import com.google.common.reflect.TypeToken
import com.google.gson.Gson


class ContactInfoFragment : Fragment() {

    private var _binding: FragmentContactInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var illerVeIlceler: Map<String, List<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // JSON dosyasını okuma ve parse etme
        val jsonString = loadJsonFromAsset("turkiye_iller_ilceler.json")
        val gson = Gson()
        val type = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
        illerVeIlceler = gson.fromJson(jsonString, type)

        val provinceAutoComplete = binding.province
        val districtAutoComplete = binding.district

        // İl liste
        val illerListesi = illerVeIlceler.keys.toList()
        val provinceAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            illerListesi
        )
        provinceAutoComplete.setAdapter(provinceAdapter)

        // İl seçilirse ilçeleri güncelle
        provinceAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val secilenIl = parent.getItemAtPosition(position) as String
            val ilceListesi = illerVeIlceler[secilenIl] ?: emptyList()
            val districtAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                ilceListesi
            )
            districtAutoComplete.setAdapter(districtAdapter)
            districtAutoComplete.text.clear()
        }

        // Dropdown açma
        provinceAutoComplete.setOnClickListener {
            provinceAutoComplete.showDropDown()
        }
        districtAutoComplete.setOnClickListener {
            districtAutoComplete.showDropDown()
        }


        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadJsonFromAsset(fileName: String): String {
        val inputStream = requireContext().assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

