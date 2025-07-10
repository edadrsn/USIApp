package com.example.usiapp.view

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Camera
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class AcademicianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var imgUser: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Switch buton kontrolü
        val switchProject = binding.switchProject
        var isProjectSelected = switchProject.isChecked

        // Switch butonunun rengini evet/hayır durumuna göre değiştir
        fun setSwitchColor(isChecked: Boolean) {
            val color = if (isChecked) "#4EA222" else "#FF0000"
            val colorStateList = ColorStateList.valueOf(Color.parseColor(color))
            switchProject.thumbTintList = colorStateList
            switchProject.trackTintList = colorStateList
        }

        setSwitchColor(isProjectSelected)

        // Switch değiştirildiğinde rengini de değiştir
        switchProject.setOnCheckedChangeListener { _, isChecked ->
            isProjectSelected = isChecked
            setSwitchColor(isChecked)
        }


        //Navigation işlemi
        val bottomNavigation = binding.bottomNavigation

        // Başlangıçta profile seç
        bottomNavigation.selectedItemId = R.id.profile

        // Bottom nav menu tıklama işlemleri
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, PreviewActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.profile -> {
                    true
                }
                else -> false
            }
        }

        // Firebase auth-firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kullanıcı giriş yapmamışsa logine yönlendir
        if (auth.currentUser == null) {
            val intent = Intent(this, AcademicianLoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Giriş yapılmışsa kullanıcının mailine göre verileri al
        val currentUserEmail = auth.currentUser?.email?.trim()?.lowercase()
        Log.d("AKADEMISYEN_MAIL", "Giriş yapan: $currentUserEmail")

        if (currentUserEmail != null) {
            getAcademicianInfo(currentUserEmail)
        }
    }

    // Firestore'dan akademisyenin bilgilerini maile göre çek
    private fun getAcademicianInfo(email: String) {
        db.collection("AcademicianInfo")
            .whereEqualTo("Email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val name = doc.getString("adSoyad") ?: "İsimsiz"
                    val mail = doc.getString("Email") ?: ""
                    val photoUrl = doc.getString("resimURL") ?: ""
                    val degree=doc.getString("Unvan") ?: ""

                    // Verileri ekrana yazdır
                    binding.txtName.text = name
                    binding.txtEmail.text = mail
                    binding.txtDegree.text=degree

                    // Resim varsa göster, yoksa varsayılan fotoğraf göster
                    if (photoUrl.isNotEmpty()) {
                        Picasso.get().load(photoUrl).into(binding.imgUser)
                    } else {
                        binding.imgUser.setImageResource(R.drawable.person)
                    }

                } else {
                    // Akademisyen bulunamazsa varsayılan verileri göster
                    binding.txtName.text = "Ad Soyad"
                    binding.txtEmail.text = email
                    binding.imgUser.setImageResource(R.drawable.person)
                    binding.txtDegree.text="Unvan"
                    Log.d("NO_MATCH", "Eşleşen akademisyen bulunamadı.")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("FIRESTORE_ERROR", e.toString())
            }
    }


fun loadFragment(fragment: Fragment) {
    binding.nestedScrollView.visibility = View.GONE
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.fragment_slide_in_right,
            R.anim.fragment_slide_out_left,
            R.anim.fragment_slide_in_left,
            R.anim.fragment_slide_out_right
        )
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commit()
}



    fun personalInfo(view: View) {
        loadFragment(PersonalInfoFragment())
    }


    fun contactInfo(view: View) {
        loadFragment(ContactInfoFragment())
    }

    fun academicInfo(view: View) {
        loadFragment(AcademicInfoFragment())
    }

    fun firmInfo(view: View) {
        loadFragment(FirmInfoFragment())
    }

    fun professionInfo(view: View) {
        loadFragment(ProfessionInfoFragment())
    }

    fun consultancyInfo(view: View) {
        loadFragment(ConsultancyFieldsFragment())
    }

    fun previousConsultanciesInfo(view: View) {
        loadFragment(PreviousConsultanciesFragment())
    }

    fun educationInfo(view: View) {
        loadFragment(EducationFragment())
    }

    fun previousEducationInfo(view: View) {
        loadFragment(PreviousEducationsFragment())
    }


    fun goToBack(view: View) {
        startActivity(Intent(this, AcademicianLoginActivity::class.java))
    }

    // Oturumu kapat ve ana sayfaya git
    fun signOut(view: View) {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
