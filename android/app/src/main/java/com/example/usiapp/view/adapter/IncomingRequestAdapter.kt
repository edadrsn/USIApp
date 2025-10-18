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
import com.squareup.picasso.Picasso

class IncomingRequestAdapter(
    private val incomingRequests: MutableList<Request>,      // Gelen isteklerin listesi
    private val onItemClick: (Request) -> Unit               // Öğeye tıklanınca çağrılacak fonksiyon
) : RecyclerView.Adapter<IncomingRequestAdapter.IncomingRequestViewHolder>() {

    // ViewHolder, liste öğesindeki view bileşenlerine referans tutar
    class IncomingRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleRequest)
        val message: TextView = itemView.findViewById(R.id.messageRequest)
        val date: TextView = itemView.findViewById(R.id.dateRequest)
        val requesterTypeTxt2=itemView.findViewById<TextView>(R.id.requesterTypeTxt2)
        val image: ImageView = itemView.findViewById(R.id.requestImage2)
        val categoryContainer: LinearLayout = itemView.findViewById(R.id.containerCategory)
        val detailIcon: ImageView = itemView.findViewById(R.id.iconDetail)
    }

    // Yeni ViewHolder oluşturulur, layout inflate edilir
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return IncomingRequestViewHolder(view)
    }

    // Her bir liste öğesi için veri bağlama işlemi yapılır
    override fun onBindViewHolder(holder: IncomingRequestViewHolder, position: Int) {
        val incomingRequest = incomingRequests[position]    // Pozisyona karşılık gelen istek

        holder.title.text = incomingRequest.requesterName   // Başlık set edilir
        holder.message.text = incomingRequest.message       // Mesaj set edilir
        holder.date.text = "Tarih: " + incomingRequest.date // Tarih set edilir

        if (!incomingRequest.requesterImage.isNullOrEmpty()) {
            Picasso.get()
                .load(incomingRequest.requesterImage)
                .placeholder(R.drawable.baseline_block_24)
                .error(R.drawable.baseline_block_24)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.baseline_block_24)
        }


        when (incomingRequest.requesterType) {
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
                holder.requesterTypeTxt2.text = incomingRequest.requesterType
            }
        }

        // Önce kategori container'ı temizlenir, önceki chip'ler kaldırılır
        holder.categoryContainer.removeAllViews()

        // Her kategori için dinamik chip (TextView) oluştur ve container'a ekle
        if (incomingRequest.requesterType == "industry") {
            for (category in incomingRequest.selectedCategories) {
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
            if (!incomingRequest.requestCategory.isNullOrEmpty()) {
                val chip = TextView(holder.itemView.context).apply {
                    text = incomingRequest.requestCategory
                    setPadding(22, 10, 22, 10)
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


        // Detay ikonuna tıklanınca onItemClick fonksiyonunu çağır
        holder.detailIcon.setOnClickListener {
            onItemClick(incomingRequest)
        }

        // Kartın tamamına tıklanınca onItemClick fonksiyonunu çağır
        holder.itemView.setOnClickListener {
            onItemClick(incomingRequest)
        }
    }

    // Liste uzunluğunu döner (kaç istek varsa)
    override fun getItemCount(): Int = incomingRequests.size
}
