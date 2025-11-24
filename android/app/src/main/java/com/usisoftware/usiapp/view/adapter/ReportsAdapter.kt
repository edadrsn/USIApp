package com.usisoftware.usiapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.view.model.Report

class ReportsAdapter(
    private val reports: MutableList<Report>,
    private val onItemClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.ReportsViewHolder>() {

    class ReportsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val complainingEmail= itemView.findViewById<TextView>(R.id.complainingEmail)
        val reportMessage=itemView.findViewById<TextView>(R.id.reportMessage)
        val requestId=itemView.findViewById<TextView>(R.id.requestId)
        val detailIcon: ImageView = itemView.findViewById(R.id.iconDetail)
    }

    // Yeni bir ViewHolder nesnesi oluştur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): ReportsAdapter.ReportsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_request, parent, false)
        return ReportsViewHolder(view)
    }

    // Her bir liste öğesi ekranda gösterileceğinde çağrılır
    override fun onBindViewHolder(holder: ReportsAdapter.ReportsViewHolder, position: Int) {

        val report = reports[position]

        holder.complainingEmail.text=report.user
        holder.reportMessage.text = report.message
        holder.requestId.text=report.requestId

        // Kartın tamamına tıklandığında ilgili fonksiyonu tetikle
        holder.itemView.setOnClickListener {
            onItemClick(report)
        }

    }


    // Liste elemanlarının toplam sayısını döndür
    override fun getItemCount(): Int = reports.size


}