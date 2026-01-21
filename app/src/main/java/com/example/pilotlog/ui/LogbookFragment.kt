package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pilotlog.R
import com.example.pilotlog.databinding.FragmentLogbookBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LogbookFragment : Fragment() {

    private var _binding: FragmentLogbookBinding? = null
    private val binding get() = _binding!!

    private val flightViewModel: FlightViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogbookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FlightAdapter(
            onItemClicked = { flight ->
                val bundle = Bundle().apply {
                    putInt("flightId", flight.id)
                }
                findNavController().navigate(R.id.action_logbook_to_detail, bundle)
            },
            onItemLongClicked = { flight ->
                showDeleteFlightDialog(flight)
            }
        )
        binding.recyclerViewFlights.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFlights.adapter = adapter

        flightViewModel.allFlights.observe(viewLifecycleOwner) { flights ->
            adapter.submitList(flights)
        }

        binding.fabAddFlight.setOnClickListener {
            findNavController().navigate(R.id.action_logbook_to_detail)
        }
    }

    private fun showDeleteFlightDialog(flight: com.example.pilotlog.data.Flight) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_flight_message, flight.departureCode, flight.arrivalCode))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                flightViewModel.deleteFlight(flight)
                android.widget.Toast.makeText(context, R.string.toast_flight_deleted, android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

