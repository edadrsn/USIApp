package com.example.usiapp.view.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.example.usiapp.databinding.FragmentProfessionInfoBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfessionInfoFragment : Fragment() {

    private var _binding: FragmentProfessionInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var professionName: EditText
    private lateinit var noTextInfo: TextView
    private lateinit var addProfInfo: Button
    private lateinit var professionContainer: LinearLayout

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val professionList = mutableListOf<String>()

    private lateinit var cardHelper: CreateCardAndAddData


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfessionInfoBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        professionName = binding.professionText
        professionContainer = binding.professionInfoContainer
        noTextInfo = binding.txtNoProfession
        addProfInfo = binding.addInfo

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        //Verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val professions = document.get("uzmanlikAlanlari") as? List<String>
                if (!professions.isNullOrEmpty()) {
                    professionList.addAll(professions)
                }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = requireContext(),
                    container = professionContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "uzmanlikAlanlari",
                    itemList = professionList,
                    noDataTextView = noTextInfo
                )

                //Kart oluştur
                professionList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (professionList.isNotEmpty()) {
                    professionContainer.removeView(noTextInfo)
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
        addProfInfo.setOnClickListener {
            val newProfession = professionName.text.toString()
            cardHelper.addItem(newProfession, professionName)
        }

        binding.goToBack.setOnClickListener {
            startActivity(Intent(requireContext(), AcademicianActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

