package com.usisoftware.usiapp.view.academicianView

import android.content.Intent
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.FragmentProfileBinding
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    var selectedBitmap: Bitmap? = null
    var selectedPicture: Uri? = null

    private lateinit var switchProject: Switch

    // Photo Picker launcher
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPicture = uri

            try {
                selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }

                binding.imgUser.setImageBitmap(selectedBitmap)

                // Firebase Storage'a yükleme
                val userId = auth.currentUser?.uid ?: return@registerForActivityResult
                val imageName = "$userId.jpg"
                val storageRef = storage.reference.child("academician_images").child(imageName)

                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            db.collection("Academician")
                                .document(userId)
                                .update("photo", downloadUrl.toString())
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Resim güncellendi", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Güncelleme hatası: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Yükleme hatası: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Resim işleme hatası", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(requireContext(), "Resim seçilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        val ortakProjeTalep = document.getString("ortakProjeTalep") ?: "Hayır"
                        val isChecked = ortakProjeTalep.equals("Evet", ignoreCase = true)
                        switchProject.isChecked = isChecked
                        setSwitchUI(isChecked)
                    }
                }
        }

        //KARTLARA TIKLAMA İŞLEMİ
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

        //Resim seçme Photo Picker)
        binding.addImage.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

    }

    private fun checkIfUserIsAdmin(userId: String) {

        db.collection("Admins")
            .document(userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && documents.exists()) {
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
        binding.swipeRefreshLayout.isRefreshing = true

        db.collection("Academician")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

                if (document != null && document.exists()) {
                    val name = try { document.getString("adSoyad") ?: "" } catch(e: Exception) { "" }
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
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
