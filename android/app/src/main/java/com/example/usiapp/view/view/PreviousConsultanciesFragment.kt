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
import com.example.usiapp.databinding.FragmentPreviousConsultanciesBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreviousConsultanciesFragment : Fragment() {

    private var _binding: FragmentPreviousConsultanciesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val prevConsultanciesList = mutableListOf<String>()

    private lateinit var prevConsultancies: EditText
    private lateinit var addPrevConsultancy: Button
    private lateinit var prevConsultancyContainer: LinearLayout
    private lateinit var txtNoConsultancy: TextView

    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviousConsultanciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prevConsultancies = binding.prevConsultancyOfArea
        addPrevConsultancy = binding.addPrevConsultancyInfo
        prevConsultancyContainer = binding.prevConsultancyContainer
        txtNoConsultancy = binding.txtNoConsultancy

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val prevConsultancy = document.get("dahaOncekiDanismanliklar") as? List<String>
                if (!prevConsultancy.isNullOrEmpty()) {
                    prevConsultanciesList.addAll(prevConsultancy)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = requireContext(),
                    container = prevConsultancyContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "dahaOncekiDanismanliklar",
                    itemList = prevConsultanciesList,
                    noDataTextView = txtNoConsultancy
                )

                //Kart oluştur
                prevConsultanciesList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (prevConsultanciesList.isNotEmpty()) {
                    prevConsultancyContainer.removeView(txtNoConsultancy)
                }

            },
            onFailure = {}
        )

        //Butona tıklama
        addPrevConsultancy.setOnClickListener {
            val newPrevConsultancy = prevConsultancies.text.toString()
            cardHelper.addItem(newPrevConsultancy, prevConsultancies)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}