package com.usisoftware.usiapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.view.model.Request

class OpenRequestsAdapter(
    private var requestList: List<Request>,
    private val onItemClick: (Request) -> Unit,
    private val onReportClick:(Request) -> Unit,
    private val onBlockClick:(Request) -> Unit
) : RecyclerView.Adapter<OpenRequestsAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.openRequestImage)
        val txtUserName: TextView = itemView.findViewById(R.id.openRequestTitle)
        val txtMessage: TextView = itemView.findViewById(R.id.openRequestMessage)
        val requestType:TextView=itemView.findViewById(R.id.openRequesterTypeTxt)
        val requestDate:TextView=itemView.findViewById(R.id.openRequestDate)
        val applyCount:TextView=itemView.findViewById(R.id.applyCount)
        val requestComplaint:ImageView=itemView.findViewById(R.id.requestComplaint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_open_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]

        if (!request.requesterImage.isNullOrEmpty()) {
            Picasso.get()
                .load(request.requesterImage)
                .placeholder(R.drawable.baseline_block_24)
                .error(R.drawable.baseline_block_24)
                .into(holder.imgProfile)
        } else {
            holder.imgProfile.setImageResource(R.drawable.baseline_block_24)
        }

        holder.txtUserName.text = request.requesterName
        holder.txtMessage.text = request.message
        holder.requestDate.text= request.date
        holder.applyCount.text=request.applyUserCount.toString()

        if (request.requesterType == "industry") {
            holder.requestType.text="Sanayi"
        }
        else if(request.requesterType=="academician"){
            holder.requestType.text="Akademisyen"
        }
        else{
            holder.requestType.text="Öğrenci"
        }


        // popup menu
        holder.requestComplaint.setOnClickListener { view ->
            val popup = PopupMenu(view.context, holder.requestComplaint)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.report -> {
                        onReportClick(request)
                        true
                    }
                    R.id.block -> {
                        onBlockClick(request)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }


        // Karta tıklandığında ilgili fonksiyonu tetikle
        holder.itemView.setOnClickListener {
            onItemClick(request)
        }
    }

    override fun getItemCount(): Int = requestList.size

}