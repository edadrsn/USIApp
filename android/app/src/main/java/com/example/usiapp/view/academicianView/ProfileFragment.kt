package com.example.usiapp.view.academicianView

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kullanıcı giriş yapmamışsa logine yönlendir
        if (auth.currentUser == null) {
            startActivity(Intent(requireContext(), AcademicianLoginActivity::class.java))
            return
        }

        // Giriş yapılmışsa kullanıcının mailine göre verileri al
        val currentUserEmail = auth.currentUser?.email?.trim()?.lowercase()


        if (currentUserEmail != null) {
            getAcademicianInfo(currentUserEmail)
        }

        if (currentUserEmail != null) {
            checkIfUserIsAdmin(currentUserEmail)
            binding.cardAdmin.visibility = View.VISIBLE
        }



        // Switch buton kontrolü
        switchProject = binding.switchProject


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

        binding.logout.setOnClickListener {
            // Firebase Authentication'dan çıkış yap
            auth.signOut()

            // Ana sayfaya (MainActivity) yönlendir
            startActivity(Intent(requireContext(), MainActivity::class.java))

            // Mevcut activity'i (veya fragment içinden çağrıldıysa host activity'yi) kapat
            requireActivity().finish()

        }

    }

    private fun checkIfUserIsAdmin(currentUserEmail: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Admins")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Kullanıcı admin
                    binding.cardAdmin.visibility = View.VISIBLE
                } else {
                    // Admin değil
                    binding.cardAdmin.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Admin kontrolü başarısız: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("FIRESTORE_ERROR", e.toString())
            }
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