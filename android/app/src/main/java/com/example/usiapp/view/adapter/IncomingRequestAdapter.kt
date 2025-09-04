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
        val image:ImageView=itemView.findViewById(R.id.requestImage2)
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

        holder.title.text = incomingRequest.title           // Başlık set edilir
        holder.message.text = incomingRequest.message       // Mesaj set edilir
        holder.date.text = incomingRequest.date             // Tarih set edilir

        if (!incomingRequest.requesterImage.isNullOrEmpty()) {
            Picasso.get()
                .load(incomingRequest.requesterImage)
                .placeholder(R.drawable.icon_company)
                .error(R.drawable.icon_company)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.icon_company)
        }

        // Önce kategori container'ı temizlenir, önceki chip'ler kaldırılır
        holder.categoryContainer.removeAllViews()

        // Her kategori için dinamik chip (TextView) oluştur ve container'a ekle
        for (request in incomingRequest.selectedCategories) {
            val chip = TextView(holder.itemView.context).apply {
                text = request
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
            // Chipi container'a ekle
            holder.categoryContainer.addView(chip)
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
