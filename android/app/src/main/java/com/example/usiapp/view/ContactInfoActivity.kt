package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.usiapp.databinding.ActivityContactInfoBinding

class ContactInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactInfoBinding
    private lateinit var provinceDropdown: AutoCompleteTextView
    private lateinit var districtDropdown: AutoCompleteTextView
    private lateinit var ilceMap: Map<String, List<String>>
    private lateinit var illerVeIlceler: Map<String, List<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContactInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // JSON okuma ve pars etme
        val jsonString = loadJsonFromAsset("turkiye_iller_ilceler.json")
        val gson = Gson()
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        illerVeIlceler = gson.fromJson(jsonString, type)

        val provinceAutoComplete = binding.province
        val districtAutoComplete = binding.district

        // İl listesi
        val illerListesi = illerVeIlceler.keys.toList()
        val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, illerListesi)
        provinceAutoComplete.setAdapter(provinceAdapter)

        // İl seçildiğinde ilçeleri güncelle
        provinceAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val secilenIl = parent.getItemAtPosition(position) as String
            val ilceListesi = illerVeIlceler[secilenIl] ?: emptyList()
            val districtAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ilceListesi)
            districtAutoComplete.setAdapter(districtAdapter)
            districtAutoComplete.text.clear() 
        }

        // Dropdown aç
        provinceAutoComplete.setOnClickListener {
            provinceAutoComplete.showDropDown()
        }
        districtAutoComplete.setOnClickListener {
            districtAutoComplete.showDropDown()
        }
    }

    private fun loadJsonFromAsset(fileName: String): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }



    fun goToBack(view: View) {
        val intent = Intent(this, AcademicianActivity::class.java)
        startActivity(intent)
    }
}