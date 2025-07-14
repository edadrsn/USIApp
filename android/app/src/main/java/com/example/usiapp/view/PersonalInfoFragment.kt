package com.example.usiapp.view

import android.app.AlertDialog
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
    private var documentId: String? = null // Firestore belgesi için ID tutulur

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ünvan seçeneklerini tanımlıyoruz
        val unvanlar = listOf(
            "Prof. Dr.",
            "Doç. Dr.",
            "Dr. Öğr. Üyesi",
            "Dr.",
            "Öğr. Gör. Dr.",
            "Öğr. Gör.",
            "Arş. Gör."
        )

        // DropDown için adapter tanımladım
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            unvanlar
        )
        val dropdown = binding.personDegree
        dropdown.setAdapter(adapter)
        dropdown.setOnClickListener { dropdown.showDropDown() }

        // Geri dön
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }


        personName = binding.personName
        personSurname = binding.personSurname
        personDegree = binding.personDegree

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Giriş yapan kullanıcının e-posta adresi
        val email = auth.currentUser?.email ?: return

        // Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id

                val fullName = document.getString("adSoyad") ?: ""
                val degree = document.getString("unvan") ?: ""

                // adSoyad alanını boşluğa göre parçala
                val nameParts = fullName.trim().split(" ")
                if (nameParts.size >= 2) {
                    val surname = nameParts.last()
                    val name = nameParts.dropLast(1).joinToString(" ")

                    personName.setText(name)
                    personSurname.setText(surname)
                } else {
                    personName.setText(fullName)
                    personSurname.setText("")
                }

                personDegree.setText(degree, false)
                personName.isEnabled = false
                personSurname.isEnabled = false
            },
            onFailure = {
                Toast.makeText(requireContext(), "Veri alınamadı: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        )

        // Güncelleme butonuna tıklanınca AlertDialog göster
        binding.updatePersonalInfo.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Güncelleme")
                setMessage("Güncellemek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->
                    val updateName = personName.text.toString()
                    val updateSurname = personSurname.text.toString()
                    val updateDegree = personDegree.text.toString()

                    if (updateName.isEmpty() || updateSurname.isEmpty() || updateDegree.isEmpty()) {
                        Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val fullName = "$updateName $updateSurname"
                    val updates = mapOf(
                        "adSoyad" to fullName,
                        "unvan" to updateDegree
                    )

                    //Güncelleme
                    GetAndUpdateAcademician.updateAcademicianInfo(
                        db,
                        documentId.toString(),
                        updates,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Bilgiler başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { Toast.makeText(requireContext(), "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
