package com.example.usiapp.view.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.usiapp.R
import com.example.usiapp.view.academicianView.AcademicianPreviewActivity
import com.example.usiapp.view.model.Academician
import com.squareup.picasso.Picasso
import java.util.Locale

class AcademicianSearchAdapter(
    private val academicianList: List<Academician>,         // Başlangıç akademisyen listesi (tüm liste)
    private val onItemClick: (Academician) -> Unit          // Dışarıdan gelen tıklama fonksiyonu (ekle butonuna)
) : RecyclerView.Adapter<AcademicianSearchAdapter.AcademicianSearchViewHolder>() {
    // Akademisyenleri aramak ve göstermek için RecyclerView Adapter
    private var fullList = academicianList.toList()          // Tüm akademisyen verisi (değişmeden saklanır)
    private var filteredList = academicianList.toMutableList() // Filtre uygulanmış hali

    // Her kartın UI öğelerini tutan ViewHolder
    inner class AcademicianSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val academicianImage: ImageView = itemView.findViewById(R.id.academicianImage)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val btnAddAcademician: ImageView = itemView.findViewById(R.id.btnAddAcademician)
        val academicianContainer: LinearLayout = itemView.findViewById(R.id.academicianContainer)
    }

    // Her bir kart için layout oluştur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicianSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_academician_search_card, parent, false)
        return AcademicianSearchViewHolder(view)
    }

    // Her akademisyen kartına veriler ata
    override fun onBindViewHolder(holder: AcademicianSearchViewHolder, position: Int) {
        val academician = filteredList[position]

        // İsim ve unvan bilgisi
        holder.tvName.text = academician.academicianName
        holder.tvTitle.text = academician.academicianDegree

        // Profil resmi yükleniyorsa göster, yoksa placeholder
        if (academician.academicianImageUrl.isNotEmpty()) {
            Picasso.get()
                .load(academician.academicianImageUrl)
                .placeholder(R.drawable.person)
                .into(holder.academicianImage)
        }

        // Uzmanlık alanlarını chip gibi TextView’lerle göster
        holder.academicianContainer.removeAllViews() // Önce temizle
        for (category in academician.academicianExpertArea) {
            val chip = TextView(holder.itemView.context).apply {
                text = category
                setPadding(24, 12, 24, 12)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.parseColor("#6f99cb"))
                setTypeface(null, Typeface.BOLD)
                textSize = 12f
                isSingleLine = true
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(10, 0, 10, 0)
                }
            }
            holder.academicianContainer.addView(chip)
        }

        // Ekle butonuna tıklanınca dışarıdan gelen fonksiyonu çalıştır
        holder.btnAddAcademician.setOnClickListener {
            onItemClick(academician)
        }

        // Kartın tamamına tıklanırsa detay ekranına yönlendir
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AcademicianPreviewActivity::class.java).apply {
                putExtra("academicianEmail", academician.academicianEmail) // Email verisi gönderilir
            }
            context.startActivity(intent)
        }
    }

    // Liste eleman sayısı
    override fun getItemCount(): Int = filteredList.size

    // Arama sorgusuna göre filtreleme yap
    fun filter(query: String) {
        val lowerQuery = query.lowercase(Locale.getDefault())
        filteredList.clear()

        if (lowerQuery.isEmpty()) {
            // Boşsa tüm liste gösterilsin
            filteredList.addAll(fullList)
        } else {
            // İsim veya uzmanlık alanına göre filtrele
            filteredList.addAll(fullList.filter { academician ->
                academician.academicianName.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                        academician.academicianExpertArea.any { expert ->
                            expert.lowercase(Locale.getDefault()).contains(lowerQuery)
                        }
            })
        }

        // Liste güncellendi, görünümü yenile
        notifyDataSetChanged()
    }

    // Dışarıdan liste güncellemesi yapıldığında çağrılır (örneğin Firestore’dan yeni veri gelirse)
    fun setData(newList: List<Academician>) {
        fullList = newList.toList()
        filteredList.clear()
        filteredList.addAll(fullList)
        notifyDataSetChanged()
    }
}
