package com.usisoftware.usiapp.view.studentView

import android.Manifest
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.FragmentStudentProfileBinding
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

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
    ): View {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        loadInfo()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadInfo()
        }

        binding.addImage.setOnClickListener {
            openGalleryWithPermission(view)
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
            startActivity(Intent(requireContext(), StudentSettingsActivity::class.java))
        }
    }

    private fun openGalleryWithPermission(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                    )
                        .setAction("İzin Ver") {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                openGallery()
            }
        } else {
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
                    )
                        .setAction("İzin Ver") {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intent)
    }

    private fun registerLauncher() {

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {

                    val intentFromResult = result.data
                    if (intentFromResult != null) {

                        selectedPicture = intentFromResult.data

                        selectedPicture?.let { uri ->

                            try {
                                selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(
                                        requireContext().contentResolver,
                                        uri
                                    )
                                    ImageDecoder.decodeBitmap(source)
                                } else {
                                    MediaStore.Images.Media.getBitmap(
                                        requireContext().contentResolver,
                                        uri
                                    )
                                }

                                binding.studentImage.setImageBitmap(selectedBitmap)

                                uploadImageToFirebase(uri)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    openGallery()
                } else {
                    Toast.makeText(requireContext(), "İzin gerekiyor!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {

        val uid = auth.currentUser?.uid ?: return
        val imageName = "$uid.jpg"

        val storageRef = storage.reference.child("student_images").child(imageName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->

                    val downloadUrl = uri.toString()

                    db.collection("Students")
                        .document(uid)
                        .update("studentImage", downloadUrl)
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
    }

    private fun loadInfo() {

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }
        binding.swipeRefreshLayout.isRefreshing = true

        db.collection("Students")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

                if (document != null && document.exists()) {

                    binding.studentNameTxt.setText(document.getString("studentName") ?: "")
                    binding.studentEmailTxt.setText(document.getString("studentEmail") ?: "")

                    val imageUrl = document.getString("studentImage")

                    if (!imageUrl.isNullOrEmpty()) {
                        loadImageWithCorrectRotation(
                            requireContext(),
                            imageUrl,
                            binding.studentImage,
                            R.drawable.person
                        )
                    } else {
                        binding.studentImage.setImageResource(R.drawable.person)
                    }
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
