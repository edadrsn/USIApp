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
import com.example.usiapp.databinding.ActivityPersonalInfoBinding

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPersonalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val unvanlar = listOf("Prof. Dr.", "Doç. Dr.", "Dr. Öğr. Üyesi", "Araş. Gör.", "Öğr. Gör.")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unvanlar)
        val dropdown = findViewById<AutoCompleteTextView>(R.id.unvanDropdown)
        dropdown.setAdapter(adapter)
        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

    }

    fun goToBack(view: View){
        val intent= Intent(this,AcademicianActivity::class.java)
        startActivity(intent)
    }
}