package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pilotlog.databinding.FragmentGforceBinding
import com.example.pilotlog.hardware.GForceMonitor

class GForceFragment : Fragment() {

    private var _binding: FragmentGforceBinding? = null
    private val binding get() = _binding!!
    private lateinit var gForceMonitor: GForceMonitor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGforceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gForceMonitor = GForceMonitor(requireContext())

        val viewsToAnimate = listOf(
            binding.labelCurrentG,
            binding.textCurrentG,
            binding.labelMaxG,
            binding.textMaxG,
            binding.buttonReset
        )
        viewsToAnimate.forEachIndexed { index, v ->
            v.alpha = 0f
            v.scaleX = 0.8f
            v.scaleY = 0.8f
            v.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(index * 80L)
                .setDuration(400)
                .start()
        }

        gForceMonitor.currentGForce.observe(viewLifecycleOwner) { g ->
            binding.textCurrentG.text = String.format("%.1f", g)
        }

        gForceMonitor.maxGForce.observe(viewLifecycleOwner) { max ->
            binding.textMaxG.text = String.format("%.1f", max)
        }

        binding.buttonReset.setOnClickListener {
            gForceMonitor.resetMax()
        }
    }

    override fun onResume() {
        super.onResume()
        gForceMonitor.startListening()
    }

    override fun onPause() {
        super.onPause()
        gForceMonitor.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
