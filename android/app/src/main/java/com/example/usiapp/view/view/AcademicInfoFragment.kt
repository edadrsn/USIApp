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
import com.example.usiapp.databinding.FragmentAcademicInfoBinding
import com.example.usiapp.view.view.AcademicianActivity
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


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        academicInfo = binding.academicEditText

        val email=auth.currentUser?.email?: return

        //Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = {document->
                documentId=document.id
                val getAcademicInfo=document.getString("akademikGecmis") ?: ""

                academicInfo.setText(getAcademicInfo)

            },
            onFailure = {
                Toast.makeText(requireContext(),"Veri alınamadı: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
            }
        )

        //Butona basınca güncellemek istediğine dair soru sor
        binding.btnUpdateAcademicInfo.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Güncelleme")
                setMessage("Akademik Geçmiş bilgilerinizi güncellemek istediğinize emin misiniz?")
                setPositiveButton("Evet") { dialog, _ ->

                    val updateAcademicInfo = binding.academicEditText.text.toString()
                    val updates = hashMapOf<String, Any>(
                        "akademikGecmis" to updateAcademicInfo
                    )
                    GetAndUpdateAcademician.updateAcademicianInfo(
                        db,
                        documentId.toString(),
                        updates,
                        onSuccess = {
                            Toast.makeText(requireContext(),"Bilgiler başarıyla güncellendi.",Toast.LENGTH_LONG).show()
                        },
                        onFailure = {
                            Toast.makeText(requireContext(),"Hata: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                        }
                    )
                    dialog.dismiss()
                }
                setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
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