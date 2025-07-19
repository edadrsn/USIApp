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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentFirmInfoBinding
import com.example.usiapp.view.model.Firm
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class FirmInfoFragment : Fragment() {

    private var _binding: FragmentFirmInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var firmNameInput: EditText
    private lateinit var workAreaInput: EditText
    private lateinit var firmContainer: LinearLayout
    private lateinit var workAreaTagContainer: FlexboxLayout
    private lateinit var emptyMessage: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val firmList = mutableListOf<Firm>()
    private val tempWorkAreas = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirmInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firmNameInput = binding.firmName
        workAreaInput = binding.firmWorkArea
        firmContainer = binding.firmContainer
        workAreaTagContainer = binding.workAreaTagContainer //alanların ekleneceği container
        emptyMessage = binding.txtNoFirm

        val btnAdd = binding.addFirmInfo  //ekle
        val btnAddWorkArea = binding.addFirmWorkArea  //alan oluştur

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        // Verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                try {
                    val firmData = document.get("firmalar") as? List<Map<String, Any>>
                    firmData?.forEach { firmMap ->
                        val firmaAdi = firmMap["firmaAdi"] as? String ?: ""
                        val calismaAlani = firmMap["firmaCalismaAlani"] as? List<String> ?: emptyList()

                        val firm = Firm(firmaAdi, calismaAlani, documentId.toString())
                        firmList.add(firm)
                        createFirmCard(firm)
                    }

                    // Firma varsa mesajı gizle, yoksa göster
                    emptyMessage.visibility = if (firmList.isNotEmpty()) View.GONE else View.VISIBLE

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            onFailure = {}
        )

        // + Butonuna basıldığında çalışma alanlarını geçici olarak biriktir
        btnAddWorkArea.setOnClickListener {
            val area = workAreaInput.text.toString().trim()
            if (area.isNotEmpty()) {
                tempWorkAreas.add(area)
                addTagToContainer(area)
                workAreaInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "Lütfen çalışma alanı girin", Toast.LENGTH_SHORT).show()
            }
        }

        // Firma Ekle butonu
        btnAdd.setOnClickListener {
            val getFirmName = firmNameInput.text.toString().trim()

            if (getFirmName.isEmpty() || tempWorkAreas.isEmpty()) {
                Toast.makeText(requireContext(), "Boş alan bırakmayın", Toast.LENGTH_SHORT).show()
            } else {

                emptyMessage.visibility = View.GONE

                val newFirm = Firm(getFirmName, tempWorkAreas.toList(), documentId.toString(), UUID.randomUUID().toString())
                firmList.add(newFirm)
                if (firmList.isNotEmpty()) emptyMessage.visibility = View.GONE
                createFirmCard(newFirm)

                val firmMapList = firmList.map {
                    mapOf(
                        "firmaAdi" to it.firmaAdi,
                        "firmaCalismaAlani" to it.calismaAlani,
                        "id" to it.id
                    )
                }

                documentId?.let {
                    db.collection("AcademicianInfo").document(it)
                        .update("firmalar", firmMapList)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Firma bilgisi eklendi", Toast.LENGTH_SHORT).show()

                            // Başarılıysa buradaki temizleme işlemleri
                            tempWorkAreas.clear()
                            workAreaTagContainer.removeAllViews()
                            firmNameInput.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }


    }

    //Alanları geçici olarak eklediğim container
    private fun addTagToContainer(text: String) {
        val tag = TextView(requireContext()).apply {
            this.text = text
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_background)
            setMargins(8, 8, 8, 8)
        }
        workAreaTagContainer.addView(tag)
    }


    private fun TextView.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }

    // Kart oluşturma
    private fun createFirmCard(firm: Firm) {
        val cardLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 10)
            }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)
            setPadding(24, 24, 24, 24)
        }

        val textLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val firmNameText = TextView(requireContext()).apply {
            text = firm.firmaAdi
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            textSize = 17f
        }

        val workAreaText = TextView(requireContext()).apply {
            text = firm.calismaAlani.joinToString(" • ")
            setTextColor(Color.DKGRAY)
            textSize = 15f
            setPadding(0, 6, 0, 0)
        }

        textLayout.addView(firmNameText)
        textLayout.addView(workAreaText)

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.baseline_delete_24)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(70, 70).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Bilgi Silinsin mi?")
                    setMessage("Bu firma bilgisini silmek istediğinize emin misiniz?")
                    setPositiveButton("Evet") { dialog, _ ->
                        firmContainer.removeView(cardLayout)
                        firmList.remove(firm)

                        // Silme sonrası boşsa mesajı göster, doluysa gizle
                        emptyMessage.visibility = if (firmList.isEmpty()) View.VISIBLE else View.GONE

                        val updatedFirmList = firmList.map {
                            mapOf(
                                "firmaAdi" to it.firmaAdi,
                                "firmaCalismaAlani" to it.calismaAlani
                            )
                        }
                        db.collection("AcademicianInfo").document(documentId!!)
                            .update("firmalar", updatedFirmList)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Firma silindi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Silme başarısız: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                    }
                    setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                    create().show()
                }
            }
        }

        cardLayout.addView(textLayout)
        cardLayout.addView(deleteButton)
        firmContainer.addView(cardLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
