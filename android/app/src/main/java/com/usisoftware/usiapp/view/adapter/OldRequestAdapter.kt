package com.usisoftware.usiapp.view.adapter

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
import com.usisoftware.usiapp.view.model.Request
import com.squareup.picasso.Picasso

class OldRequestAdapter(
    private val oldRequests: MutableList<Request>,
    private val onItemClick: (Request) -> Unit
) : RecyclerView.Adapter<OldRequestAdapter.OldRequestViewHolder>() {

    // ViewHolder, liste öğesindeki view bileşenlerine referans tutar
    class OldRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleRequest)
        val message: TextView = itemView.findViewById(R.id.messageRequest)
        val date: TextView = itemView.findViewById(R.id.dateRequest)
        val requesterTypeTxt2 = itemView.findViewById<TextView>(R.id.requesterTypeTxt2)
        val image: ImageView = itemView.findViewById(R.id.requestImage2)
        val categoryContainer: LinearLayout = itemView.findViewById(R.id.containerCategory)
        val detailIcon: ImageView = itemView.findViewById(R.id.iconDetail)
        val isOpenRequestTextOld:TextView=itemView.findViewById(R.id.isOpenRequestText)
        val isOpenRequestImage:ImageView=itemView.findViewById(R.id.isOpenRequestImage)
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

        holder.title.text = oldRequest.requesterName
        holder.message.text = oldRequest.message
        holder.date.text = "Tarih: " + oldRequest.date

        val openReq=oldRequest.requestType
        println(openReq)
        if(openReq == true){
            holder.isOpenRequestImage.visibility=View.VISIBLE
            holder.isOpenRequestTextOld.visibility=View.VISIBLE
            holder.isOpenRequestTextOld.text="Açık Talep"
        }else{
            holder.isOpenRequestTextOld.visibility=View.GONE
        }


        if (!oldRequest.requesterImage.isNullOrEmpty()) {
            Picasso.get()
                .load(oldRequest.requesterImage)
                .placeholder(R.drawable.baseline_block_24)
                .error(R.drawable.baseline_block_24)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.baseline_block_24)
        }


        when (oldRequest.requesterType) {
            "academician" -> {
                holder.requesterTypeTxt2.text = "Akademisyen"
            }

            "student" -> {
                holder.requesterTypeTxt2.text = "Öğrenci"
            }

            "industry" -> {
                holder.requesterTypeTxt2.text = "Sanayi"
            }

            else -> {
                holder.requesterTypeTxt2.text = oldRequest.requesterType
            }
        }

        // Önceki kategorileri temizle
        holder.categoryContainer.removeAllViews()

        // Her kategori için dinamik chip oluştur
        if (oldRequest.requesterType == "industry") {
            for (category in oldRequest.selectedCategories) {
                val chip = TextView(holder.itemView.context).apply {
                    text = category
                    setPadding(20, 10, 20, 10)
                    setBackgroundResource(R.drawable.category_chip_bg)
                    setTextColor(Color.parseColor("#000000"))
                    setTypeface(null, Typeface.BOLD)
                    textSize = 11f
                    isSingleLine = true
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(5, 0, 10, 0)
                    }
                }
                holder.categoryContainer.addView(chip)
            }
        } else {
            // Akademisyen & Öğrenci
            if (!oldRequest.requestCategory.isNullOrEmpty()) {
                val chip = TextView(holder.itemView.context).apply {
                    text = oldRequest.requestCategory
                    setPadding(24, 12, 24, 12)
                    setBackgroundResource(R.drawable.category_chip_bg)
                    setTextColor(Color.parseColor("#000000"))
                    setTypeface(null, Typeface.BOLD)
                    textSize = 11f
                    isSingleLine = true
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(5, 0, 10, 0)
                    }
                }
                holder.categoryContainer.addView(chip)
            }
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
