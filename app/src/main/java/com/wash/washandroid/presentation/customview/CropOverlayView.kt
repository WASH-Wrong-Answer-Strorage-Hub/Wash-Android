package com.wash.washandroid.presentation.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.wash.washandroid.R
import kotlin.math.absoluteValue

class CropOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val rect = Rect(100, 200, 500, 600)
    private var movingCorner: Corner? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(rect, paint)
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

    private fun isNearCorner(x: Float, y: Float, cornerX: Int, cornerY: Int): Boolean {
        val threshold = 40
        return (x - cornerX).absoluteValue < threshold && (y - cornerY).absoluteValue < threshold
    }

    private fun updateRect(x: Float, y: Float, corner: Corner) {
        when (corner) {
            Corner.TOP_LEFT -> {
                rect.left = x.toInt()
                rect.top = y.toInt()
            }
            Corner.TOP_RIGHT -> {
                rect.right = x.toInt()
                rect.top = y.toInt()
            }
            Corner.BOTTOM_LEFT -> {
                rect.left = x.toInt()
                rect.bottom = y.toInt()
            }
            Corner.BOTTOM_RIGHT -> {
                rect.right = x.toInt()
                rect.bottom = y.toInt()
            }
        }
    }

    private enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}
