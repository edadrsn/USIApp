package com.example.usiapp.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentContactInfoBinding
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson


class ContactInfoFragment : Fragment() {

    private var _binding: FragmentContactInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var illerVeIlceler: Map<String, List<String>>

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private lateinit var userPhoneNum: EditText
    private lateinit var userCorporateNum: EditText
    private lateinit var userEmail: EditText
    private lateinit var userWebsite: EditText
    private lateinit var userProvince: AutoCompleteTextView
    private lateinit var userDistrict: AutoCompleteTextView


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


        //Geri dön
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userPhoneNum = binding.phoneNumber
        userCorporateNum = binding.corporateNumber
        userEmail = binding.email
        userWebsite = binding.webSite
        userProvince = binding.province
        userDistrict = binding.district

        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            getContactInfo(currentUserEmail)
        }


        //Butona basınca güncellemek istediğine dair soru sor
        binding.updateContact.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Güncelleme")
                setMessage("Güncellemek istediğinize emin misiniz ?")
                setPositiveButton("Evet") { dialog, _ ->
                    updateContactInfo()
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


    //Firebaseden verileri çek
    private fun getContactInfo(email: String) {
        db.collection("AcademicianInfo")
            .whereEqualTo("Email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    this.documentId = doc.id
                    val getPhone = doc.getString("tel") ?: ""
                    val getCorporate = doc.getString("kurumsalTel") ?: ""
                    val getEmail = doc.getString("Email") ?: ""
                    val getWebsite = doc.getString("web") ?: ""
                    val getProvince = doc.getString("sehir") ?: ""
                    val getDistrict = doc.getString("ilce") ?: ""


                    userPhoneNum.setText(getPhone)
                    userCorporateNum.setText(getCorporate)
                    userEmail.setText(getEmail)
                    userWebsite.setText(getWebsite)
                    userProvince.setText(getProvince, false)
                    userDistrict.setText(getDistrict, false)

                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    //İletişim bilgilerini güncelle
    private fun updateContactInfo() {
        val updatePhone = binding.phoneNumber.text.toString()
        val updateCorporateNum = binding.corporateNumber.text.toString()
        val updateEmail = binding.email.text.toString()
        val updateWebsite = binding.webSite.text.toString()
        val updateProvince = binding.province.text.toString()
        val updateDistrict = binding.district.text.toString()

        if (updatePhone.isEmpty()) {
            Toast.makeText(requireContext(), "Telefon alanı boş bırakılamaz!", Toast.LENGTH_LONG)
                .show()
        } else {
            if (updateCorporateNum.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Kurumsal telefon alanı boş bırakılamaz!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (updateEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Email boş bırakılamaz!", Toast.LENGTH_LONG)
                        .show()
                } else {
                    if (updateWebsite.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Website alanı boş bırakılamaz!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if (updateProvince.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "İl boş bırakılamaz!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        if (documentId == null) {
            Toast.makeText(
                requireContext(),
                "Belge bulunamadı,lütfen tekrar deneyiniz !",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val updates = hashMapOf<String, Any>(
            "tel" to updatePhone,
            "kurumsalTel" to updateCorporateNum,
            "Email" to updateEmail,
            "web" to updateWebsite,
            "sehir" to updateProvince,
            "ilce" to updateDistrict
        )

        db.collection("AcademicianInfo").document(documentId!!)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Bilgiler başarıyla güncellendi !",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Güncelleme başarısız ${e.localizedMessage} !",
                    Toast.LENGTH_LONG
                ).show()

            }

    }


    //Json
    private fun loadJsonFromAsset(fileName: String): String {
        val inputStream = requireContext().assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

