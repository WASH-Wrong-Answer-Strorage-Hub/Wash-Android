package com.wash.washandroid.presentation.fragment.home

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeClickQuizBinding

class HomeClickQuizFragment : Fragment() {

    private var _binding: FragmentHomeClickQuizBinding? = null
    private val binding get() = _binding!!

    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeClickQuizBinding.inflate(inflater, container, false)
        val view = binding.root

        // back_btn 클릭 이벤트 설정
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeClickQuizFragment_to_homeDetailFragment)
        }

        // ImageView 클릭 리스너 설정
        binding.addQuizImg.setOnClickListener { openImageSelector() }
        binding.paragraphImage.setOnClickListener { openImageSelector() }
        binding.solutionImage.setOnClickListener { openImageSelector() }

        return view
    }

    private fun openImageSelector() { //사진 접근하기(창)
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePhoto()
                1 -> chooseFromGallery()
                2 -> {}
            }
        }
        builder.show()
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // Handle the image taken from the camera
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    // Do something with the image
                }
                GALLERY_REQUEST_CODE -> {
                    val imageUri = data.data
                    // Handle the image selected from the gallery
                    // Do something with the image URI
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
