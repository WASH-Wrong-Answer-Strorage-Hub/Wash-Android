package com.wash.washandroid.presentation.fragment.note

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentNoteSelectAreaBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class NoteSelectAreaFragment : Fragment() {

    private var _binding: FragmentNoteSelectAreaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteSelectAreaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Glide를 사용하여 이미지 로드
        Glide.with(this)
            .asBitmap()
            .load(R.drawable.image) // 여기에 실제 이미지 경로 또는 URL을 입력하세요
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.ivCaptured.setImageBitmap(resource)
                    binding.btnCrop.setOnClickListener {
                        val overlay = binding.cropOverlayView
                        val croppedBitmap = getCroppedBitmap(resource, overlay.rect)
                        saveBitmapToFile(croppedBitmap, "cropped_image.jpg")
                    }
                }
            })
    }

    private fun getCroppedBitmap(source: Bitmap, rect: Rect): Bitmap {
        return Bitmap.createBitmap(source, rect.left, rect.top, rect.width(), rect.height())
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String) {
        val file = File(requireContext().getExternalFilesDir(null), fileName)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            Log.e("NoteSelectAreaFragment", "Error saving bitmap", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
