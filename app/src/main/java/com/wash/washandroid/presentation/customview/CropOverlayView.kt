package com.wash.washandroid.presentation.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.wash.washandroid.R
import kotlin.math.absoluteValue


class CropOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
    }

    private val cornerPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.sub4) // 모서리 색상 변경
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }


    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#80000000") // 검정색 50% 투명도
    }

    private val rect = RectF(100f, 200f, 500f, 600f)
    private var movingCorner: Corner? = null
    private val cornerRadius = 20f // 둥근 모서리 반지름

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cornerRadius = 20f // 모든 모서리를 20f로 수정

        // 선택되지 않은 부분 그리기
        canvas.drawRect(0f, 0f, width.toFloat(), rect.top, backgroundPaint)
        canvas.drawRect(0f, rect.bottom, width.toFloat(), height.toFloat(), backgroundPaint)
        canvas.drawRect(0f, rect.top, rect.left, rect.bottom, backgroundPaint)
        canvas.drawRect(rect.right, rect.top, width.toFloat(), rect.bottom, backgroundPaint)

        // 선택된 영역 반투명 흰색으로 채우기
        val selectedAreaPaint = Paint().apply {
            color = Color.parseColor("#66FFFFFF") // 흰색 40% 투명도
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, selectedAreaPaint)

        // 선택된 영역 테두리 그리기
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        drawCorners(canvas)
    }






    private fun drawCorners(canvas: Canvas) {
        val cornerRadius = 20f
        val cornerLength = 30f

        // 상단 왼쪽
        canvas.drawLine(rect.left, rect.top, rect.left + cornerLength, rect.top, cornerPaint)
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top + cornerLength, cornerPaint)
        // 상단 오른쪽
        canvas.drawLine(rect.right, rect.top, rect.right - cornerLength, rect.top, cornerPaint)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + cornerLength, cornerPaint)
        // 하단 왼쪽
        canvas.drawLine(rect.left, rect.bottom, rect.left + cornerLength, rect.bottom, cornerPaint)
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - cornerLength, cornerPaint)
        // 하단 오른쪽
        canvas.drawLine(rect.right, rect.bottom, rect.right - cornerLength, rect.bottom, cornerPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - cornerLength, cornerPaint)

        // 각 모서리에 둥근 부분 그리기
//        canvas.drawArc(RectF(rect.left - cornerRadius, rect.top - cornerRadius, rect.left + cornerRadius, rect.top + cornerRadius), 180f, 90f, false, cornerPaint)
//        canvas.drawArc(RectF(rect.right - cornerRadius, rect.top - cornerRadius, rect.right + cornerRadius, rect.top + cornerRadius), -90f, 90f, false, cornerPaint)
//        canvas.drawArc(RectF(rect.left - cornerRadius, rect.bottom - cornerRadius, rect.left + cornerRadius, rect.bottom + cornerRadius), 90f, 90f, false, cornerPaint)
//        canvas.drawArc(RectF(rect.right - cornerRadius, rect.bottom - cornerRadius, rect.right + cornerRadius, rect.bottom + cornerRadius), 0f, 90f, false, cornerPaint)
    }







    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                movingCorner = getCorner(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                movingCorner?.let {
                    updateRect(event.x, event.y, it)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                movingCorner = null
            }
        }
        return true
    }

    private fun getCorner(x: Float, y: Float): Corner? {
        return when {
            isNearCorner(x, y, rect.left, rect.top) -> Corner.TOP_LEFT
            isNearCorner(x, y, rect.right, rect.top) -> Corner.TOP_RIGHT
            isNearCorner(x, y, rect.left, rect.bottom) -> Corner.BOTTOM_LEFT
            isNearCorner(x, y, rect.right, rect.bottom) -> Corner.BOTTOM_RIGHT
            else -> null
        }
    }

    private fun isNearCorner(x: Float, y: Float, cornerX: Float, cornerY: Float): Boolean {
        val threshold = 40
        return (x - cornerX).absoluteValue < threshold && (y - cornerY).absoluteValue < threshold
    }

    private fun updateRect(x: Float, y: Float, corner: Corner) {
        when (corner) {
            Corner.TOP_LEFT -> {
                rect.left = x
                rect.top = y
            }
            Corner.TOP_RIGHT -> {
                rect.right = x
                rect.top = y
            }
            Corner.BOTTOM_LEFT -> {
                rect.left = x
                rect.bottom = y
            }
            Corner.BOTTOM_RIGHT -> {
                rect.right = x
                rect.bottom = y
            }
        }
    }

    private enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}

