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
import com.example.usiapp.databinding.FragmentPreviousEducationsBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreviousEducationsFragment : Fragment() {

    private var _binding: FragmentPreviousEducationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val prevEducationList = mutableListOf<String>()

    private lateinit var prevEducationInfo: EditText
    private lateinit var addPrevEdu: Button
    private lateinit var prevEduContainer: LinearLayout
    private lateinit var txtNo: TextView

    private lateinit var cardHelper: CreateCardAndAddData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviousEducationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        prevEducationInfo = binding.prevEducationOfArea
        addPrevEdu = binding.btnAddPrevEducation
        prevEduContainer = binding.prevEducationContainer
        txtNo = binding.txtNoEducation

        //Veri çekme
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id

                    val prevEducation = document.get("dahaOnceVerdigiEgitimler") as? List<String>
                    if (!prevEducation.isNullOrEmpty()) {
                        prevEducationList.addAll(prevEducation)
                    }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = requireContext(),
                    container = prevEduContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "dahaOnceVerdigiEgitimler",
                    itemList = prevEducationList,
                    noDataTextView = txtNo
                )

                //Kart oluştur
                prevEducationList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (prevEducationList.isNotEmpty()) {
                    prevEduContainer.removeView(txtNo)
                }

            },
            onFailure = {}
        )


        //Butona tıklama
        addPrevEdu.setOnClickListener {
            val newPrevEducation = prevEducationInfo.text.toString()
            cardHelper.addItem(newPrevEducation, prevEducationInfo)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
