package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pilotlog.databinding.FragmentMapBinding
import com.example.pilotlog.network.RetrofitClient
import com.example.pilotlog.hardware.LocationHelper
import kotlinx.coroutines.launch
import com.google.gson.Gson

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationHelper: LocationHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
            } else {
                android.widget.Toast.makeText(context, "Location permission denied. Radar will simulate.", android.widget.Toast.LENGTH_LONG).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationHelper = LocationHelper(requireContext())

        checkPermissions()

        binding.webviewMap.settings.javaScriptEnabled = true
        binding.webviewMap.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                centerMapOnCurrentLocation()
            }
        }
        binding.webviewMap.loadUrl("file:///android_asset/map.html")

        binding.fabRefresh.setOnClickListener {
            refreshRadar()
        }
    }

    private fun checkPermissions() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun centerMapOnCurrentLocation() {
        locationHelper.getCurrentLocation { location ->
            if (location != null && _binding != null) {
                binding.webviewMap.evaluateJavascript("setCenter(${location.latitude}, ${location.longitude})", null)
                binding.webviewMap.evaluateJavascript("updateUserLocation(${location.latitude}, ${location.longitude})", null)
            }
        }
    }

    private fun refreshRadar() {
        android.widget.Toast.makeText(context, "Refreshing Radar...", android.widget.Toast.LENGTH_SHORT).show()
        locationHelper.getCurrentLocation { location ->
            if (location != null && _binding != null) {
                binding.webviewMap.evaluateJavascript("updateUserLocation(${location.latitude}, ${location.longitude})", null)
                fetchTraffic(location.latitude, location.longitude)
            } else {
                android.widget.Toast.makeText(context, "No GPS Signal!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchTraffic(lat: Double, lon: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.openSkyService.getFlights(
                    lamin = lat - 2.0,
                    lomin = lon - 2.0,
                    lamax = lat + 2.0,
                    lomax = lon + 2.0
                )

                val aircrafts: List<Map<String, Any>> = response.states?.map { state ->
                    mapOf(
                        "icao24" to (state[0] as? String ?: ""),
                        "callsign" to (state[1] as? String ?: "").trim(),
                        "lon" to (state[5] as? Double ?: 0.0),
                        "lat" to (state[6] as? Double ?: 0.0),
                        "alt" to (state[7] as? Double ?: 0.0),
                        "spd" to (state[9] as? Double ?: 0.0),
                        "heading" to (state[10] as? Double ?: 0.0)
                    )
                } ?: emptyList()

                if (_binding == null) return@launch

                if (aircrafts.isEmpty()) {
                    android.widget.Toast.makeText(context, "No Targets Found", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Radar Contact: ${aircrafts.size} Targets", android.widget.Toast.LENGTH_SHORT).show()
                }

                val json = Gson().toJson(aircrafts)
                binding.webviewMap.evaluateJavascript("updateAircrafts('$json')", null)

            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Radar Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

