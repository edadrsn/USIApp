package com.example.usiapp.view.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentEducationBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EducationFragment : Fragment() {

    private var _binding: FragmentEducationBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val educationList = mutableListOf<String>()

    private lateinit var educationInput: EditText
    private lateinit var addEducation: Button
    private lateinit var educationContainer: LinearLayout
    private lateinit var txtNoEducation: TextView

    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEducationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        educationInput = binding.educationOfArea
        addEducation = binding.addEducationInfo
        educationContainer = binding.educationContainer
        txtNoEducation = binding.txtNoEducation

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val education = document.get("verebilecegiEgitimler") as? List<String>
                if (!education.isNullOrEmpty()) {
                    educationList.addAll(education)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = requireContext(),
                    container = educationContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "verebilecegiEgitimler",
                    itemList = educationList,
                    noDataTextView = txtNoEducation
                )

                //Kart oluştur
                educationList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (educationList.isNotEmpty()) {
                    educationContainer.removeView(txtNoEducation)
                }
            },
            onFailure = {
                Toast.makeText(
                    requireContext(),
                    "Veri alınamadı: ${it.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )


        //Butona tıklama
        addEducation.setOnClickListener {
            val newEducation = educationInput.text.toString()
            cardHelper.addItem(newEducation, educationInput)
        }

        //Geri dön
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
