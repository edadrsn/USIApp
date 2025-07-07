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

        val switchProject = binding.switchProject
        var isProjectSelected = switchProject.isChecked

        fun setSwitchColor(isChecked: Boolean) {
            val color = if (isChecked) "#4EA222" else "#FF0000"
            val colorStateList = ColorStateList.valueOf(Color.parseColor(color))
            switchProject.thumbTintList = colorStateList
            switchProject.trackTintList = colorStateList
        }

        setSwitchColor(isProjectSelected)

        switchProject.setOnCheckedChangeListener { _, isChecked ->
            isProjectSelected = isChecked
            setSwitchColor(isChecked)
        }

        registerLauncher()


        val bottomNavigation = binding.bottomNavigation


        // Bu satır eksikti → Profile sekmesini seçili göster
        bottomNavigation.selectedItemId = R.id.profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, PreviewActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.profile -> {
                    // Zaten bu sayfadasın, hiçbir şey yapma
                    true
                }

                else -> false
            }
        }



        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

// 🔒 Giriş kontrolü
        if (auth.currentUser == null) {
            val intent = Intent(this, AcademicianLoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

//Giriş varsa maili al ve veriyi çek
        val currentUserEmail = auth.currentUser?.email?.trim()?.lowercase()

        Log.d("AKADEMISYEN_MAIL", "Giriş yapan: $currentUserEmail")

        if (currentUserEmail != null) {
            getAcademicianInfo(currentUserEmail)
        }


    }

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

                    binding.txtName.text = name
                    binding.txtEmail.text = mail

                    if (photoUrl.isNotEmpty()) {
                        Picasso.get().load(photoUrl).into(binding.imgUser)
                    } else {
                        binding.imgUser.setImageResource(R.drawable.person)
                    }

                } else {
                    binding.txtName.text = "Ad Soyad Yok"
                    binding.txtEmail.text = email
                    binding.imgUser.setImageResource(R.drawable.person)
                    Log.d("NO_MATCH", "Eşleşen akademisyen bulunamadı.")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("FIRESTORE_ERROR", e.toString())
            }
    }


    fun uploadPhoto(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    Snackbar.make(view, "Permission Needed For Gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission") {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Snackbar.make(view, "Permission Needed For Gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission") {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedPicture = intentFromResult.data
                        try {
                            selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    this@AcademicianActivity.contentResolver,
                                    selectedPicture!!
                                )
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(
                                    this@AcademicianActivity.contentResolver,
                                    selectedPicture
                                )
                            }
                            //binding.imageView3.setImageBitmap(selectedBitmap)
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
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    Toast.makeText(
                        this@AcademicianActivity,
                        "Permission needed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun personalInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, PersonalInfoActivity::class.java)
        startActivity(intent)

    }

    fun contactInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, ContactInfoActivity::class.java)
        startActivity(intent)
    }

    fun academicInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, AcademicInfoActivity::class.java)
        startActivity(intent)
    }

    fun firmInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, FirmInfoActivity::class.java)
        startActivity(intent)
    }

    fun professionInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, ProfessionInfoActivity::class.java)
        startActivity(intent)
    }

    fun consultancyInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, ConsultancyFieldsActivity::class.java)
        startActivity(intent)
    }

    fun previousConsultanciesInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, PreviousConsultanciesActivity::class.java)
        startActivity(intent)
    }

    fun educationInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, EducationActivity::class.java)
        startActivity(intent)
    }

    fun previousEducationInfo(view: View) {
        val intent = Intent(this@AcademicianActivity, PreviousEducationActivity::class.java)
        startActivity(intent)
    }

    fun goToBack(view: View) {
        val intent = Intent(this@AcademicianActivity, AcademicianLoginActivity::class.java)
        startActivity(intent)
    }

    fun signOut(view: View) {
        auth.signOut()
        val intent = Intent(this@AcademicianActivity, MainActivity::class.java)
        startActivity(intent)
    }

}