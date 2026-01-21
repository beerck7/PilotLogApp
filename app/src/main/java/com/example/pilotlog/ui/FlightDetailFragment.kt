package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pilotlog.R
import com.example.pilotlog.data.Aircraft
import com.example.pilotlog.data.Flight
import com.example.pilotlog.databinding.FragmentFlightDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.pilotlog.hardware.LocationHelper
import com.example.pilotlog.data.Airport
import java.io.File

class FlightDetailFragment : Fragment() {

    private var _binding: FragmentFlightDetailBinding? = null
    private val binding get() = _binding!!

    private val flightViewModel: FlightViewModel by viewModels()
    private var selectedAircraft: Aircraft? = null
    private lateinit var locationHelper: LocationHelper
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLocationAndAutofill()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePhoto()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            binding.cardPreview.visibility = View.VISIBLE
            binding.imagePreview.setImageURI(photoUri)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyUriToFile(it)
            if (file != null) {
                currentPhotoPath = file.absolutePath
                binding.cardPreview.visibility = View.VISIBLE
                binding.imagePreview.setImageURI(Uri.fromFile(file))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlightDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.editDate.setText(dateFormat.format(Date()))

        val flightId = arguments?.getInt("flightId", -1) ?: -1
        if (flightId != -1) {
            flightViewModel.getFlightById(flightId).observe(viewLifecycleOwner) { flight ->
                flight?.let {
                    binding.editDate.setText(dateFormat.format(it.date))
                    binding.editDeparture.setText(it.departureCode)
                    binding.editArrival.setText(it.arrivalCode)
                    binding.editDuration.setText(it.durationMinutes.toString())
                    binding.editRemarks.setText(it.remarks)
                    
                    if (it.photoPath != null) {
                        currentPhotoPath = it.photoPath
                        binding.cardPreview.visibility = View.VISIBLE
                        binding.imagePreview.setImageURI(Uri.fromFile(File(it.photoPath)))
                    }
                    
                    flightViewModel.allAircrafts.observe(viewLifecycleOwner) { aircrafts ->
                         val index = aircrafts.indexOfFirst { a -> a.id == it.aircraftId }
                         if (index >= 0) {
                             binding.spinnerAircraft.setSelection(index)
                             selectedAircraft = aircrafts[index]
                         }
                    }
                }
            }
        }
        
        flightViewModel.allAirports.observe(viewLifecycleOwner) {
        }

        val launchMethods = listOf(
            getString(R.string.launch_self),
            getString(R.string.launch_winch),
            getString(R.string.launch_aerotow),
            getString(R.string.launch_bungee)
        )
        val launchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, launchMethods)
        binding.editLaunchMethod.setAdapter(launchAdapter)

        flightViewModel.allAircrafts.observe(viewLifecycleOwner) { aircrafts ->
            if (aircrafts.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    aircrafts.map { "${it.registration} (${it.model})" }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerAircraft.adapter = adapter
                
                binding.spinnerAircraft.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedAircraft = aircrafts[position]
                        if (selectedAircraft?.type == "GLD") {
                            binding.layoutLaunchMethod.visibility = View.VISIBLE
                            binding.layoutReleaseHeight.visibility = View.VISIBLE
                        } else {
                            binding.layoutLaunchMethod.visibility = View.GONE
                            binding.layoutReleaseHeight.visibility = View.GONE
                        }
                    }
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
                
                val currentFlightId = arguments?.getInt("flightId", -1) ?: -1
                if (currentFlightId != -1) {
                     flightViewModel.getFlightById(currentFlightId).observe(viewLifecycleOwner) { flight ->
                         flight?.let { f ->
                             val index = aircrafts.indexOfFirst { a -> a.id == f.aircraftId }
                             if (index >= 0) {
                                 binding.spinnerAircraft.setSelection(index)
                             }
                         }
                     }
                }
            }
        }

        locationHelper = LocationHelper(requireContext())

        binding.buttonAutofill.setOnClickListener {
            checkLocationPermission()
        }

        binding.buttonAddPhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }

        binding.buttonPickGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.buttonSave.setOnClickListener {
            saveFlight()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndAutofill()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun fetchLocationAndAutofill() {
        locationHelper.getCurrentLocation { location ->
            if (location != null) {
                val airports = flightViewModel.allAirports.value
                if (!airports.isNullOrEmpty()) {
                    val nearest = findNearestAirport(location, airports)
                    nearest?.let {
                        binding.editDeparture.setText(it.code)
                        Toast.makeText(context, "Nearest airport: ${it.code}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No airports in database", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Could not get location. Check if GPS is enabled.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun findNearestAirport(location: Location, airports: List<Airport>): Airport? {
        return airports.minByOrNull { airport ->
            val results = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, airport.latitude, airport.longitude, results)
            results[0]
        }
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto() {
        try {
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val photoFile = File.createTempFile(
                "flight_${System.currentTimeMillis()}", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            
            currentPhotoPath = photoFile.absolutePath
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating image file: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun copyUriToFile(uri: Uri): File? {
        return try {
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(storageDir, "flight_${System.currentTimeMillis()}.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun saveFlight() {
        val dateStr = binding.editDate.text.toString()
        val dep = binding.editDeparture.text.toString()
        val arr = binding.editArrival.text.toString()
        val durationStr = binding.editDuration.text.toString()
        val remarks = binding.editRemarks.text.toString()
        val launchMethod = if (binding.layoutLaunchMethod.visibility == View.VISIBLE) binding.editLaunchMethod.text.toString() else null
        val releaseHeightStr = if (binding.layoutReleaseHeight.visibility == View.VISIBLE) binding.editReleaseHeight.text.toString() else null

        if (dep.isBlank() || arr.isBlank() || durationStr.isBlank()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedAircraft == null) {
             Toast.makeText(context, "Please select an aircraft", Toast.LENGTH_SHORT).show()
             return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        val currentFlightId = arguments?.getInt("flightId", -1) ?: -1
        
        val flight = Flight(
            id = if (currentFlightId != -1) currentFlightId else 0,
            date = date,
            aircraftId = selectedAircraft!!.id,
            departureCode = dep,
            arrivalCode = arr,
            durationMinutes = durationStr.toIntOrNull() ?: 0,
            remarks = remarks,
            photoPath = currentPhotoPath,
            launchMethod = launchMethod,
            releaseHeight = releaseHeightStr?.toIntOrNull()
        )

        if (currentFlightId != -1) {
            flightViewModel.update(flight)
        } else {
            flightViewModel.insert(flight)
        }
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
