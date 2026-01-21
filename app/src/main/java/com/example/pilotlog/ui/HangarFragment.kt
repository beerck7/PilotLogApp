package com.example.pilotlog.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pilotlog.R
import com.example.pilotlog.data.Aircraft
import com.example.pilotlog.databinding.FragmentHangarBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class HangarFragment : Fragment() {

    private var _binding: FragmentHangarBinding? = null
    private val binding get() = _binding!!
    private val flightViewModel: FlightViewModel by viewModels()

    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null
    private var dialogImagePreview: ImageView? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePhoto()
        } else {
            android.widget.Toast.makeText(context, "Camera permission denied", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            dialogImagePreview?.setImageURI(null)
            dialogImagePreview?.setImageURI(photoUri)
            dialogImagePreview?.imageTintList = null
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyUriToFile(it)
            if (file != null) {
                currentPhotoPath = file.absolutePath
                dialogImagePreview?.setImageURI(Uri.fromFile(file))
                dialogImagePreview?.imageTintList = null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHangarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AircraftAdapter(
            onItemClicked = { aircraft ->
            },
            onItemLongClicked = { aircraft ->
                showDeleteAircraftDialog(aircraft)
            }
        )
        binding.recyclerViewHangar.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewHangar.adapter = adapter

        flightViewModel.allAircrafts.observe(viewLifecycleOwner) { aircrafts ->
            adapter.submitList(aircrafts)
        }

        binding.fabAddAircraft.setOnClickListener {
            showAddAircraftDialog()
        }
    }

    private fun showAddAircraftDialog() {
        currentPhotoPath = null
        
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_aircraft, null)
        val inputReg = view.findViewById<EditText>(R.id.input_registration)
        val inputModel = view.findViewById<EditText>(R.id.input_model)
        val inputType = view.findViewById<EditText>(R.id.input_type)
        dialogImagePreview = view.findViewById(R.id.image_preview)
        
        val btnTakePhoto = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_take_photo)
        val btnPickGallery = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_pick_gallery)

        btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }

        btnPickGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(view)
            .setPositiveButton(R.string.action_add) { _, _ ->
                val reg = inputReg.text.toString().uppercase()
                val model = inputModel.text.toString()
                val type = inputType.text.toString()

                if (reg.isNotEmpty()) {
                    val newAircraft = Aircraft(
                        registration = reg,
                        model = model,
                        type = type,
                        imagePath = currentPhotoPath
                    )
                    flightViewModel.insertAircraft(newAircraft)
                    android.widget.Toast.makeText(context, R.string.toast_aircraft_added, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
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
                "aircraft_${System.currentTimeMillis()}",
                ".jpg",
                storageDir
            )

            currentPhotoPath = photoFile.absolutePath
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error creating image file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToFile(uri: Uri): File? {
        return try {
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(storageDir, "aircraft_${System.currentTimeMillis()}.jpg")
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

    private fun showDeleteAircraftDialog(aircraft: Aircraft) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_aircraft_message, aircraft.registration))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                flightViewModel.deleteAircraft(aircraft)
                android.widget.Toast.makeText(context, R.string.toast_aircraft_deleted, android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogImagePreview = null
        _binding = null
    }
}

