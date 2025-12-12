package com.usisoftware.usiapp.view.industryView

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
import com.usisoftware.usiapp.databinding.FragmentProfileIndustryBinding
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class ProfileIndustryFragment : Fragment() {

    private var _binding: FragmentProfileIndustryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    var selectedPicture: Uri? = null
    var selectedBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileIndustryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerPicker()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //Verileri çek
        loadInfo()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadInfo()
        }

        binding.addImage.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
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

    private fun registerPicker() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    selectedPicture = uri

                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver, uri
                            )
                        }

                        binding.imgIndustry.setImageBitmap(selectedBitmap)

                        uploadImageToFirebase(uri)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("industry_images").child("$uid.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("Industry")
                        .document(uid)
                        .update("requesterImage", downloadUrl.toString())
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

    fun loadInfo() {
        val uid = auth.currentUser?.uid ?: run {
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
                        loadImageWithCorrectRotation(
                            requireContext(),
                            getPhoto,
                            binding.imgIndustry,
                            R.drawable.person
                        )
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
            }
    }
}



