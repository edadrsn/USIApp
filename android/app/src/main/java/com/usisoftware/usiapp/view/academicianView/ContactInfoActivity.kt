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

        val countryCodes = listOf(
            "+90",   // Türkiye
            "+1",    // ABD / Kanada
            "+7",    // Rusya / Kazakistan
            "+20",   // Mısır
            "+27",   // Güney Afrika
            "+30",   // Yunanistan
            "+31",   // Hollanda
            "+32",   // Belçika
            "+33",   // Fransa
            "+34",   // İspanya
            "+36",   // Macaristan
            "+39",   // İtalya
            "+40",   // Romanya
            "+41",   // İsviçre
            "+43",   // Avusturya
            "+44",   // İngiltere
            "+45",   // Danimarka
            "+46",   // İsveç
            "+47",   // Norveç
            "+48",   // Polonya
            "+49",   // Almanya
            "+52",   // Meksika
            "+55",   // Brezilya
            "+61",   // Avustralya
            "+62",   // Endonezya
            "+63",   // Filipinler
            "+64",   // Yeni Zelanda
            "+65",   // Singapur
            "+66",   // Tayland
            "+81",   // Japonya
            "+82",   // Güney Kore
            "+84",   // Vietnam
            "+86",   // Çin
            "+91",   // Hindistan
            "+92",   // Pakistan
            "+94",   // Sri Lanka
            "+98",   // İran
            "+212",  // Fas
            "+213",  // Cezayir
            "+216",  // Tunus
            "+218",  // Libya
            "+351",  // Portekiz
            "+352",  // Lüksemburg
            "+353",  // İrlanda
            "+354",  // İzlanda
            "+358",  // Finlandiya
            "+370",  // Litvanya
            "+371",  // Letonya
            "+372",  // Estonya
            "+380",  // Ukrayna
            "+385",  // Hırvatistan
            "+386",  // Slovenya
            "+420",  // Çekya
            "+421",  // Slovakya
            "+852",  // Hong Kong
            "+971"   // BAE
        )


        // Ortak adapter
        val codeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            countryCodes
        )

        // Üst telefon alan kodu
        binding.phoneCode.apply {
            setAdapter(codeAdapter)
            setText("+90", false)   // varsayılan
            setOnClickListener {
                showDropDown()
            }
        }

        // Kurumsal telefon alan kodu
        binding.corporatePhoneCode.apply {
            setAdapter(codeAdapter)
            setText("+90", false)   // varsayılan
            setOnClickListener {
                showDropDown()
            }
        }

        // Kullanıcı yanlışlıkla yazamasın
        binding.phoneCode.keyListener = null
        binding.corporatePhoneCode.keyListener = null

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        // Giriş yapan kullanıcı uid
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId = currentUser.uid

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

                // PERSONEL TELEFON
                val phoneCode =
                    countryCodes.find { getPhone.startsWith(it) } ?: "+90"

                val phoneNumber =
                    getPhone.removePrefix(phoneCode).trim()

                binding.phoneCode.setText(phoneCode, false)
                binding.phoneNumber.setText(phoneNumber)

                // KURUMSAL TELEFON
                val corporateCode =
                    countryCodes.find { getCorporate.startsWith(it) } ?: "+90"

                val corporateNumber =
                    getCorporate.removePrefix(corporateCode).trim()

                binding.corporatePhoneCode.setText(corporateCode, false)
                binding.corporateNumber.setText(corporateNumber)
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

                    val updatePhone = binding.phoneCode.text.toString().trim() + " " +
                            binding.phoneNumber.text.toString().trim()

                    val updateCorporateNum = binding.corporatePhoneCode.text.toString().trim() + " " +
                                binding.corporateNumber.text.toString().trim()
                    val updateEmail = binding.email.text.toString()
                    val updateWebsite = binding.webSite.text.toString()
                    val updateProvince = binding.province.text.toString()
                    val updateDistrict = binding.district.text.toString()

                    if (
                        binding.phoneNumber.text.isNullOrBlank() ||
                        binding.email.text.isNullOrBlank() ||
                        binding.province.text.isNullOrBlank()
                    ) {
                        Toast.makeText(this@ContactInfoActivity, "Telefon, e-posta ve il alanları boş bırakılamaz!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val fullPhone = updatePhone
                    val fullCorporate = updateCorporateNum

                    if (getNumberLength(fullPhone) > 15 || getNumberLength(fullCorporate) > 15) {
                        Toast.makeText(
                            this@ContactInfoActivity,
                            "Telefon numaraları en fazla 15 haneli olabilir!",
                            Toast.LENGTH_LONG
                        ).show()
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
                            Log.e("Hata", ": ${it.localizedMessage}")
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

    // + işaretinden sonraki rakam sayısını kontrol et
    fun getNumberLength(phone: String): Int {
        return phone.replace("+", "")
            .replace(" ", "")
            .length
    }
}