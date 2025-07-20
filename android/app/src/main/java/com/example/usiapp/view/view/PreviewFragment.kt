package com.example.usiapp.view.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.usiapp.R
import com.example.usiapp.databinding.FragmentPreviewBinding
import com.example.usiapp.view.repository.GetAndUpdateAcademician
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class PreviewFragment : Fragment() {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var documentId: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding=FragmentPreviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return


        //Akademisyen verilerini çek
        GetAndUpdateAcademician.getAcademicianInfoByEmail(
            db,
            email,
            onSuccess = { document ->
                documentId = document.id
                val getPhoto = document.getString("photo") ?: ""
                if (!getPhoto.isNullOrEmpty()) {
                    Picasso.get()
                        .load(getPhoto)
                        .placeholder(R.drawable.person) // geçici resim
                        .error(R.drawable.person) // hata olursa
                        .into(binding.academicianPhoto)
                }

                val getName = document.getString("adSoyad") ?: ""
                binding.academicianName.setText(getName)

                val getDegree = document.getString("unvan") ?: ""
                binding.academicianDegree.setText(getDegree)

                val getAcademicInfo = document.getString("akademikGecmis") ?: ""
                binding.academicInfo.setText(getAcademicInfo)


                val projectRequest = document.getString("ortakProjeTalep") ?: "Hayır"
                val isChecked = projectRequest == "Evet"
                binding.switchProject.isChecked = isChecked
                setSwitchUI(isChecked)


                val getPhoneNum = document.getString("personelTel") ?: ""
                binding.phoneNum.setText(getPhoneNum)

                val getCorporateNum=document.getString("kurumsalTel") ?: ""
                binding.corporateNum.setText(getCorporateNum)

                val getEmail = document.getString("email") ?: ""
                binding.email.setText(getEmail)

                val getProvince=document.getString("il") ?: ""
                val getDistrict=document.getString("ilce") ?: ""
                val location=getProvince+" / "+getDistrict
                binding.districtAndProvince.setText(location)


                val getWebsite = document.getString("web") ?: ""
                binding.web.setText(getWebsite)


                val firmContainer = binding.firm
                firmContainer.removeAllViews()

                val firmData = document.get("firmalar") as? List<Map<String, Any>> ?: emptyList()

                firmData.forEach { firmMap ->
                    val firmaAdi = firmMap["firmaAdi"] as? String ?: "Firma adı yok"
                    val calismaAlaniList = firmMap["firmaCalismaAlani"] as? List<String> ?: emptyList()
                    val calismaAlaniText = calismaAlaniList.joinToString(separator = " • ")

                    val firmNameText = TextView(requireContext()).apply {
                        text = "💻 $firmaAdi"
                        setTextColor(Color.BLACK)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        setTypeface(null, Typeface.BOLD)
                    }

                    val workAreaText = TextView(requireContext()).apply {
                        text = "📍 $calismaAlaniText"
                        setTextColor(Color.DKGRAY)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    }

                    // Araya boşluk için spacer view
                    val spacer = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            20
                        )
                    }

                    // LinearLayout'a sırayla ekle
                    firmContainer.addView(firmNameText)
                    firmContainer.addView(workAreaText)
                    firmContainer.addView(spacer)
                }



                val getProfessions = document.get("uzmanlikAlanlari") as? List<String> ?: emptyList()
                binding.profession.text = getProfessions.joinToString(separator = "\n"){ "• $it" }

                val getConsultancyFields = document.get("verebilecegiDanismanlikKonulari") as? List<String> ?: emptyList()
                binding.consultancyFields.text = getConsultancyFields.joinToString(separator = "\n"){ "• $it" }

                val getPrevConsultancies = document.get("dahaOncekiDanismanliklar") as? List<String> ?: emptyList()
                binding.previousConsultancy.text = getPrevConsultancies.joinToString(separator = "\n"){ "• $it" }

                val getEducations = document.get("verebilecegiEgitimler") as? List<String> ?: emptyList()
                binding.educations.text = getEducations.joinToString(separator = "\n"){ "• $it" }

                val getPrevEducations = document.get("dahaOnceVerdigiEgitimler") as? List<String> ?: emptyList()
                binding.prevEducations.text = getPrevEducations.joinToString(separator = "\n"){ "• $it" }

            },
            onFailure = {
                println(
                    Toast.makeText(
                        requireContext(),
                        "Hata: ${it.localizedMessage} . Veri bulunamadı",
                        Toast.LENGTH_LONG
                    )
                )
            }
        )

    }

    // Switch rengini ve track, thumb renklerini ayarla
    fun setSwitchUI(isChecked: Boolean) {
        val colorHex = if (isChecked) "#4EA222" else "#FF0000"
        val color = Color.parseColor(colorHex)
        val colorStateList = ColorStateList.valueOf(color)

        binding.switchProject.thumbTintList = colorStateList
        binding.switchProject.trackTintList = colorStateList
        binding.project.strokeColor = color
    }
}