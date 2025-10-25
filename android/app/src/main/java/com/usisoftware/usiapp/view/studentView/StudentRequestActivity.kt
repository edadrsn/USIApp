package com.usisoftware.usiapp.view.studentView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivityStudentRequestBinding

class StudentRequestActivity : AppCompatActivity() {

    private lateinit var binding:ActivityStudentRequestBinding
    private lateinit var radioGroupStudent: RadioGroup
    private lateinit var selectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityStudentRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        radioGroupStudent = binding.radioGroupStudentCategories

        binding.goRequestSubject.setOnClickListener {
            val selectedId = radioGroupStudent.checkedRadioButtonId

            if (selectedId != -1) { // -1 = hiçbir şey seçilmemiş
                val selectedRadioButton = findViewById<RadioButton>(selectedId)
                selectedCategory = selectedRadioButton.text.toString()

                val intent = Intent(this, RequestSubjectStudentActivity::class.java)
                intent.putExtra("selectedCategory", selectedCategory)
                startActivity(intent)

            } else {
                Toast.makeText(this, "Lütfen bir kategori seçiniz", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Geri dön
    fun goToBack(view: View) {
        finish()
    }
}