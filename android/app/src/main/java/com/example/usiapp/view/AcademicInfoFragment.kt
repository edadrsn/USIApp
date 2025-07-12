package com.example.usiapp.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentAcademicInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AcademicInfoFragment : Fragment() {

    private var _binding: FragmentAcademicInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null
    private lateinit var academicInfo: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAcademicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Butona basınca güncellemek istediğine dair soru sor
        binding.btnUpdateAcademicInfo.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Güncelleme")
                setMessage("Akademik Geçmiş yazısını güncellemek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->
                    updateAcademicInfo()
                    dialog.dismiss()
                }
                setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        academicInfo = binding.academicEditText

        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            getAcademicInfo(currentUserEmail)
        }


        //Geri dön
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }
    }


    private fun getAcademicInfo(email: String) {
        db.collection("AcademicianInfo")
            .whereEqualTo("Email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    this.documentId = doc.id
                    val getAcademicInfo = doc.getString("akademikGecmis") ?: ""

                    academicInfo.setText(getAcademicInfo)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Akademisyen bilgisi bulunamadı !",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }

    }

    private fun updateAcademicInfo() {
        val updateAcademicInfo = binding.academicEditText.text.toString()
        if (updateAcademicInfo.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Akademik Geçmiş alanı boş bırakılamaz !",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val updates = hashMapOf<String, Any>(
            "akademikGecmis" to updateAcademicInfo
        )

        db.collection("AcademicianInfo").document(documentId!!)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Bilgiler başarıyla güncellendi.",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Güncelleme başarısız: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}