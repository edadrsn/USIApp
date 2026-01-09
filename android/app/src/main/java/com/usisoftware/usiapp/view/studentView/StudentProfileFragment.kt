package com.usisoftware.usiapp.view.studentView

import android.content.Intent
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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

    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
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

        registerPhotoPicker()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        loadInfo()

        binding.swipeRefreshLayout.setOnRefreshListener { loadInfo() }

        binding.addImage.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
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

    private fun registerPhotoPicker() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

                if (uri != null) {
                    selectedPicture = uri

                    try {
                        selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(requireContext().contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                        }

                        binding.studentImage.setImageBitmap(selectedBitmap)

                        uploadImageToFirebase(uri)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Resim yüklenemedi", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("student_images").child("$uid.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    db.collection("Students")
                        .document(uid)
                        .update("studentImage", downloadUri.toString())
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

        binding.studentNameTxt.text = "Öğrenci isim bilgisi girilmedi"
        binding.studentEmailTxt.text = "Email bilgisi girilmedi"
        binding.studentImage.setImageResource(R.drawable.person)

        db.collection("Students")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

                if (document != null && document.exists()) {

                    // Öğrenci adı
                    val studentName = document.getString("studentName")
                    binding.studentNameTxt.text =
                        if (!studentName.isNullOrBlank()) studentName else "Öğrenci bilgisi girilmedi"

                    // Öğrenci email
                    val studentEmail = document.getString("studentEmail")
                    binding.studentEmailTxt.text =
                        if (!studentEmail.isNullOrBlank()) studentEmail else "Email bilgisi girilmedi"

                    // Profil resmi
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
            .addOnFailureListener {
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Hata: veri alınamadı", Toast.LENGTH_SHORT).show()
            }
    }
}
