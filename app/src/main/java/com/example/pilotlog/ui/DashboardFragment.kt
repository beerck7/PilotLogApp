package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pilotlog.databinding.FragmentDashboardBinding
import com.example.pilotlog.hardware.LocationHelper
import com.example.pilotlog.R

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val flightViewModel: FlightViewModel by viewModels()
    private lateinit var locationHelper: LocationHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireContext())

        val viewsToAnimate = listOf(
            binding.textHeader,
            binding.cardRank,
            binding.layoutStats,
            binding.cardDashboardWeather,
            binding.cardGforceLauncher,
            binding.cardChecklistLauncher,
            binding.cardHorizonLauncher,
            binding.textRecent,
            binding.recyclerViewRecent
        )

        viewsToAnimate.forEachIndexed { index, v ->
            v.alpha = 0f
            v.translationY = 50f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 100L)
                .setDuration(500)
                .start()
        }

        val adapter = FlightAdapter(
            onItemClicked = { flight ->
            }
        )
        binding.recyclerViewRecent.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewRecent.adapter = adapter

        flightViewModel.allFlights.observe(viewLifecycleOwner) { flights ->
            adapter.submitList(flights.take(3))
        }

        flightViewModel.totalFlightTime.observe(viewLifecycleOwner) { minutes ->
            val totalMinutes = minutes ?: 0
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            binding.textTotalHours.text = getString(R.string.format_hours_minutes, hours, mins)

            val (rankResId, nextRankResId, targetHours) = when {
                hours < 10 -> Triple(R.string.rank_cadet, R.string.rank_officer, 10)
                hours < 50 -> Triple(R.string.rank_officer, R.string.rank_captain, 50)
                hours < 200 -> Triple(R.string.rank_captain, R.string.rank_ace, 200)
                hours < 500 -> Triple(R.string.rank_ace, R.string.rank_legend, 500)
                else -> Triple(R.string.rank_legend, R.string.rank_max, 1000)
            }

            binding.textCurrentRank.text = getString(rankResId)
            binding.textNextRank.text = getString(R.string.label_next_rank, getString(nextRankResId))
            
            if (rankResId == R.string.rank_legend) {
                binding.progressRank.max = 100
                binding.progressRank.progress = 100
                binding.textRankProgress.text = getString(R.string.rank_max_level)
            } else {
                val prevThreshold = when(rankResId) {
                    R.string.rank_cadet -> 0
                    R.string.rank_officer -> 10
                    R.string.rank_captain -> 50
                    R.string.rank_ace -> 200
                    else -> 0
                }
                
                val progress = ((hours - prevThreshold).toFloat() / (targetHours - prevThreshold) * 100).toInt()
                binding.progressRank.max = 100
                binding.progressRank.progress = progress.coerceIn(0, 100)
                binding.textRankProgress.text = getString(R.string.rank_progress_format, hours, targetHours)
            }
        }

        flightViewModel.totalFlights.observe(viewLifecycleOwner) { count ->
            binding.textTotalFlights.text = count.toString()
        }

        flightViewModel.weatherMetar.observe(viewLifecycleOwner) { metar ->
            binding.textDashboardMetar.text = metar ?: "No Data"
        }
        fetchWeatherForNearestAirport()
    }

    private fun fetchWeatherForNearestAirport() {
        locationHelper.getCurrentLocation { location ->
            if (location != null) {
                val airports = flightViewModel.allAirports.value
                if (!airports.isNullOrEmpty()) {
                    val nearest = airports.minByOrNull { airport ->
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            location.latitude, location.longitude,
                            airport.latitude, airport.longitude, results
                        )
                        results[0]
                    }
                    nearest?.let {
                        flightViewModel.fetchWeather(it.code, com.example.pilotlog.BuildConfig.WEATHER_API_KEY)
                    }
                } else {
                    flightViewModel.fetchWeather("EPWA", com.example.pilotlog.BuildConfig.WEATHER_API_KEY)
                }
            } else {
                flightViewModel.fetchWeather("EPWA", com.example.pilotlog.BuildConfig.WEATHER_API_KEY)
            }
        }

        binding.cardGforceLauncher.setOnClickListener {
            findNavController().navigate(R.id.gForceFragment)
        }

        binding.cardChecklistLauncher.setOnClickListener {
            findNavController().navigate(R.id.checklistFragment)
        }

        binding.cardHorizonLauncher.setOnClickListener {
            findNavController().navigate(R.id.horizonFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
