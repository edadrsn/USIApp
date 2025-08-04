package com.example.usiapp.view.industryView

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.usiapp.databinding.FragmentProfileIndustryBinding
import com.example.usiapp.view.academicianView.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileIndustryFragment : Fragment() {

    private var _binding: FragmentProfileIndustryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var industryFirmName: EditText
    private lateinit var industryFirmWorkArea: AutoCompleteTextView
    private lateinit var address: EditText
    private lateinit var industryTel: EditText
    private lateinit var logOutIndustry: Button
    private lateinit var editInfo: Button
    private lateinit var saveInfo: Button
    private lateinit var otherText: TextView
    private lateinit var otherWorkArea: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileIndustryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        industryFirmName = binding.industryFirmName
        industryFirmWorkArea = binding.industryFirmWorkArea
        address = binding.address
        industryTel = binding.industryTel
        logOutIndustry = binding.logOutIndustry
        editInfo = binding.btneEditInfo
        saveInfo = binding.btnSaveInfo
        otherText = binding.otherTxt
        otherWorkArea = binding.otherWorkArea

        // "Diğer" seçeneği başlangıçta gizli
        otherText.visibility = View.GONE
        otherWorkArea.visibility = View.GONE

        val alanlar = listOf(
            "Seçiniz",
            "Sağlık",
            "Yapay Zeka",
            "Enerji",
            "Makine",
            "Tarım",
            "Tekstil",
            "Diğer"
        )

        // MEVCUT KULLANICI VERİLERİNİ ÇEK
        val uid = auth.currentUser?.uid ?: return
        db.collection("Industry").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Mevcut bilgileri al
                    industryFirmName.setText(document.getString("firmaAdi") ?: "")
                    val calismaAlani = document.getString("calismaAlanlari") ?: ""

                    // Çalışma alanı "Diğer" ya da listede değilse özel alanı göster
                    if (calismaAlani !in alanlar || calismaAlani == "Diğer") {
                        industryFirmWorkArea.setText("Diğer", false)
                        otherWorkArea.setText(calismaAlani)
                        otherWorkArea.visibility = View.VISIBLE
                        otherText.visibility = View.VISIBLE
                    } else {
                        industryFirmWorkArea.setText(calismaAlani, false)
                    }

                    // Diğer alanlar
                    address.setText(document.getString("adres") ?: "")
                    industryTel.setText(document.getString("telefon") ?: "")
                }
            }


        // Kullanıcı "Diğer" seçerse özel giriş alanını göster
        industryFirmWorkArea.setOnItemClickListener { parent, view, position, id ->
            val selectedText = alanlar[position]
            if (selectedText == "Diğer") {
                otherWorkArea.visibility = View.VISIBLE
                otherText.visibility = View.VISIBLE
            } else {
                otherWorkArea.visibility = View.GONE
                otherText.visibility = View.GONE
            }
        }

        // DropDown menüsü için adapter
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, alanlar)
        val dropdown = binding.industryFirmWorkArea
        dropdown.setAdapter(adapter)
        dropdown.setOnClickListener { dropdown.showDropDown() }

        // Başlangıçta alanları düzenlenemez yap
        setFieldsEditable(false)

        // Düzenle butonuna basıldığında alanları düzenlenebilir hale getir
        editInfo.setOnClickListener {
            setFieldsEditable(true)
            editInfo.visibility = View.GONE
            saveInfo.visibility = View.VISIBLE
        }

        // Kaydet butonuna basıldığında verileri Firestore'a gönder
        saveInfo.setOnClickListener {
            saveToFirestore()
            setFieldsEditable(false)
            saveInfo.visibility = View.GONE
            editInfo.visibility = View.VISIBLE
        }

        // Çıkış yap butonu
        logOutIndustry.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    // Tüm form alanlarını düzenlenebilir ya da değil yap
    private fun setFieldsEditable(editable: Boolean) {
        industryFirmName.isEnabled = editable
        industryFirmWorkArea.isEnabled = editable
        address.isEnabled = editable
        industryTel.isEnabled = editable
        binding.otherWorkArea.isEnabled = editable

        // Renk belirle
        val color = if (editable) Color.parseColor("#000000") else Color.parseColor("#A0A0A0")

        industryFirmName.setTextColor(color)
        industryFirmWorkArea.setTextColor(color)
        address.setTextColor(color)
        industryTel.setTextColor(color)
        binding.otherWorkArea.setTextColor(color)
    }


    // Firestore'a verileri kaydeder
    fun saveToFirestore() {
        val firmNameText = industryFirmName.text.toString().trim()
        val selectedWorkArea = industryFirmWorkArea.text.toString().trim()
        val addressText = address.text.toString().trim()
        val phoneText = industryTel.text.toString().trim()

        // Boş alan kontrolü
        if (firmNameText.isEmpty() || selectedWorkArea.isEmpty() || addressText.isEmpty() || phoneText.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Adres uzunluk kontrolü
        if (addressText.length < 10) {
            Toast.makeText(
                requireContext(),
                "Adres en az 10 karakter olmalıdır",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Telefon numarası geçerliliği
        if (!phoneText.matches(Regex("^\\d{10,11}\$"))) {
            Toast.makeText(
                requireContext(),
                "Telefon numarası 10-11 haneli olmalıdır",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Eğer "Diğer" seçiliyse, özel alanı al
        val finalWorkArea = if (selectedWorkArea == "Diğer") {
            val other = binding.otherWorkArea.text.toString().trim()
            if (other.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Lütfen çalışma alanınızı giriniz",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            other
        } else {
            selectedWorkArea
        }

        // UID ve email'i al
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        // Kaydedilecek veriler
        val userMap = hashMapOf(
            "firmaAdi" to firmNameText,
            "calismaAlanlari" to finalWorkArea,
            "adres" to addressText,
            "telefon" to phoneText,
            "email" to email
        )

        // Firestore'a sanayici bilgilerini kaydet
        db.collection("Industry").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Hata oluştu: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}



