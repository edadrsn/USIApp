package com.example.usiapp.view.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianBinding
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

        //Fragment yönetimi
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.nestedScrollView.visibility = View.VISIBLE
            } else {
                binding.nestedScrollView.visibility = View.GONE
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

        // Switch buton kontrolü
        val switchProject = binding.switchProject


        // Switch UI görünümü güncelle
        fun setSwitchUI(isChecked: Boolean) {
            val colorHex = if (isChecked) "#4EA222" else "#FF0000"
            val color = Color.parseColor(colorHex)
            val colorStateList = ColorStateList.valueOf(color)

            switchProject.thumbTintList = colorStateList
            switchProject.trackTintList = colorStateList
            binding.project.strokeColor = color
        }


        // Firestore’a ortakProjeTalep değerini güncelle
        fun updateFirestore(isChecked: Boolean) {
            val ortakProjeTalep = if (isChecked) "Evet" else "Hayır"
            val data = mapOf("ortakProjeTalep" to ortakProjeTalep)

            val currentUser = auth.currentUser
            val userEmail = currentUser?.email

            if (userEmail != null) {
                db.collection("AcademicianInfo")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val documentId = documents.documents[0].id
                            db.collection("AcademicianInfo")
                                .document(documentId)
                                .update(data)
                                .addOnSuccessListener {
                                    Log.d(
                                        "FirestoreUpdate",
                                        "Switch durumu güncellendi: $ortakProjeTalep"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreUpdate", "Güncelleme hatası: ", e)
                                }
                        }
                    }
            }
        }

        // Switch ilk değeri ve UI ayarı
        switchProject.setOnCheckedChangeListener(null) // önce listener’ı temizle
        switchProject.isChecked = true // varsayılan olarak kapalı başlat (gerekirse)
        setSwitchUI(true)

        // Firestore'dan ortakProjeTalep değerini çekip switch’i güncelle
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("AcademicianInfo")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val ortakProjeTalep = doc.getString("ortakProjeTalep") ?: "Hayır"
                        val isChecked = ortakProjeTalep == "Evet"
                        switchProject.isChecked = isChecked
                        setSwitchUI(isChecked)
                    }
                }
        }

        // Switch değiştirildiğinde renkleri ve Firestore’u güncelle
        switchProject.setOnCheckedChangeListener { _, isChecked ->
            setSwitchUI(isChecked)
            updateFirestore(isChecked)
        }

        //Navigation işlemi
        val bottomNavigation = binding.bottomNavigation
        bottomNavigation.selectedItemId = R.id.profile
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.preview -> {
                    val intent = Intent(this, PreviewActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.profile -> true
                else -> false
            }
        }
    }

    //Geri tuşuna basma işlemi olduğunda
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            // Kısa bir gecikme verip görünürlüğü ayarla
            Handler(Looper.getMainLooper()).postDelayed({
                if (fragmentManager.backStackEntryCount == 0) {
                    binding.nestedScrollView.visibility = View.VISIBLE
                }
            }, 100)
        } else {
            super.onBackPressed()
        }
    }

    // Firestore’dan akademisyen bilgilerini çek
    private fun getAcademicianInfo(email: String) {
        db.collection("AcademicianInfo")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val name = doc.getString("adSoyad") ?: "İsimsiz"
                    val mail = doc.getString("email") ?: ""
                    val photoUrl = doc.getString("photo") ?: ""
                    val degree = doc.getString("unvan") ?: ""

                    binding.txtName.text = name
                    binding.txtEmail.text = mail
                    binding.txtDegree.text = degree

                    if (photoUrl.isNotEmpty()) {
                        Picasso.get().load(photoUrl).into(binding.imgUser)
                    } else {
                        binding.imgUser.setImageResource(R.drawable.person)
                    }
                } else {
                    binding.txtName.text = "Ad Soyad"
                    binding.txtEmail.text = email
                    binding.imgUser.setImageResource(R.drawable.person)
                    binding.txtDegree.text = "Unvan"
                    Log.d("NO_MATCH", "Eşleşen akademisyen bulunamadı.")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("FIRESTORE_ERROR", e.toString())
            }
    }

    // Sayfa yükleyici ve diğer buton fonksiyonları (değişmeden)
    fun loadFragment(fragment: Fragment) {
        try {
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
        } catch (e: Exception) {
            Toast.makeText(this, "Sayfa yüklenirken bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    fun personalInfo(view: View) = loadFragment(PersonalInfoFragment())
    fun contactInfo(view: View) = loadFragment(ContactInfoFragment())
    fun academicInfo(view: View) = loadFragment(AcademicInfoFragment())
    fun firmInfo(view: View) = loadFragment(FirmInfoFragment())
    fun professionInfo(view: View) = loadFragment(ProfessionInfoFragment())
    fun consultancyInfo(view: View) = loadFragment(ConsultancyFieldsFragment())
    fun previousConsultanciesInfo(view: View) = loadFragment(PreviousConsultanciesFragment())
    fun educationInfo(view: View) = loadFragment(EducationFragment())
    fun previousEducationInfo(view: View) = loadFragment(PreviousEducationsFragment())

    fun signOut(view: View) {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
