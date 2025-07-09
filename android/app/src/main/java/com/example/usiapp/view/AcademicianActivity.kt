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
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityAcademicianBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class AcademicianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicianBinding
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    var selectedPicture: Uri? = null
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

        // Galeri işlemleri için gerekli izin kayıtları yapılır
        registerLauncher()

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

    // Kullanıcının galeriden fotoğraf seçmesini sağlayan fonksiyon
    fun uploadPhoto(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 ve üstü
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Galeriye erişim izni gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver") {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            // Android 12 ve altı
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Galeriye erişim izni gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver") {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    // İzin ve galeri işlemlerini yönet
    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedPicture = intentFromResult.data
                        try {
                            selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(contentResolver, selectedPicture!!)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(contentResolver, selectedPicture)
                            }
                            binding.imgUser.setImageBitmap(selectedBitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    Toast.makeText(this, "İzin gerekli!", Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun personalInfo(view: View) {
        startActivity(Intent(this, PersonalInfoActivity::class.java))
    }

    fun contactInfo(view: View) {
        startActivity(Intent(this, ContactInfoActivity::class.java))
    }

    fun academicInfo(view: View) {
        startActivity(Intent(this, AcademicInfoActivity::class.java))
    }

    fun firmInfo(view: View) {
        startActivity(Intent(this, FirmInfoActivity::class.java))
    }

    fun professionInfo(view: View) {
        startActivity(Intent(this, ProfessionInfoActivity::class.java))
    }

    fun consultancyInfo(view: View) {
        startActivity(Intent(this, ConsultancyFieldsActivity::class.java))
    }

    fun previousConsultanciesInfo(view: View) {
        startActivity(Intent(this, PreviousConsultanciesActivity::class.java))
    }

    fun educationInfo(view: View) {
        startActivity(Intent(this, EducationActivity::class.java))
    }

    fun previousEducationInfo(view: View) {
        startActivity(Intent(this, PreviousEducationActivity::class.java))
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
