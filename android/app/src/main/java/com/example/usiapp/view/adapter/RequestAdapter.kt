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

// RecyclerView.Adapter sınıfından türeyen RequestAdapter, kullanıcıdan gelen istekleri listelemek için kullanılır.
class RequestAdapter(
    private val requests: MutableList<Request>,                      // Listeye ait tüm istek verileri
    private val onDeleteClick: (Request) -> Unit,                    // Silme ikonu tıklandığında çağır
    private val onItemClick: (Request) -> Unit                       // Kart öğesine tıklandığında çağır
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    // ViewHolder, liste öğesindeki view bileşenlerine referans tutar
    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.requestTitle)
        val message = itemView.findViewById<TextView>(R.id.requestMessage)
        val date = itemView.findViewById<TextView>(R.id.requestDate)
        val categoryContainer = itemView.findViewById<LinearLayout>(R.id.categoryContainer)
        val image=itemView.findViewById<ImageView>(R.id.requestImage)
        val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)
        // ViewHolder View öğelerine referans sağlar
    }

    // Yeni bir ViewHolder nesnesi oluştur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_request_card,
                parent,
                false
            )
        return RequestViewHolder(view)
    }


    // Her bir liste öğesi ekranda gösterileceğinde çağrılır
    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {

        val request = requests[position]

        holder.title.text = request.title
        holder.message.text = request.message
        holder.date.text = request.date

        if (!request.requesterImage.isNullOrEmpty()) {
            Picasso.get()
                .load(request.requesterImage)
                .placeholder(R.drawable.icon_company)
                .error(R.drawable.icon_company)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.icon_company)
        }


        // Önceki kategorileri temizle
        holder.categoryContainer.removeAllViews()

        // Her bir kategori için dinamik olarak bir "chip" oluştur ve container'a ekle
        for (category in request.selectedCategories) {
            val chip = TextView(holder.itemView.context).apply {
                text = category
                setPadding(22, 10, 22, 10)
                setBackgroundResource(R.drawable.category_chip_bg)
                setTextColor(Color.parseColor("#6f99cb"))
                setTypeface(null, Typeface.BOLD)
                textSize = 11f
                isSingleLine = true
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(12, 0, 12, 0)
                }
            }
            holder.categoryContainer.addView(chip)
        }

        // Kartın tamamına tıklandığında ilgili fonksiyonu tetikle
        holder.itemView.setOnClickListener {
            onItemClick(request)
        }

        // Silme ikonuna tıklandığında ilgili fonksiyonu tetikle
        holder.deleteIcon.setOnClickListener {
            onDeleteClick(request)
        }
    }

    // Liste elemanlarının toplam sayısını döndür
    override fun getItemCount(): Int = requests.size
}
