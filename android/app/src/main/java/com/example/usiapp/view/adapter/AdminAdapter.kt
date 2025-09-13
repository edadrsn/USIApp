package com.example.usiapp.view.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.usiapp.R
import com.example.usiapp.view.model.Request
import com.squareup.picasso.Picasso

class AdminAdapter(
    private val requests: MutableList<Request>,                      // Listeye ait tüm istek verileri
    private val onItemClick: (Request) -> Unit                       // Kart öğesine tıklandığında çağır
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    // ViewHolder View öğelerine referans sağlar
    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title:TextView = itemView.findViewById(R.id.adminRequestTitle)
        val message:TextView = itemView.findViewById(R.id.adminRequestMessage)
        val requesterTypeTxt:TextView = itemView.findViewById(R.id.requesterTypeTxt)
        val date:TextView = itemView.findViewById(R.id.adminRequestDate)
        val image: ImageView = itemView.findViewById(R.id.requestImage2)
        val categoryContainer:LinearLayout = itemView.findViewById(R.id.adminCategoryContainer)
        val detailIcon:ImageView = itemView.findViewById(R.id.detailIcon)
    }

    //Yeni view holder nesnesi oluştur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_card, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val request = requests[position]

        // Temel metinleri ata
        holder.title.text = request.title
        holder.message.text = request.message
        holder.date.text = "Tarih:" + request.date

        //Resim
        if (!request.requesterImage.isNullOrEmpty()) {
            Picasso.get()
                .load(request.requesterImage)
                .placeholder(R.drawable.baseline_block_24)
                .error(R.drawable.baseline_block_24)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.baseline_block_24)
        }

        //Requester Type
        val bg = holder.requesterTypeTxt.background as GradientDrawable

        when (request.requesterType) {
            "academician" -> {
                holder.requesterTypeTxt.text = "Akademisyen"
                bg.setColor(Color.parseColor("#1A9AAF"))
            }

            "student" -> {
                holder.requesterTypeTxt.text = "Öğrenci"
                bg.setColor(Color.parseColor("#5BB35E"))
            }

            "industry" -> {
                holder.requesterTypeTxt.text = "Sanayi"
                bg.setColor(Color.parseColor("#F06E1B"))
            }

            else -> {
                holder.requesterTypeTxt.text = request.requesterType
                bg.setColor(Color.parseColor("#9E9E9E"))
            }
        }

        // Önceki kategorileri temizle
        holder.categoryContainer.removeAllViews()

        // Her kategori için dinamik chip oluştur
        if (request.requesterType == "industry") {
            for (category in request.selectedCategories) {
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
        } else {
            // Akademisyen & Öğrenci
            if (!request.requestCategory.isNullOrEmpty()) {
                val chip = TextView(holder.itemView.context).apply {
                    text = request.requestCategory
                    setPadding(24, 12, 24, 12)
                    setBackgroundResource(R.drawable.category_chip_bg)
                    setTextColor(Color.parseColor("#6f99cb"))
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

        // Karttaki icona tıklandığında ilgili fonksiyonu tetikle
        holder.detailIcon.setOnClickListener {
            onItemClick(request)
        }

        // Karta tıklandığında ilgili fonksiyonu tetikle
        holder.itemView.setOnClickListener {
            onItemClick(request)
        }
    }

    // Liste elemanlarının toplam sayısı döndürülür
    override fun getItemCount(): Int = requests.size


}