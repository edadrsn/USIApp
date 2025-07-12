package com.example.usiapp.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentPersonalInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var personName: EditText
    private lateinit var personSurname: EditText
    private lateinit var personDegree: AutoCompleteTextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var documentId:String?= null


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
            "Prof. Dr.",
            "Doç. Dr.",
            "Dr. Öğr. Üyesi",
            "Dr.",
            "Öğr. Gör. Dr.",
            "Öğr. Gör.",
            "Arş. Gör."
        )

        // Dropdown için adapter oluşturdum ve basit bir tek satırlı görünüme sahip liste tanımladım
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            unvanlar
        )

        val dropdown = binding.personDegree
        // Adapter dropdown'a atadım
        dropdown.setAdapter(adapter)

        // Kullanıcı kutuya tıkladığında listeyi göster
        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

        //Geri dön
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }


        personName = binding.personName
        personSurname = binding.personSurname
        personDegree = binding.personDegree

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            getPersonalInfo(currentUserEmail)
        }


        binding.btnUpdate.setOnClickListener {
            updateAcademicianInfo()
        }
    }

    //Firebaseden çekilen veriyi ilgili alanlara ata
    private fun getPersonalInfo(email: String) {
        db.collection("AcademicianInfo")
            .whereEqualTo("Email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    this.documentId = doc.id
                    val fullName = doc.getString("adSoyad") ?: ""
                    val degree = doc.getString("Unvan") ?: ""

                    //Firebaseden çekilen adSoyadı parçala
                    val nameParts = fullName.trim().split(" ")
                    if (nameParts.size >= 2) {
                        val surname = nameParts.last() // Son kelime soyadı
                        val name = nameParts.subList(0, nameParts.size - 1)
                            .joinToString(" ") // Boşluğa göre birleştirir

                        personName.setText(name)
                        personSurname.setText(surname)
                    } else {
                        // Tek kelimelik isim varsa her ikisine aynı şeyi koy
                        personName.setText(fullName)
                        personSurname.setText("")
                    }

                    personDegree.setText(degree,false)
                    binding.personName.isEnabled=false
                    binding.personSurname.isEnabled=false
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Akademisyen bilgisi bulunamadı !",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }

    }

    //Update
    private fun updateAcademicianInfo(){
        val updateName=binding.personName.text.toString()
        val updateSurname=binding.personSurname.text.toString()
        val updateDegree=binding.personDegree.text.toString()
        if(updateName.isEmpty() || updateSurname.isEmpty() ||updateDegree.isEmpty()){
            Toast.makeText(requireContext(),"Lütfen tüm alanları doldurun!",Toast.LENGTH_LONG).show()
            return
        }
        val fullName="$updateName $updateSurname"
        val updates= hashMapOf<String,Any>(
            "adSoyad" to fullName,
            "Unvan" to updateDegree
        )
        if(documentId == null){
            Toast.makeText(requireContext(),"Belge bulunamadı,lütfen tekrar deneyiniz !",Toast.LENGTH_LONG).show()
            return
        }

        db.collection("AcademicianInfo").document(documentId!!)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Bilgiler başarıyla güncellendi.",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(requireContext(),"Güncelleme başarısız: ${e.localizedMessage}",Toast.LENGTH_LONG).show()
            }


    }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

    }