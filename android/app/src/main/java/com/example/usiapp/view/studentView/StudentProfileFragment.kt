package com.example.usiapp.view.studentView

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentStudentProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class StudentProfileFragment : Fragment() {

    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    var selectedPicture: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val currentUserEmail = auth.currentUser?.email

        //Verileri çek
        db.collection("Students")
            .whereEqualTo("studentEmail", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    binding.studentNameTxt.setText(document.getString("studentName") ?: "")
                    binding.studentEmailTxt.setText(document.getString("studentEmail") ?: "")

                    val getPhoto = document.getString("studentImage")
                    if (!getPhoto.isNullOrEmpty()) {
                        Picasso.get()
                            .load(getPhoto)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(binding.studentImage)
                    }
                }
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

        binding.cardStudentInfo.setOnClickListener {
            startActivity(Intent(requireContext(), StudentInfoActivity::class.java))
        }

        binding.cardUniversityInfo.setOnClickListener {
            startActivity(Intent(requireContext(), UniversityInfoActivity::class.java))
        }

        binding.cardStudentDepartmentInfo.setOnClickListener {
            startActivity(Intent(requireContext(), DepartmentInfoActivity::class.java))
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(requireContext(),StudentSettingsActivity::class.java))
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
                                binding.studentImage.setImageBitmap(selectedBitmap)
                            } else {
                                // Android 8 ve altı için MediaStore
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().contentResolver,
                                    selectedPicture
                                )
                                binding.studentImage.setImageBitmap(selectedBitmap)
                            }


                            // Firebase Storage ve Firestore'a yükleme örneği
                            if (selectedPicture != null) {
                                val uuid = UUID.randomUUID()
                                val imageName = "$uuid.jpg"

                                // Storage referansı
                                val storageRef =
                                    storage.reference.child("student_images").child(imageName)

                                storageRef.putFile(selectedPicture!!)
                                    .addOnSuccessListener {
                                        // URL'yi al ve Firestore'a kaydet
                                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                                            val downloadUrl = uri.toString()

                                            // Burada Industry koleksiyonundaki ilgili dokümanı güncelliyoruz
                                            val currentUserEmail = auth.currentUser?.email

                                            if (currentUserEmail != null) {
                                                db.collection("Students")
                                                    .whereEqualTo(
                                                        "studentEmail",
                                                        currentUserEmail
                                                    ) // Kullanıcıya ait firmayı bul
                                                    .get()
                                                    .addOnSuccessListener { querySnapshot ->
                                                        if (!querySnapshot.isEmpty) {
                                                            val documentId =
                                                                querySnapshot.documents[0].id

                                                            db.collection("Students")
                                                                .document(documentId)
                                                                .update(
                                                                    "studentImage",
                                                                    downloadUrl
                                                                )
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                        requireContext(),
                                                                        "Resim başarıyla güncellendi",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Toast.makeText(
                                                                        requireContext(),
                                                                        "Firestore hata: ${e.localizedMessage}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        } else {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Firma bulunamadı",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Sorgu hatası: ${e.localizedMessage}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            requireContext(),
                                            "Storage hata: ${e.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
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

}