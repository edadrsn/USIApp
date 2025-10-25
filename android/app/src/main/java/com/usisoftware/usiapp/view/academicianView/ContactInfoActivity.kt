package com.usisoftware.usiapp.view.academicianView

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.usisoftware.usiapp.databinding.ActivityContactInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.usisoftware.usiapp.view.repository.GetAndUpdateAcademician

class ContactInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null
    private lateinit var illerVeIlceler: Map<String, List<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContactInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            this@ContactInfoActivity,
            android.R.layout.simple_dropdown_item_1line,
            illerListesi
        )
        provinceAutoComplete.setAdapter(provinceAdapter)

        // İl seçilirse ilçeleri güncelle
        provinceAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val secilenIl = parent.getItemAtPosition(position) as String
            val ilceListesi = illerVeIlceler[secilenIl] ?: emptyList()
            val districtAdapter = ArrayAdapter(
                this@ContactInfoActivity,
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
        // Giriş yapan kullanıcının e-posta adresi
        val email = auth.currentUser?.email ?: return

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


                binding.phoneNumber.setText(getPhone)
                binding.corporateNumber.setText(getCorporate)
                binding.email.setText(getEmail)
                binding.webSite.setText(getWebsite)
                binding.province.setText(getProvince, false)
                binding.district.setText(getDistrict, false)

            },
            onFailure = {}
        )

        //Butona basınca güncellemek istediğine dair soru sor
        binding.updateContact.setOnClickListener {
            AlertDialog.Builder(this@ContactInfoActivity).apply {
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
                        onSuccess = { Toast.makeText(
                                this@ContactInfoActivity, "Bilgiler başarıyla güncellendi", Toast.LENGTH_SHORT).show() },
                        onFailure = { Toast.makeText(this@ContactInfoActivity, "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
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


    //Geri dön
    fun goToProfile(view: View) {
       finish()
    }

    //Json
    private fun loadJsonFromAsset(fileName: String): String {
        val inputStream = this@ContactInfoActivity.assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }

}