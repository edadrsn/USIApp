package com.example.usiapp.view.industryView

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityIndustrySettingsBinding
import com.example.usiapp.view.academicianView.MainActivity
import com.example.usiapp.view.academicianView.OpinionAndSuggestionActivity
import com.example.usiapp.view.academicianView.UpdatePasswordActivity
import com.google.firebase.auth.FirebaseAuth

class IndustrySettingsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityIndustrySettingsBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityIndustrySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

        val languages = listOf("Türkçe")
        val themes = listOf("Aydınlık")

        val langAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        val themeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, themes)

        binding.dropdownLanguage.setAdapter(langAdapter)
        binding.dropdownTheme.setAdapter(themeAdapter)

        //SharedPreferences
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val editor = sharedPref.edit()

        //Önceden kaydedilmiş değerleri yükle
        val savedLanguage = sharedPref.getString("appLanguage", "Türkçe")
        val savedTheme = sharedPref.getString("appTheme", "Aydınlık")

        binding.dropdownLanguage.setText(savedLanguage, false)
        binding.dropdownTheme.setText(savedTheme, false)

        //Seçim yapılınca kaydet
        binding.dropdownLanguage.setOnItemClickListener { _, _, position, _ ->
            val selectedLang = languages[position]
            editor.putString("appLanguage", selectedLang)
            editor.apply()
        }

        binding.dropdownTheme.setOnItemClickListener { _, _, position, _ ->
            val selectedTheme = themes[position]
            editor.putString("appTheme", selectedTheme)
            editor.apply()
        }

        //Önizleme sayfasına git
        binding.goToPreview.setOnClickListener{
            startActivity(Intent(this@IndustrySettingsActivity, IndustryPreviewActivity::class.java))
        }

        //Şifremi unuttum sayfasına git
        binding.forgotPassword.setOnClickListener{
            startActivity(Intent(this@IndustrySettingsActivity, UpdatePasswordActivity::class.java))
        }

        //Görüş ve öneri gönder sayfasına git
        binding.opinionAndSuggestion.setOnClickListener{
            startActivity(Intent(this@IndustrySettingsActivity, OpinionAndSuggestionActivity::class.java))
        }

        //Destek
        binding.support.setOnClickListener {
            val emailIntent=Intent(Intent.ACTION_SENDTO).apply {  //sadece e-posta uygulamalarını filtreler
                data= Uri.parse("mailto:")  //intent’in mail için olduğunu belirtir
                putExtra(Intent.EXTRA_EMAIL,"usiappmobile@gmail.com")  //alıcı adres(ler)i (String[] olarak)
                putExtra(Intent.EXTRA_SUBJECT,"USIApp Destek Talebi")  //mail başlığı.
                putExtra(Intent.EXTRA_TEXT,"Merhaba,\\n\\nYaşadığım sorunla ilgili detaylar aşağıdadır:\\n") //mailin gövdesi (kullanıcı düzenleyebilir).
            }
            try {
                startActivity(Intent.createChooser(emailIntent,"E-posta uygulaması seçin:"))  //kullanıcıya Gmail, Outlook, Yandex gibi seçenekleri gösterir.

            }catch (e:ActivityNotFoundException){
                Toast.makeText(this@IndustrySettingsActivity,"E-posta uygulaması bulunamadı !",Toast.LENGTH_SHORT).show()
            }
        }

        //Çıkış yap
        binding.logOutIndustry.setOnClickListener {
            startActivity(Intent(this@IndustrySettingsActivity,MainActivity::class.java))
            auth.signOut()
        }

    }

    // Geri dön
    fun back(view: View) {
        finish()
    }
}