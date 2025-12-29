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
import com.usisoftware.usiapp.view.repository.loadImageWithCorrectRotation

class AdminAdapter(
    private val requests: MutableList<Request>,                      // Listeye ait tüm istek verileri
    private val onItemClick: (Request) -> Unit                       // Kart öğesine tıklandığında çağır
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    // ViewHolder View öğelerine referans sağlar
    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.adminRequestTitle)
        val message: TextView = itemView.findViewById(R.id.adminRequestMessage)
        val requesterTypeTxt: TextView = itemView.findViewById(R.id.requesterTypeTxt)
        val date: TextView = itemView.findViewById(R.id.adminRequestDate)
        val image: ImageView = itemView.findViewById(R.id.requestImage2)
        val categoryContainer: LinearLayout = itemView.findViewById(R.id.adminCategoryContainer)
        val detailIcon: ImageView = itemView.findViewById(R.id.detailIcon)
        val isOpenRequestAdmin = itemView.findViewById<TextView>(R.id.isOpenRequestText)
        val isOpenRequestImage = itemView.findViewById<ImageView>(R.id.isOpenRequestImage)
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
        holder.title.text = request.requesterName
        holder.message.text = request.message
        holder.date.text = "Tarih:" + request.date

        if (request.requestType == true) {
            holder.isOpenRequestImage.visibility = View.VISIBLE
            holder.isOpenRequestAdmin.visibility = View.VISIBLE
            holder.isOpenRequestAdmin.text = "Açık Talep"
        } else {
            holder.isOpenRequestImage.visibility = View.GONE
            holder.isOpenRequestAdmin.visibility = View.GONE
        }

        //Resim
        if (!request.requesterImage.isNullOrEmpty()) {
            try {
                loadImageWithCorrectRotation(
                    context = holder.itemView.context,
                    imageUrl = request.requesterImage,
                    imageView = holder.image,
                    placeholderRes = R.drawable.baseline_block_24
                )
            } catch (e: Exception) {
                holder.image.setImageResource(R.drawable.baseline_block_24)
            }

        } else {
            holder.image.setImageResource(R.drawable.baseline_block_24)
        }

        when (request.requesterType) {
            "academician" -> {
                holder.requesterTypeTxt.text = "Akademisyen"
            }

            "student" -> {
                holder.requesterTypeTxt.text = "Öğrenci"
            }

            "industry" -> {
                holder.requesterTypeTxt.text = "Sanayi/Girişimci"
            }

            else -> {
                holder.requesterTypeTxt.text = request.requesterType
            }
        }

        // Önceki kategorileri temizle
        holder.categoryContainer.removeAllViews()

        // Her kategori için dinamik chip oluştur
        if (request.requesterType == "industry") {
            request.selectedCategories?.forEach { category ->
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
            if (!request.requestCategory.isNullOrEmpty()) {
                val chip = TextView(holder.itemView.context).apply {
                    text = request.requestCategory
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