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
import com.example.usiapp.databinding.FragmentConsultancyFieldsBinding
import com.example.usiapp.view.repository.CreateCardAndAddData
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConsultancyFieldsFragment : Fragment() {

    private var _binding: FragmentConsultancyFieldsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val consultancyFieldsList = mutableListOf<String>()

    private lateinit var consultancyFieldsInput: EditText
    private lateinit var addConsultancy: Button
    private lateinit var consultancyContainer: LinearLayout
    private lateinit var txtNoConsultancy: TextView

    private lateinit var cardHelper: CreateCardAndAddData


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConsultancyFieldsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // View oluşturulduktan sonra yapılacak işlemler
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        consultancyFieldsInput = binding.consultancyOfArea
        addConsultancy = binding.addConsultancyInfo
        consultancyContainer = binding.consultancyInfoContainer
        txtNoConsultancy = binding.txtNoConsultancy

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return


        //Kayıtlı verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                    val consultancies = document.get("verebilecegiDanismanlikKonulari") as? List<String>
                    if (!consultancies.isNullOrEmpty()) {
                        consultancyFieldsList.addAll(consultancies)
                    }

                //CardHelper'ı başlat
                cardHelper = CreateCardAndAddData(
                    context = requireContext(),
                    container = consultancyContainer,
                    db = db,
                    documentId = documentId!!,
                    listKey = "verebilecegiDanismanlikKonulari",
                    itemList = consultancyFieldsList,
                    noDataTextView = txtNoConsultancy
                )

                //Kart oluştur
                consultancyFieldsList.forEach { cardHelper.createCard(it) }

                //Boş yazıyı kaldır
                if (consultancyFieldsList.isNotEmpty()) {
                    consultancyContainer.removeView(txtNoConsultancy)
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
        addConsultancy.setOnClickListener {
            val newConsultancy = consultancyFieldsInput.text.toString()
            cardHelper.addItem(newConsultancy, consultancyFieldsInput)
        }


        // Geri butonuna bas
        binding.goToBack.setOnClickListener {
            val intent = Intent(requireContext(), AcademicianActivity::class.java)
            startActivity(intent)
        }
    }


    // Fragment yok edildiğinde bindingi temizle
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
