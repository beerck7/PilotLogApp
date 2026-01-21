package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.content.ContextCompat
import com.example.pilotlog.databinding.FragmentProfileBinding
import com.example.pilotlog.R
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val flightViewModel: FlightViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listOf(binding.chartDna, binding.gridAchievements, binding.layoutWallet).forEachIndexed { index, v ->
            v.alpha = 0f
            v.translationY = 40f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 120L)
                .setDuration(400)
                .start()
        }

        var cachedMinutes: Int = 0
        var cachedFlights: Int = 0

        flightViewModel.totalFlightTime.observe(viewLifecycleOwner) { totalMinutes ->
            cachedMinutes = totalMinutes ?: 0
            updateDnaChart(cachedMinutes, cachedFlights)
        }

        flightViewModel.totalFlights.observe(viewLifecycleOwner) { totalFlights ->
            cachedFlights = totalFlights
            updateDnaChart(cachedMinutes, cachedFlights)
        }
    }

    private fun updateDnaChart(totalMinutes: Int, totalFlights: Int) {
        val totalHours = totalMinutes / 60f
        val avgDuration = if (totalFlights > 0) totalMinutes.toFloat() / totalFlights else 0f
        
        val endurance = (avgDuration / 120f).coerceIn(0.1f, 1.0f)
        val experience = (totalHours / 100f).coerceIn(0.1f, 1.0f)
        val versatility = 0.6f
        val precision = (totalFlights / 50f).coerceIn(0.1f, 1.0f)
        val nightOps = 0.3f

        binding.chartDna.setData(
            listOf(endurance, experience, versatility, precision, nightOps),
            listOf("Endurance", "Experience", "Versatility", "Precision", "Night Ops")
        )
        
        updateAchievements(totalHours, totalFlights)
        updateWallet()
    }

    private fun updateAchievements(totalHours: Float, totalFlights: Int) {
        binding.gridAchievements.removeAllViews()
        
        val achievements = listOf(
            Triple("Frequent Flyer", totalFlights >= 50, android.R.drawable.ic_menu_rotate),
            Triple("Marathon Man", totalHours >= 100, android.R.drawable.ic_menu_recent_history),
            Triple("Night Stalker", false, android.R.drawable.ic_menu_view),
            Triple("Glider Ace", true, android.R.drawable.ic_menu_compass),
            Triple("Globetrotter", false, android.R.drawable.ic_menu_mapmode),
            Triple("Instructor", false, android.R.drawable.ic_menu_info_details)
        )

        val unlockedColor = ContextCompat.getColor(requireContext(), R.color.achievement_unlocked)
        val lockedColor = ContextCompat.getColor(requireContext(), R.color.achievement_locked)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)

        for ((name, unlocked, iconRes) in achievements) {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_achievement, binding.gridAchievements, false)
            val icon = view.findViewById<android.widget.ImageView>(R.id.image_medal)
            val text = view.findViewById<android.widget.TextView>(R.id.text_medal_name)
            
            icon.setImageResource(iconRes)
            text.text = name
            
            if (unlocked) {
                icon.setColorFilter(unlockedColor)
                text.setTextColor(whiteColor)
                view.alpha = 1.0f
            } else {
                icon.setColorFilter(lockedColor)
                text.setTextColor(lockedColor)
                view.alpha = 0.5f
            }
            
            binding.gridAchievements.addView(view)
        }
    }

    private fun updateWallet() {
        binding.layoutWallet.removeAllViews()
        
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green_success)
        val yellowColor = ContextCompat.getColor(requireContext(), R.color.yellow_warning)
        val goldColor = ContextCompat.getColor(requireContext(), R.color.gold_accent)
        val accentColor = ContextCompat.getColor(requireContext(), R.color.card_stroke_accent)
        val cardBgColor = ContextCompat.getColor(requireContext(), R.color.card_bg_transparent)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        
        val licenses = listOf(
            Triple("PPL(A)", getString(R.string.label_in_training), goldColor),
            Triple("SPL (Glider)", getString(R.string.label_in_training), goldColor),
            Triple("Medical Cl.2", getString(R.string.label_valid), greenColor)
        )

        val density = resources.displayMetrics.density
        val cardWidth = (100 * density).toInt()
        val cardHeight = (130 * density).toInt()
        val margin = (12 * density).toInt()
        val padding = (12 * density).toInt()
        val iconSize = (28 * density).toInt()

        for ((name, status, color) in licenses) {
             val card = com.google.android.material.card.MaterialCardView(requireContext())
             val params = android.widget.LinearLayout.LayoutParams(cardWidth, cardHeight)
             params.marginEnd = margin
             card.layoutParams = params
             card.radius = 16f * density
             card.setCardBackgroundColor(cardBgColor)
             card.strokeColor = accentColor
             card.strokeWidth = (1.5f * density).toInt()
             
             val layout = android.widget.LinearLayout(requireContext())
             layout.orientation = android.widget.LinearLayout.VERTICAL
             layout.gravity = android.view.Gravity.CENTER
             layout.setPadding(padding, padding, padding, padding)
             
             val icon = android.widget.ImageView(requireContext())
             icon.setImageResource(android.R.drawable.ic_menu_my_calendar)
             icon.setColorFilter(accentColor)
             icon.layoutParams = android.widget.LinearLayout.LayoutParams(iconSize, iconSize)
             
             val title = android.widget.TextView(requireContext())
             title.text = name
             title.setTextColor(whiteColor)
             title.textSize = 13f
             title.typeface = android.graphics.Typeface.DEFAULT_BOLD
             title.gravity = android.view.Gravity.CENTER
             title.layoutParams = android.widget.LinearLayout.LayoutParams(
                 android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                 android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
             ).apply { topMargin = (8 * density).toInt() }

             val statusText = android.widget.TextView(requireContext())
             statusText.text = status
             statusText.setTextColor(color)
             statusText.textSize = 10f
             statusText.gravity = android.view.Gravity.CENTER
             statusText.layoutParams = android.widget.LinearLayout.LayoutParams(
                 android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                 android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
             ).apply { topMargin = (4 * density).toInt() }

             layout.addView(icon)
             layout.addView(title)
             layout.addView(statusText)
             card.addView(layout)
             
             binding.layoutWallet.addView(card)
        }
        binding.buttonClearLogbook.setOnClickListener {
            flightViewModel.clearData()
            android.widget.Toast.makeText(context, "Logbook cleared!", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        binding.buttonImportEchronometraz.setOnClickListener {
            findNavController().navigate(R.id.importFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

