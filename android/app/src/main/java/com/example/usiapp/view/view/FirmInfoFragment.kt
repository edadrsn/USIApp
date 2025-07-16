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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirmInfoFragment : Fragment() {

    private var _binding: FragmentFirmInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var firmNameInput: EditText
    private lateinit var workAreaInput: EditText
    private lateinit var firmContainer: LinearLayout
    private lateinit var emptyMessage: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null

    private val firmList = mutableListOf<Firm>()

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
        emptyMessage=binding.txtNoFirm
        val btnAdd = binding.addFirmInfo

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        //Verileri çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = {document->
                documentId=document.id
                try {
                    val firmData = document.get("firmalar") as? List<HashMap<String, String>>
                    firmData?.forEach {
                        val firm = Firm(
                            it["firma"] ?: "",
                            it["firmaCalismaAlani"] ?: ""
                        )
                        firmList.add(firm)
                        createFirmCard(firm)
                    }
                    if (firmList.isNotEmpty()) firmContainer.removeView(emptyMessage)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            onFailure = {
                Toast.makeText(requireContext(),"Veri alınamadı: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
            }
        )


        //Yeni firma bilgisi oluşturma
        btnAdd.setOnClickListener {
            val getFirmName=firmNameInput.text.toString()
            val getWorkArea=workAreaInput.text.toString()

            if(getFirmName.isEmpty() || getWorkArea.isEmpty()){
                Toast.makeText(requireContext(),"Lütfen boş alan bırakmayınız!",Toast.LENGTH_LONG).show()
            }

            val newFirm=Firm(getFirmName,getWorkArea)
            firmList.add(newFirm)

            //Firebase kaydet
            val firmMapList=firmList.map {
                mapOf("firma" to it.firma , "firmaCalismaAlani" to it.calismaAlani)
            }

            if(documentId!=null){
                db.collection("AcademicianInfo").document(documentId.toString())
                    .update("firmalar",firmMapList)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Firma eklendi", Toast.LENGTH_SHORT).show()
                        createFirmCard(newFirm) // Yeni kart oluştur
                        firmNameInput.text.clear()
                        workAreaInput.text.clear()
                        firmContainer.removeView(emptyMessage)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),"Hata: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                    }
            }else{
                Toast.makeText(requireContext(),"Belge ID bulunamadı",Toast.LENGTH_LONG).show()
            }
        }





        //Geri dön
        binding.goToBack.setOnClickListener {
            startActivity(Intent(requireContext(), AcademicianActivity::class.java))
        }
    }


    //Kart oluşturma
    private fun createFirmCard(firm: Firm) {
        // Kartın ana layout'u yatay LinearLayout
        val cardLayout = LinearLayout(requireContext()).apply {
            // içerikler yatay olcak
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // kart kenar boşlukları ekledim
                setMargins(25, 16, 25, 0)
            }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)
            elevation = 6f // gölge
            setPadding(24, 24, 24, 24) // iç boşluk verdim
        }

        // Firma adı ve çalışma alanı için dikey LinearLayout
        val textLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, // genişlik: 0 verilir çünkü weight kullanılacak
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // ağırlık: mevcut alanın çoğu buraya verilir
            )
        }

        // Firma adını gösteren TextView
        val firmNameText = TextView(requireContext()).apply {
            text = firm.firma
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD) // kalın yazı tipi
            textSize = 17f
        }

        // Çalışma alanını gösteren TextView
        val workAreaText = TextView(requireContext()).apply {
            text = firm.calismaAlani
            setTextColor(Color.DKGRAY)
            textSize = 15f
            setPadding(0, 6, 0, 0) // üst boşluk
        }

        // TextView'ler dikey layout'a ekleniyor
        textLayout.addView(firmNameText)
        textLayout.addView(workAreaText)

        // Sağda ortalanmış çöp kutusu (silme) butonu
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.baseline_delete_24)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                70,
                70
            ).apply {
                gravity = Gravity.CENTER_VERTICAL // dikeyde ortala
            }

            // Sil butonuna tıklanınca
            setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Bilgi Silinsin mi?")
                    setMessage("Bu firma bilgisini silmek istediğinize emin misiniz?")


                    setPositiveButton("Evet") { dialog, _ ->
                        // Kartı arayüzden kaldır
                        firmContainer.removeView(cardLayout)

                        // firmList listesinden çıkar
                        firmList.remove(firm)

                        // Geriye kalanları Firebase'e yeniden gönder
                        val updatedFirmList = firmList.map {
                            mapOf(
                                "firma" to it.firma,
                                "firmaCalismaAlani" to it.calismaAlani
                            )
                        }

                        // Firestore güncelle
                        db.collection("AcademicianInfo").document(documentId!!)
                            .update("firmalar", updatedFirmList)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Firma silindi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Silme başarısız: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }

                        // Tüm firmalar silinmişse boş mesajı göster
                        if (firmList.isEmpty()) firmContainer.addView(emptyMessage)

                        dialog.dismiss()
                    }


                    setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }

                    create().show()
                }
            }
        }

        // Kartın içine layoutları ekle
        cardLayout.addView(textLayout)
        cardLayout.addView(deleteButton)

        // Kartı ana layout'a (firmContainer) ekle
        firmContainer.addView(cardLayout)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
