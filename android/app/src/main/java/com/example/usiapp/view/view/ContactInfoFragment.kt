package com.example.usiapp.view.view

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
import com.example.usiapp.databinding.FragmentContactInfoBinding
import com.example.usiapp.view.repository.GetAndUpdateAcademician
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



        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userPhoneNum = binding.phoneNumber
        userCorporateNum = binding.corporateNumber
        userEmail = binding.email
        userWebsite = binding.webSite
        userProvince = binding.province
        userDistrict = binding.district

        // Giriş yapan kullanıcının e-posta adresi
       val email=auth.currentUser?.email?: return

        //Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val getPhone = document.getString("personelTel") ?: ""
                val getCorporate = document.getString("kurumsalTel") ?: ""
                val getEmail = document.getString("email") ?: ""
                val getWebsite = document.getString("web") ?: ""
                val getProvince = document.getString("il") ?: ""
                val getDistrict = document.getString("ilce") ?: ""


                userPhoneNum.setText(getPhone)
                userCorporateNum.setText(getCorporate)
                userEmail.setText(getEmail)
                userWebsite.setText(getWebsite)
                userProvince.setText(getProvince, false)
                userDistrict.setText(getDistrict, false)

            },
            onFailure = {}
        )

        //Butona basınca güncellemek istediğine dair soru sor
        binding.updateContact.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Güncelleme")
                setMessage("İletişim bilgilerinizi güncellemek istediğinize emin misiniz ?")
                setPositiveButton("Evet") { dialog, _ ->

                    val updatePhone = binding.phoneNumber.text.toString()
                    val updateCorporateNum = binding.corporateNumber.text.toString()
                    val updateEmail = binding.email.text.toString()
                    val updateWebsite = binding.webSite.text.toString()
                    val updateProvince = binding.province.text.toString()
                    val updateDistrict = binding.district.text.toString()

                    val updates = mapOf<String, Any>(
                        "personelTel" to updatePhone,
                        "kurumsalTel" to updateCorporateNum,
                        "email" to updateEmail,
                        "web" to updateWebsite,
                        "il" to updateProvince,
                        "ilce" to updateDistrict
                    )

                    //Güncelleme
                    GetAndUpdateAcademician.updateAcademicianInfo(
                        db,
                        documentId.toString(),
                        updates,
                        onSuccess = {
                            Toast.makeText(
                                requireContext(),
                                "Bilgiler başarıyla güncellendi",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = {
                            Toast.makeText(
                                requireContext(),
                                "Hata: ${it.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
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

