package com.usisoftware.usiapp.view.academicianView

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.FragmentProfileBinding
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation
import java.util.UUID

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    var selectedPicture: Uri? = null

    private lateinit var switchProject: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage=FirebaseStorage.getInstance()

        // Kullanıcı giriş yapmamışsa logine yönlendir
        if (auth.currentUser == null) {
            startActivity(Intent(requireContext(), AcademicianLoginActivity::class.java))
            return
        }

        // Giriş yapılmışsa kullanıcının mailine göre verileri al
        val userId = auth.currentUser?.uid ?: ""

        binding.swipeRefreshLayout.setOnRefreshListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                getAcademicianInfo(userId)
                checkIfUserIsAdmin(userId)
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }


        if (userId != null) {
            getAcademicianInfo(userId)
        }

        if (userId != null) {
            checkIfUserIsAdmin(userId)
            binding.cardAdmin.visibility = View.VISIBLE
        }
        

        // Switch buton kontrolü
        switchProject = binding.switchProject


        // Switch ilk değeri ve UI ayarı
        switchProject.setOnCheckedChangeListener(null) // önce listener’ı temizle
        switchProject.isChecked = true // varsayılan olarak kapalı başlat (gerekirse)
        setSwitchUI(true)

        // Firestore'dan ortakProjeTalep değerini çekip switch’i güncelle
        if (userId != null) {
            db.collection("Academician")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val doc = document.id
                        val ortakProjeTalep = document.getString("ortakProjeTalep") ?: "Hayır"
                        val isChecked = ortakProjeTalep.equals("Evet", ignoreCase = true)
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

        binding.cardAdmin.setOnClickListener {
            val intent = Intent(requireContext(), AdminPanelActivity::class.java)
            startActivity(intent)
        }

        binding.cardPersonal.setOnClickListener {
            val intent = Intent(requireContext(), PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        binding.cardContact.setOnClickListener {
            val intent = Intent(requireContext(), ContactInfoActivity::class.java)
            startActivity(intent)
        }

        binding.cardAcademic.setOnClickListener {
            val intent = Intent(requireContext(), AcademicInfoActivity::class.java)
            startActivity(intent)
        }

        binding.cardFirm.setOnClickListener {
            val intent = Intent(requireContext(), FirmInfoActivity::class.java)
            startActivity(intent)
        }

        binding.cardFirmWork.setOnClickListener {
            val intent = Intent(requireContext(), FirmInfoActivity::class.java)
            startActivity(intent)
        }

        binding.cardProfession.setOnClickListener {
            val intent = Intent(requireContext(), ProfessionInfoActivity::class.java)
            startActivity(intent)
        }

        binding.cardConsultancy.setOnClickListener {
            val intent = Intent(requireContext(), ConsultancyFieldsActivity::class.java)
            startActivity(intent)
        }

        binding.cardPreviousConsultancy.setOnClickListener {
            val intent = Intent(requireContext(), PreviousConsultanciesActivity::class.java)
            startActivity(intent)
        }

        binding.cardEducation.setOnClickListener {
            val intent = Intent(requireContext(), EducationActivity::class.java)
            startActivity(intent)
        }

        binding.cardPreviousEducation.setOnClickListener {
            val intent = Intent(requireContext(), PreviousEducationsActivity::class.java)
            startActivity(intent)
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(requireContext(),AcademicianSettingsActivity::class.java))
        }

        //Kartlara tıklama
        binding.addImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 ve sonrası
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Galeriye erişmek için izin gerekli",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("İzin Ver") {
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
                // Android 12 ve öncesi
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Galeriye erişmek için izin gerekli",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("İzin Ver") {
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

    }


    private fun checkIfUserIsAdmin(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Admins")
            .document(userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.exists()) {
                    // Kullanıcı admin
                    binding.cardAdmin.visibility = View.VISIBLE
                } else {
                    // Admin değil
                    binding.cardAdmin.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Admin kontrolü başarısız: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Firestore’dan akademisyen bilgilerini çek
    private fun getAcademicianInfo(userId: String) {
        db.collection("Academician")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val doc = document.id
                    val name = document.getString("adSoyad") ?: ""
                    val mail = document.getString("email") ?: ""
                    val photoUrl = document.getString("photo") ?: ""
                    val degree = document.getString("unvan") ?: ""

                    binding.txtName.text = name
                    binding.txtEmail.text = mail
                    binding.txtDegree.text = degree


                    if (!photoUrl.isNullOrEmpty()) {
                        loadImageWithCorrectRotation(requireContext(), photoUrl, binding.imgUser, R.drawable.person)
                    } else {
                        binding.imgUser.setImageResource(R.drawable.person)
                    }


                } else {
                    binding.txtName.text = "İsim bulunamadı"
                    binding.txtEmail.text = document.getString("email") ?: ""
                    binding.imgUser.setImageResource(R.drawable.person)
                    binding.txtDegree.text = "Unvan bulunamadı"
                    Log.d("NO_MATCH", "Eşleşen akademisyen bulunamadı.")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("FIRESTORE_ERROR", e.toString())
            }
    }


    // Firestore’a ortakProjeTalep değerini güncelle
    fun updateFirestore(isChecked: Boolean) {
        val ortakProjeTalep = if (isChecked) "Evet" else "Hayır"
        val data = mapOf("ortakProjeTalep" to ortakProjeTalep)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("Academician")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val documentId = document.id
                        db.collection("Academician")
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

    // Galeriye gitmek ve izin almak için launcher'ları kaydediyorum
    private fun registerLauncher() {
        // Galeriden resim seçtikten sonra ne yapacağımı burada belirtiyorum
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedPicture = intentFromResult.data
                        try {
                            // Android 9 ve üstü için ImageDecoder
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireContext().contentResolver,
                                    selectedPicture!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imgUser.setImageBitmap(selectedBitmap)
                            } else {
                                // Android 8 ve altı için MediaStore
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().contentResolver,
                                    selectedPicture
                                )
                                binding.imgUser.setImageBitmap(selectedBitmap)
                            }


                            // Firebase Storage ve Firestore'a yükleme örneği
                            if (selectedPicture != null) {
                                val userId = auth.currentUser?.uid ?: UUID.randomUUID().toString()
                                val imageName = "$userId.jpg"

                                // Storage referansı
                                val storageRef =
                                    storage.reference.child("academician_images").child(imageName)

                                storageRef.putFile(selectedPicture!!)
                                    .addOnSuccessListener {
                                        // URL'yi al ve Firestore'a kaydet
                                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                                            val downloadUrl = uri.toString()

                                            // Burada Academician koleksiyonundaki ilgili dokümanı güncelliyoruz
                                            val userId = auth.currentUser?.uid

                                            if (userId != null) {
                                                db.collection("Academician")
                                                    .document(userId)
                                                    .get()
                                                    .addOnSuccessListener { documentSnapshot ->
                                                        if (documentSnapshot.exists()) {
                                                            db.collection("Academician")
                                                                .document(userId)
                                                                .update("photo", downloadUrl)
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(requireContext(), "Resim başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                                                                }
                                                        } else {
                                                            Toast.makeText(requireContext(), "Akademisyen bulunamadı", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            }

                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Storage hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }


        // İzin alma işlemini burada kontrol ediyorum
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    // İzin verildiyse galeriyi aç
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    // İzin verilmediyse uyarı göster
                    Toast.makeText(requireContext(), "İzin gerekiyor!", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Switch UI görünümü güncelle
    fun setSwitchUI(isChecked: Boolean) {
        val colorHex = if (isChecked) "#4EA222" else "#FF0000"
        val color = Color.parseColor(colorHex)
        val colorStateList = ColorStateList.valueOf(color)

        switchProject.thumbTintList = colorStateList
        switchProject.trackTintList = colorStateList
        binding.project.strokeColor = color
    }

}