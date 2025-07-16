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
                try {
                    val professions = document.get("uzmanlikAlanlari") as? List<String>
                    if (!professions.isNullOrEmpty()) {
                        professionList.addAll(professions)
                        professions.forEach { info ->
                            createProfessionCard(info)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Uzmanlık alanı bulunamadı!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Hata:${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
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


        //Ekle butonu ile yeni veri oluşturma
        addProfInfo.setOnClickListener {
            val getProfession = binding.professionText.text.toString()
            professionList.add(getProfession)

            if (documentId != null) {
                db.collection("AcademicianInfo").document(documentId.toString())
                    .update("uzmanlikAlanlari", professionList)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Uzmanlık alanı eklendi",
                            Toast.LENGTH_LONG
                        ).show()

                        createProfessionCard(getProfession)
                        professionName.text.clear()
                        professionContainer.removeView(binding.txtNoProfession)

                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Hata:${it.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Belge ID bulunamadı", Toast.LENGTH_LONG).show()
            }
        }


        //Geri dön
        binding.goToBack.setOnClickListener {
            startActivity(Intent(requireContext(), AcademicianActivity::class.java))
        }

    }

    private fun createProfessionCard(profession: String) {
        //Ana layout
        val cardLayout = LinearLayout(requireContext()).apply {
            // içerikler yatay olcak
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
            ).apply {
                setMargins(25, 22, 25, 0)
            }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)
            setPadding(24, 24, 24, 24) // iç boşluk verdim
        }

        //Uzmanlık adı
        val textLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, // genişlik: 0 verilir çünkü weight kullanılacak
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // ağırlık: mevcut alanın çoğu buraya verilir
            )
        }

        val professionName = TextView(requireContext()).apply {
            text = profession
            setTextColor(Color.BLACK)
            textSize = 17f
            gravity = Gravity.CENTER_VERTICAL
        }

        textLayout.addView(professionName)

        //Silme butonu
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.baseline_delete_24)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                70, 70
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            //Silme iconuna tıklanınca
            setOnClickListener{
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Bilgi silinsin mi ?")
                    setMessage("Uzmanlık alanını silmek istediğinizden emin misiniz")
                    setPositiveButton("Evet"){dialog,_ ->
                        //Kartı kaldır
                        professionContainer.removeView(cardLayout)

                        //Listeden kaldır
                        professionList.remove(profession)

                        //Günncel listeyi firebase e yeniden gönder
                        db.collection("AcademicianInfo").document(documentId.toString())
                            .update("uzmanlikAlanlari",professionList)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(),"Uzmanlık alanı silindi",Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(),"Hata:${it.localizedMessage}",Toast.LENGTH_LONG).show()
                            }


                        dialog.dismiss()
                    }
                    setNegativeButton("Hayır"){dialog,_ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            }


        }

        cardLayout.addView(textLayout)
        cardLayout.addView(deleteButton)

        professionContainer.addView(cardLayout)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
