package com.example.usiapp.view.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentProfileBinding
import com.example.usiapp.view.academicianView.AcademicInfoActivity
import com.example.usiapp.view.academicianView.AcademicianLoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase auth-firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kullanıcı giriş yapmamışsa logine yönlendir
        if (auth.currentUser == null) {
            val intent = Intent(requireContext(), AcademicianLoginActivity::class.java)
            startActivity(intent)
            return
        }

        // Giriş yapılmışsa kullanıcının mailine göre verileri al
        val currentUserEmail = auth.currentUser?.email?.trim()?.lowercase()
        Log.d("AKADEMISYEN_MAIL", "Giriş yapan: $currentUserEmail")

        if (currentUserEmail != null) {
            getAcademicianInfo(currentUserEmail)
        }

        // Switch buton kontrolü
        val switchProject = binding.switchProject


        // Switch UI görünümü güncelle
        fun setSwitchUI(isChecked: Boolean) {
            val colorHex = if (isChecked) "#4EA222" else "#FF0000"
            val color = Color.parseColor(colorHex)
            val colorStateList = ColorStateList.valueOf(color)

            switchProject.thumbTintList = colorStateList
            switchProject.trackTintList = colorStateList
            binding.project.strokeColor = color
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

        binding.cardFirmWork.setOnClickListener{
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


}