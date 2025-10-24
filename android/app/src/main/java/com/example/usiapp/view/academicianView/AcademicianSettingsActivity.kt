package com.example.usiapp.view.academicianView

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.usiapp.databinding.ActivityAcademicianSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AcademicianSettingsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAcademicianSettingsBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityAcademicianSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        db=FirebaseFirestore.getInstance()

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
            startActivity(Intent(this@AcademicianSettingsActivity,PreviewActivity::class.java))
        }

        //Şifremi unuttum sayfasına git
        binding.forgotPassword.setOnClickListener{
            startActivity(Intent(this@AcademicianSettingsActivity,UpdatePasswordActivity::class.java))
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

            }catch (e: ActivityNotFoundException){
                Toast.makeText(this@AcademicianSettingsActivity,"E-posta uygulaması bulunamadı !",
                    Toast.LENGTH_SHORT).show()
            }
        }

        //Görüş ve öneri gönder sayfasına git
        binding.opinionAndSuggestion.setOnClickListener{
            startActivity(Intent(this@AcademicianSettingsActivity,OpinionAndSuggestionActivity::class.java))
        }

        //Hesabımı sil
        binding.deleteAccount.setOnClickListener{
            showDeleteConfirmationDialog()
        }
        //Çıkış Yap
        binding.logout.setOnClickListener {
            // Ana sayfaya (MainActivity) yönlendir
            startActivity(Intent(this, MainActivity::class.java))
            auth.signOut()

        }
    }


    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hesabı Sil")
            .setMessage("Hesabınızı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Evet") { _, _ ->
                deleteUserAccount()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteUserAccount() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid

            // Firestore'da bu kullanıcıya ait dokümanı bul
            db.collection("AcademicianInfo")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val docId = querySnapshot.documents[0].id

                        // Dokümanı sil
                        db.collection("AcademicianInfo").document(docId)
                            .delete()
                            .addOnSuccessListener {
                                // Firebase Authentication hesabını sil
                                user.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Hesabınız silindi.", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Hesap silinemedi", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Veri silinemedi", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Kullanıcı verisi bulunamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Sorgu hatası", Toast.LENGTH_SHORT).show()
                }
        }
    }



    // Geri dön
    fun back(view: View) {
        finish()
    }
}