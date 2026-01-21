package com.example.pilotlog.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pilotlog.R
import com.example.pilotlog.data.Flight
import com.example.pilotlog.data.FlightWithAircraft
import java.text.SimpleDateFormat
import java.util.Locale

class FlightAdapter(
    private val onItemClicked: (Flight) -> Unit,
    private val onItemLongClicked: ((Flight) -> Unit)? = null
) : ListAdapter<FlightWithAircraft, FlightAdapter.FlightViewHolder>(FlightComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flight, parent, false)
        return FlightViewHolder(view, onItemClicked, onItemLongClicked)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class FlightViewHolder(
        itemView: View,
        private val onItemClicked: (Flight) -> Unit,
        private val onItemLongClicked: ((Flight) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val dateView: TextView = itemView.findViewById(R.id.text_date)
        private val routeView: TextView = itemView.findViewById(R.id.text_route)
        private val durationView: TextView = itemView.findViewById(R.id.text_duration)
        private val remarksView: TextView = itemView.findViewById(R.id.text_remarks)
        private val aircraftView: TextView = itemView.findViewById(R.id.text_aircraft)
        private val imageView: android.widget.ImageView = itemView.findViewById(R.id.image_flight_thumb)
        private val cardView: View = itemView

        fun bind(item: FlightWithAircraft) {
            val flight = item.flight
            val aircraft = item.aircraft
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateView.text = dateFormat.format(flight.date)
            routeView.text = "${flight.departureCode} -> ${flight.arrivalCode}"
            durationView.text = "${flight.durationMinutes} min"
            remarksView.text = flight.remarks
            
            if (aircraft != null) {
                aircraftView.text = "${aircraft.registration} (${aircraft.model})"
            } else {
                aircraftView.text = "Unknown Aircraft"
            }
            
            if (!flight.photoPath.isNullOrEmpty()) {
                val photoFile = java.io.File(flight.photoPath)
                if (photoFile.exists()) {
                    imageView.setImageURI(android.net.Uri.fromFile(photoFile))
                } else {
                    setDefaultAircraftImage(aircraft, flight)
                }
            } else {
                setDefaultAircraftImage(aircraft, flight)
            }
            imageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            
            cardView.setOnClickListener {
                onItemClicked(flight)
            }
            
            cardView.setOnLongClickListener {
                onItemLongClicked?.invoke(flight)
                true
            }
        }

        private fun setDefaultAircraftImage(aircraft: com.example.pilotlog.data.Aircraft?, flight: Flight) {
            val modelLower = aircraft?.model?.lowercase(Locale.getDefault()) ?: ""
            val regLower = aircraft?.registration?.lowercase(Locale.getDefault()) ?: ""
            val remarksLower = flight.remarks?.lowercase(Locale.getDefault()) ?: ""
            
            if (regLower.contains("sp-3372") || regLower.contains("sp-3472") || modelLower.contains("junior") || remarksLower.contains("junior") || remarksLower.contains("solo")) {
                imageView.setImageResource(R.drawable.img_junior_real)
            } else {
                imageView.setImageResource(R.drawable.img_puchacz_real)
            }
        }
    }

    class FlightComparator : DiffUtil.ItemCallback<FlightWithAircraft>() {
        override fun areItemsTheSame(oldItem: FlightWithAircraft, newItem: FlightWithAircraft): Boolean {
            return oldItem.flight.id == newItem.flight.id
        }

        override fun areContentsTheSame(oldItem: FlightWithAircraft, newItem: FlightWithAircraft): Boolean {
            return oldItem == newItem
        }
    }
}

