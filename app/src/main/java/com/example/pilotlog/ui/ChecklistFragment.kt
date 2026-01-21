package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pilotlog.databinding.FragmentChecklistBinding
import com.google.android.material.tabs.TabLayout
import com.example.pilotlog.R

class ChecklistFragment : Fragment() {

    private var _binding: FragmentChecklistBinding? = null
    private val binding get() = _binding!!

    private lateinit var preFlightItems: List<ChecklistItem>
    private lateinit var beforeTakeoffItems: List<ChecklistItem>
    private lateinit var landingItems: List<ChecklistItem>
    private lateinit var currentList: List<ChecklistItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preFlightItems = listOf(
            ChecklistItem(getString(R.string.check_documents)),
            ChecklistItem(getString(R.string.check_control_lock)),
            ChecklistItem(getString(R.string.check_ignition_off)),
            ChecklistItem(getString(R.string.check_master_on)),
            ChecklistItem(getString(R.string.check_fuel_qty)),
            ChecklistItem(getString(R.string.check_flaps_extend)),
            ChecklistItem(getString(R.string.check_master_off)),
            ChecklistItem(getString(R.string.check_exterior))
        )

        beforeTakeoffItems = listOf(
            ChecklistItem(getString(R.string.check_seats_belts)),
            ChecklistItem(getString(R.string.check_doors)),
            ChecklistItem(getString(R.string.check_controls_free)),
            ChecklistItem(getString(R.string.check_fuel_both)),
            ChecklistItem(getString(R.string.check_trim_takeoff)),
            ChecklistItem(getString(R.string.check_throttle_1700)),
            ChecklistItem(getString(R.string.check_mags)),
            ChecklistItem(getString(R.string.check_carb_heat)),
            ChecklistItem(getString(R.string.check_ammeter)),
            ChecklistItem(getString(R.string.check_suction)),
            ChecklistItem(getString(R.string.check_idle)),
            ChecklistItem(getString(R.string.check_throttle_1000))
        )

        landingItems = listOf(
            ChecklistItem(getString(R.string.check_seats_belts)),
            ChecklistItem(getString(R.string.check_fuel_both)),
            ChecklistItem(getString(R.string.check_mixture_rich)),
            ChecklistItem(getString(R.string.check_carb_heat_on)),
            ChecklistItem(getString(R.string.check_landing_light)),
            ChecklistItem(getString(R.string.check_flaps_required))
        )

        currentList = preFlightItems

        listOf(binding.tabLayout, binding.recyclerViewChecklist, binding.buttonReset).forEachIndexed { index, v ->
            v.alpha = 0f
            v.translationY = 30f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 100L)
                .setDuration(350)
                .start()
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tab_pre_flight)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tab_before_takeoff)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tab_landing)))

        binding.recyclerViewChecklist.layoutManager = LinearLayoutManager(context)
        updateList(preFlightItems)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> updateList(preFlightItems)
                    1 -> updateList(beforeTakeoffItems)
                    2 -> updateList(landingItems)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.buttonReset.setOnClickListener {
            currentList.forEach { it.isChecked = false }
            binding.recyclerViewChecklist.adapter?.notifyDataSetChanged()
        }
    }

    private fun updateList(items: List<ChecklistItem>) {
        currentList = items
        binding.recyclerViewChecklist.adapter = ChecklistAdapter(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
