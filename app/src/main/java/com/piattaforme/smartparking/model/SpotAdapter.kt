package com.piattaforme.smartparking.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.piattaforme.smartparking.R

class SpotAdapter : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {
    private var history = emptyList<Spots>()


    override fun getItemCount() : Int{
        return history.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parking, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val currentSpot = history[position]

        holder.tvNota.text = currentSpot.note

        holder.tvCoordinate.text = "Lat: ${currentSpot.latitude}, Lon : ${currentSpot.longitude}"
    }

    fun setData(newList: List<Spots>) {
        this.history = newList
        notifyDataSetChanged()
    }

    class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNota: TextView = itemView.findViewById(R.id.tv_parking_note)
        val tvCoordinate: TextView = itemView.findViewById(R.id.tv_parking_coords)
    }
}