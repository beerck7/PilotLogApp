package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pilotlog.databinding.FragmentWeatherBinding
import com.example.pilotlog.network.RetrofitClient
import com.example.pilotlog.R
import kotlinx.coroutines.launch

class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGetWeather.setOnClickListener {
            val code = binding.editAirportCode.text.toString().trim()
            val apiKey = binding.editApiKey.text.toString().trim()
            
            if (code.isEmpty()) {
                Toast.makeText(context, "Airport Code Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (apiKey.isEmpty()) {
                Toast.makeText(context, "API Key Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchWeather(code, apiKey)
        }

        if (com.example.pilotlog.BuildConfig.WEATHER_API_KEY.isNotEmpty()) {
            binding.editApiKey.setText(com.example.pilotlog.BuildConfig.WEATHER_API_KEY)
        }
    }

    private fun fetchWeather(code: String, apiKey: String) {
        binding.textMetar.text = "Loading..."
        binding.textTaf.text = "Loading..."
        binding.textMetar.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black))
        binding.textTaf.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black))

        lifecycleScope.launch {
            try {
                val metarResponse = RetrofitClient.instance.getMetar(code, apiKey)
                if (metarResponse.results > 0 && metarResponse.data.isNotEmpty()) {
                    binding.textMetar.text = metarResponse.data[0]
                } else {
                    binding.textMetar.text = "No METAR found"
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    binding.textMetar.text = "Error: Invalid API Key"
                    binding.textMetar.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red_error))
                } else {
                    binding.textMetar.text = "Error: ${e.message()}"
                }
            } catch (e: Exception) {
                binding.textMetar.text = "Error: ${e.message}"
            }

            try {
                val tafResponse = RetrofitClient.instance.getTaf(code, apiKey)
                if (tafResponse.results > 0 && tafResponse.data.isNotEmpty()) {
                    binding.textTaf.text = tafResponse.data[0]
                } else {
                    binding.textTaf.text = "No TAF found"
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    binding.textTaf.text = "Error: Invalid API Key"
                    binding.textTaf.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red_error))
                } else {
                    binding.textTaf.text = "Error: ${e.message()}"
                }
            } catch (e: Exception) {
                binding.textTaf.text = "Error: ${e.message}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
