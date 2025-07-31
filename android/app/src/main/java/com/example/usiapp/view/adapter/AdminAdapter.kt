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

class AdminAdapter(
    private val requests: MutableList<Request>,                      // Listeye ait tüm istek verileri
    private val onItemClick: (Request) -> Unit                       // Kart öğesine tıklandığında çağır
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    // ViewHolder View öğelerine referans sağlar
    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.adminRequestTitle)
        val message = itemView.findViewById<TextView>(R.id.adminRequestMessage)
        val date = itemView.findViewById<TextView>(R.id.adminRequestDate)
        val categoryContainer = itemView.findViewById<LinearLayout>(R.id.adminCategoryContainer)
        val detailIcon = itemView.findViewById<ImageView>(R.id.detailIcon)

    }

    //Yeni view holder nesnesi oluştur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_admin_card,
                parent,
                false
            )
        return com.example.usiapp.view.adapter.AdminAdapter.AdminViewHolder(view)
    }


    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val request = requests[position]

        // Temel metinleri ata
        holder.title.text = request.title
        holder.message.text = request.message
        holder.date.text = request.date

        // Önceki kategorileri temizle
        holder.categoryContainer.removeAllViews()

        // Her bir kategori için dinamik olarak bir "chip" oluştur ve container'a ekle
        for (category in request.selectedCategories) {
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
            holder.categoryContainer.addView(chip)
        }

        // Karttaki icona tıklandığında ilgili fonksiyonu tetikle
        holder.detailIcon.setOnClickListener {
            onItemClick(request)
        }

        // Karta tıklandığında ilgili fonksiyonu tetikle
        holder.itemView.setOnClickListener{
            onItemClick(request)
        }

    }


    // Liste elemanlarının toplam sayısı döndürülür
    override fun getItemCount(): Int = requests.size


}