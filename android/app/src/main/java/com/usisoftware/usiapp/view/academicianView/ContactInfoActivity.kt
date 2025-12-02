package com.usisoftware.usiapp.view.academicianView

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.usisoftware.usiapp.databinding.ActivityContactInfoBinding
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
        illerVeIlceler = try {
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e("ContactInfoActivity", "JSON parse hatası: ${e.localizedMessage}")
            emptyMap()
        }

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
        // Giriş yapan kullanıcı uid
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId=currentUser.uid

        //Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            userId,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getAcademicianInfoByEmail

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
            onFailure = { e ->
                Log.e("ContactInfoActivity", "Firestore fetch error", e)
                Toast.makeText(this, "Hata veri alınamadı", Toast.LENGTH_SHORT).show()
            }
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

                    if (updatePhone.isEmpty() || updateEmail.isEmpty() || updateProvince.isEmpty()) {
                        Toast.makeText(
                            this@ContactInfoActivity, "Telefon, e-posta ve il alanları boş bırakılamaz!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

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
                        userId,
                        updates,
                        onSuccess = {
                            Toast.makeText(this@ContactInfoActivity, "Bilgiler başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onFailure = {
                            Toast.makeText(this@ContactInfoActivity, "Bilgileri güncellerken sorun oluştu!", Toast.LENGTH_LONG).show()
                            Log.e("Hata",": ${it.localizedMessage}")
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
        return try {
            val inputStream = assets.open(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("ContactInfoActivity", "JSON okunamadı: ${e.localizedMessage}")
            ""
        }
    }

}