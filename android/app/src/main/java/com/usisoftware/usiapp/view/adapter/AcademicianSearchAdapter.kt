package com.usisoftware.usiapp.view.adapter

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
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.view.academicianView.AcademicianPreviewActivity
import com.usisoftware.usiapp.view.model.Academician
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation
import java.util.Locale

class AcademicianSearchAdapter(
    private val academicianList: List<Academician>,   //akademisyen listesini al
    private val onItemClick: (Academician) -> Unit    //Öğeye tıklanınca yapılacak işlem
) : RecyclerView.Adapter<AcademicianSearchAdapter.AcademicianSearchViewHolder>() {

    private var fullList = academicianList.toList()           // Orijinal tam listeyi tutar
    private var filteredList =
        academicianList.toMutableList()// Filtrelenmiş listeyi tutar (arama için)

    // ViewHolder sınıfı, RecyclerView'da her bir öğenin görünümünü tutar
    inner class AcademicianSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val academicianImage: ImageView = itemView.findViewById(R.id.academicianImage)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val btnAddAcademician: ImageView = itemView.findViewById(R.id.btnAddAcademician)
        val academicianContainer: LinearLayout = itemView.findViewById(R.id.academicianContainer)
    }

    // ViewHolder oluşturulduğunda çağrılır, layout burada inflate edilir
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicianSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_academician_search_card, parent, false)
        return AcademicianSearchViewHolder(view)
    }

    // ViewHolder içeriği burada set edilir, pozisyona göre veri bağlanır
    override fun onBindViewHolder(holder: AcademicianSearchViewHolder, position: Int) {
        val academician = filteredList[position]  // Gösterilecek akademisyen

        holder.tvName.text = academician.academicianName      // İsmi set et
        holder.tvTitle.text = academician.academicianDegree   // Ünvanı set et


        // Akademisyenin resim URL'si boş veya null değilse Picasso ile yükle
        val imageUrl = academician.academicianImageUrl

        if (!imageUrl.isNullOrEmpty()) {
            loadImageWithCorrectRotation(
                context = holder.itemView.context,   // Adapter içindeyiz
                imageUrl = imageUrl,
                imageView = holder.academicianImage,
                placeholderRes = R.drawable.person
            )
        } else {
            // Resim yoksa varsayılan kişi ikonunu göster
            holder.academicianImage.setImageResource(R.drawable.person)
        }


        // Uzmanlık alanı chip'lerini temizle ve yeniden oluştur
        holder.academicianContainer.removeAllViews()
        for (category in academician.academicianExpertArea) {
            val chip = TextView(holder.itemView.context).apply {
                text = category
                setPadding(22, 10, 22, 10)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.parseColor("#000000"))
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

        // Ekle butonuna tıklanınca onItemClick fonksiyonunu çağır
        holder.btnAddAcademician.setOnClickListener {
            onItemClick(academician)
        }

        // Öğenin tamamına tıklanınca AcademicianPreviewActivity'yi aç, akademisyen id'sini gönder
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AcademicianPreviewActivity::class.java).apply {
                putExtra("source", "appoint")
                putExtra("academicianId", academician.documentId)
            }
            context.startActivity(intent)
        }

    }

    // Liste kaç elemanlı onu döner (filtrelenmiş liste)
    override fun getItemCount(): Int = filteredList.size

    // Arama için filtre fonksiyonu
    fun filter(query: String) {
        val lowerQuery = query.lowercase(Locale.getDefault())
        filteredList.clear()

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(fullList)
        } else {
            filteredList.addAll(fullList.filter { academician ->
                // İsim veya uzmanlık alanında arama terimi geçiyor mu kontrol et
                academician.academicianName.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                        academician.academicianExpertArea.any { expert ->
                            expert.lowercase(Locale.getDefault()).contains(lowerQuery)
                        }
            })
        }

        notifyDataSetChanged()   // Liste değişti, adapter'ı güncelle
    }

    // Adapter'ın listesini dışarıdan güncellemek için kullanılır
    fun setData(newList: List<Academician>) {
        fullList = newList.toList()     // Tam listeyi güncelle
        filteredList.clear()
        filteredList.addAll(fullList)   // Filtrelenmiş listeyi de güncelle
        notifyDataSetChanged()
    }
}
