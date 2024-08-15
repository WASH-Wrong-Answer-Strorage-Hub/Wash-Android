package com.wash.washandroid.presentation.fragment.note

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentNoteSelectAreaBinding
import com.wash.washandroid.presentation.base.MainActivity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.widget.ImageView
import com.wash.washandroid.presentation.customview.CropOverlayView
import java.io.File
import java.io.FileOutputStream

class NoteSelectAreaFragment : Fragment() {

    private var _binding: FragmentNoteSelectAreaBinding? = null
    private val binding get() = _binding!!
    private var originalPaddingTop: Int = 0
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteSelectAreaBinding.inflate(inflater, container, false)
        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // 현재 패딩을 저장하고 0으로 설정
        originalPaddingTop = (activity as MainActivity).findViewById<View>(R.id.container).paddingTop
        (activity as MainActivity).setContainerPadding(0)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        // bundle로 이미지 uri 수신
        val imageUri = arguments?.getString("imgUri")
        if (imageUri == "0") {
            Log.d("fraglog", "imgUri : 이미지 불러오기 실패")
        } else {
            Log.d("fraglog", "imgUri : 이미지 불러오기 성공")
            loadCapturedImage(Uri.parse(imageUri))
        }

        // imguri2
        val imageUri2 = arguments?.getString("imgUri2")
        if (imageUri2 == "0") {
            Log.d("fraglog", "imgUri2 : 이미지 불러오기 실패")
        } else {
            Log.d("fraglog", "imgUri2 : 이미지 불러오기 성공")
            loadCapturedImage(Uri.parse((imageUri2)))
        }

        // 영역 추가
        binding.btnAddArea.setOnClickListener {
            binding.cropOverlayView.addNewRect()
        }

        // 확인 버튼
        binding.btnCrop.setOnClickListener {
            val croppedUris = binding.cropOverlayView.cropAllRectangles(binding.ivCaptured)
            if (croppedUris.isNotEmpty()) {
                val bundle = bundleOf("croppedUris" to croppedUris.map { it.toString() }.toTypedArray())
                navController.navigate(R.id.action_navigation_note_select_area_to_navigation_problem_add, bundle)
            }
        }

        binding.btnBack.setOnClickListener {
            navController = Navigation.findNavController(view)
            navController.navigateUp()
        }
    }

    private fun loadCapturedImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(binding.ivCaptured)
    }

    /**
     * crop all rectangles
     */
    fun CropOverlayView.cropAllRectangles(imageView: ImageView): List<Uri> {
        val croppedUris = mutableListOf<Uri>()

        // Get the bitmap from the ImageView
        val drawable = imageView.drawable
        val bitmap = (drawable as BitmapDrawable).bitmap

        // Get image and view dimensions
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        // 이미지 뷰의 이미지가 어떻게 스케일링되었는지를 확인
        val matrixValues = FloatArray(9)
        imageView.imageMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]

        // Loop through all rectangles and crop the bitmap
        rects.forEachIndexed { index, rect ->
            // Adjust the rectangle coordinates to match the bitmap scale
            var left = (rect.left * scaleX).toInt().coerceAtLeast(0)
            var top = (rect.top * scaleY).toInt().coerceAtLeast(0)
            var right = (rect.right * scaleX).toInt().coerceAtMost(imageWidth)
            var bottom = (rect.bottom * scaleY).toInt().coerceAtMost(imageHeight)

            // 좌표가 잘못된 경우 보정
            if (left > right) {
                val temp = left
                left = right
                right = temp
            }
            if (top > bottom) {
                val temp = top
                top = bottom
                bottom = temp
            }

            // 폭과 높이가 0 이하가 아닌지 확인
            val width = right - left
            val height = bottom - top

            Log.d("CropOverlayView", "Adjusted Rect $index: left=$left, top=$top, right=$right, bottom=$bottom")
            Log.d("CropOverlayView", "Bitmap size: width=$imageWidth, height=$imageHeight")

            if (width > 0 && height > 0 && bottom <= imageHeight && right <= imageWidth) {
                try {
                    val croppedBitmap = Bitmap.createBitmap(
                        bitmap,
                        left,
                        top,
                        width,
                        height
                    )

                    // Save the cropped bitmap to a file and get its URI
                    val uri = saveCroppedBitmap(croppedBitmap)
                    uri?.let {
                        croppedUris.add(it)
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("CropOverlayView", "Crop area out of bounds: ${e.message}")
                    Toast.makeText(context, "잘못된 크롭 영역입니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 잘라내려는 범위가 원본 사진의 크기를 초과할 경우
                Log.e("CropOverlayView", "Rect $index is out of bounds after scaling!")
                Toast.makeText(context, "문제 영역의 범위를 조정해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        return croppedUris
    }

    /**
     * save cropped bitmaps
     */
    private fun saveCroppedBitmap(croppedBitmap: Bitmap): Uri? {
        val filename = "cropped_${System.currentTimeMillis()}.png"
        val directory = File(Environment.getExternalStorageDirectory().toString() + "/CroppedImages")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, filename)

        return try {
            val out = FileOutputStream(file)
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 원래 패딩을 복원
        (activity as MainActivity).setContainerPadding(originalPaddingTop)

        _binding = null
    }
}
