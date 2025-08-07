package com.example.usiapp.view.adapter

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
import com.example.usiapp.view.model.Request

class OldRequestAdapter(
    private val oldRequests: MutableList<Request>,
    private val onItemClick: (Request) -> Unit
) : RecyclerView.Adapter<OldRequestAdapter.OldRequestViewHolder>() {

    // ViewHolder, liste öğesindeki view bileşenlerine referans tutar
    class OldRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleRequest)
        val message: TextView = itemView.findViewById(R.id.messageRequest)
        val date: TextView = itemView.findViewById(R.id.dateRequest)
        val categoryContainer: LinearLayout = itemView.findViewById(R.id.containerCategory)
        val detailIcon: ImageView = itemView.findViewById(R.id.iconDetail)
    }

    // Yeni ViewHolder oluşturulur, layout inflate edilir
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OldRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return OldRequestViewHolder(view)
    }

    // Her bir liste öğesi için veri bağlama işlemi yapılır
    override fun onBindViewHolder(holder: OldRequestViewHolder, position: Int) {
        val oldRequest = oldRequests[position]

        holder.title.text = oldRequest.title
        holder.message.text = oldRequest.message
        holder.date.text = oldRequest.date

        // Önceki kategorileri temizle
        holder.categoryContainer.removeAllViews()

        // Her kategori için dinamik chip oluştur
        for (category in oldRequest.selectedCategories) {
            val chip = TextView(holder.itemView.context).apply {
                text = category
                setPadding(20, 10, 20, 10)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.parseColor("#6f99cb"))
                setTypeface(null, Typeface.BOLD)
                textSize = 11f
                isSingleLine = true
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(10, 0, 10, 0)
                }
            }
            holder.categoryContainer.addView(chip)
        }

        // Detay ikonuna tıklanınca
        holder.detailIcon.setOnClickListener {
            onItemClick(oldRequest)
        }

        // Kartın tamamına tıklanınca
        holder.itemView.setOnClickListener {
            onItemClick(oldRequest)
        }
    }

    // Liste uzunluğunu döner (kaç tane eski istek varsa)
    override fun getItemCount(): Int = oldRequests.size
}
