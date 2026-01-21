package com.example.pilotlog.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pilotlog.R
import com.example.pilotlog.data.Aircraft

class AircraftAdapter(
    private val onItemClicked: (Aircraft) -> Unit,
    private val onItemLongClicked: ((Aircraft) -> Unit)? = null
) : ListAdapter<Aircraft, AircraftAdapter.AircraftViewHolder>(AircraftDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AircraftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aircraft, parent, false)
        return AircraftViewHolder(view, onItemClicked, onItemLongClicked)
    }

    override fun onBindViewHolder(holder: AircraftViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AircraftViewHolder(
        itemView: View,
        val onItemClicked: (Aircraft) -> Unit,
        val onItemLongClicked: ((Aircraft) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val textRegistration: TextView = itemView.findViewById(R.id.text_registration)
        private val textModel: TextView = itemView.findViewById(R.id.text_model)
        private val textType: TextView = itemView.findViewById(R.id.text_type)
        private val imageAircraft: android.widget.ImageView = itemView.findViewById(R.id.image_aircraft)

        fun bind(aircraft: Aircraft) {
            textRegistration.text = aircraft.registration
            textModel.text = aircraft.model
            textType.text = aircraft.type
            
            if (!aircraft.imagePath.isNullOrEmpty()) {
                val photoFile = java.io.File(aircraft.imagePath)
                if (photoFile.exists()) {
                    imageAircraft.setImageURI(android.net.Uri.fromFile(photoFile))
                } else {
                    setDefaultAircraftImage(aircraft)
                }
            } else {
                setDefaultAircraftImage(aircraft)
            }

            itemView.setOnClickListener {
                onItemClicked(aircraft)
            }

            itemView.setOnLongClickListener {
                onItemLongClicked?.invoke(aircraft)
                true
            }
        }

        private fun setDefaultAircraftImage(aircraft: Aircraft) {
            val reg = aircraft.registration.uppercase()
            val model = aircraft.model.lowercase()
            
            val imageRes = when {
                reg.contains("SP-3472") || reg.contains("SP-3372") || model.contains("junior") -> R.drawable.img_junior_real
                else -> R.drawable.img_puchacz_real
            }
            imageAircraft.setImageResource(imageRes)
        }
    }

    class AircraftDiffCallback : DiffUtil.ItemCallback<Aircraft>() {
        override fun areItemsTheSame(oldItem: Aircraft, newItem: Aircraft): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Aircraft, newItem: Aircraft): Boolean {
            return oldItem == newItem
        }
    }
}

