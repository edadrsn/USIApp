package com.usisoftware.usiapp.view.industryView

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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.usisoftware.usiapp.databinding.FragmentProfileIndustryBinding
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class ProfileIndustryFragment : Fragment() {

    private var _binding: FragmentProfileIndustryBinding? = null
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
        _binding = FragmentProfileIndustryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //Verileri çek
        loadInfo()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadInfo()
        }

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

        binding.cardFirmInfo.setOnClickListener {
            startActivity(Intent(requireContext(), IndustryInfoActivity::class.java))
        }

        binding.cardFirmContactInfo.setOnClickListener {
            startActivity(Intent(requireContext(), IndustryContactInfoActivity::class.java))
        }

        binding.cardFirmAddressInfo.setOnClickListener {
            startActivity(Intent(requireContext(), IndustryAddressInfoActivity::class.java))
        }

        binding.cardFirmWorkerInfo.setOnClickListener {
            startActivity(Intent(requireContext(), IndustryWorkerInfoActivity::class.java))
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(requireContext(), IndustrySettingsActivity::class.java))
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
                                binding.imgIndustry.setImageBitmap(selectedBitmap)
                            } else {
                                // Android 8 ve altı için MediaStore
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().contentResolver,
                                    selectedPicture
                                )
                                binding.imgIndustry.setImageBitmap(selectedBitmap)
                            }

                            // Firebase Storage ve Firestore'a yükleme örneği
                            selectedPicture?.let { uri ->
                                val uid = auth.currentUser?.uid ?: return@let
                                val storageRef =
                                    storage.reference.child("industry_images").child("$uid.jpg")

                                try {
                                    storageRef.putFile(uri)
                                        .addOnSuccessListener {
                                            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                db.collection("Industry")
                                                    .document(uid)  // UID ile dokümanı güncelle
                                                    .update(
                                                        "requesterImage",
                                                        downloadUrl.toString()
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
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                requireContext(),
                                                "Storage hata: ${e.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "Resim yüklenirken bir hata oluştu",
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

    //Verileri çek
    fun loadInfo() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }
        binding.swipeRefreshLayout.isRefreshing = true

        db.collection("Industry")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

                if (document != null && document.exists()) {
                    binding.txtFirmName.setText(document.getString("firmaAdi") ?: "")
                    binding.txtFirmWorkArea.setText(document.getString("calismaAlanlari") ?: "")

                    val getPhoto = document.getString("requesterImage")
                    if (!getPhoto.isNullOrEmpty()) {
                        try {
                            loadImageWithCorrectRotation(
                                requireContext(),
                                getPhoto,
                                binding.imgIndustry,
                                R.drawable.person
                            )
                        } catch (e: Exception) {
                            binding.imgIndustry.setImageResource(R.drawable.person)
                        }
                    } else {
                        binding.imgIndustry.setImageResource(R.drawable.person)
                    }
                } else {
                    Toast.makeText(requireContext(), "Firma bulunamadı", Toast.LENGTH_SHORT).show()
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Veri alınırken hata oluştu: ${e.message}")

            }
    }
}



