package com.usisoftware.usiapp.view.academicianView

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityAcademicianSettingsBinding

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
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }


    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hesabı Sil")
            .setMessage("Hesabınızı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Evet") { _, _ ->
                requestPasswordForDeletion()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    // Şifre sor ve reauthenticate et
    private fun requestPasswordForDeletion() {
        val passwordInput = EditText(this)
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Şifrenizi girin")
            .setMessage("Hesabınızı silmek için şifrenizi tekrar girin")
            .setView(passwordInput)
            .setPositiveButton("Onayla") { _, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    reauthenticateAndDelete(password)
                } else {
                    Toast.makeText(this, "Şifre boş olamaz", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun reauthenticateAndDelete(password: String) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email.isNullOrEmpty()) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                // Reauthenticate başarılı → Firestore ve kullanıcı sil
                deleteStudentAccount(user.uid)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Şifre yanlış veya işlem başarısız!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteStudentAccount(userId: String) {
        db.collection("Academician").document(userId)
            .delete()
            .addOnSuccessListener {
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Hesabınız silindi.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Hesap silinemedi!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veri silinemedi!", Toast.LENGTH_SHORT).show()
            }
    }


    // Geri dön
    fun back(view: View) {
        finish()
    }

}