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
import androidx.fragment.app.FragmentActivity
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.note.DeleteRectangleBottomSheet
import kotlin.math.absoluteValue


class CropOverlayViewExample(context: Context, attrs: AttributeSet) : View(context, attrs) {

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
//        color = Color.parseColor("#80000000") // 검정색 50% 투명도
        color = Color.parseColor("#00000000") // 투명
    }

    // 사각형 리스트
    val rects = mutableListOf<RectF>()
    private var movingCorner: Pair<RectF, Corner>? = null
    private val cornerRadius = 20f // 둥근 모서리 반지름

    init {
        // 초기 사각형 추가
        rects.add(RectF(100f, 200f, 500f, 600f))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cornerRadius = 20f // 모든 모서리를 20f로 수정

        rects.forEach { rect ->
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
            drawCorners(canvas, rect)
        }
    }

    private fun drawCorners(canvas: Canvas, rect: RectF) {
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
    }

    private var offsetX = 0f
    private var offsetY = 0f
    private var isLongPressDetected = false
    private val initialOffsetX = 300f
    private val initialOffsetY = 300f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isLongPressDetected = false
                movingCorner = getCorner(event.x, event.y)

                // 모서리가 아닌 부분을 길게 눌렀을 때만 바텀 시트 표시
                if (movingCorner == null) {
                    handler.postDelayed({
                        val selectedRect = getSelectedRect(event.x, event.y)
                        if (selectedRect != null) {
                            isLongPressDetected = true
                            showBottomSheet(selectedRect)
                        }
                    }, 500L) // 500ms 길게 누르기 감지
                }

                // 사각형이 선택되지 않았을 때 드래그를 위한 CENTER 선택 로직
                if (movingCorner == null) {
                    val selectedRect = getSelectedRect(event.x, event.y)
                    if (selectedRect != null) {
                        movingCorner = Pair(selectedRect, Corner.CENTER)

                        // 터치 시작 시점과 사각형의 차이를 저장하고, 초기 이동 추가
                        offsetX = event.x - selectedRect.left
                        offsetY = event.y - selectedRect.top

                        // 사각형을 처음 터치할 때 30px 왼쪽, 30px 위로 이동
                        selectedRect.offset(-initialOffsetX, -initialOffsetY)
                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isLongPressDetected) {
                    movingCorner?.let { (rect, corner) ->
                        if (corner == Corner.CENTER) {
                            // 사각형 전체를 이동
                            val newLeft = (event.x - offsetX).coerceIn(0f, width - rect.width())
                            val newTop = (event.y - offsetY).coerceIn(0f, height - rect.height())
                            val newRight = newLeft + rect.width()
                            val newBottom = newTop + rect.height()

                            // 경계 검사 및 겹침 방지
                            if (isWithinBounds(newLeft, newTop, newRight, newBottom) &&
                                !isOverlappingWithOtherRects(newLeft, newTop, newRight, newBottom, rect)
                            ) {
                                rect.left = newLeft
                                rect.top = newTop
                                rect.right = newRight
                                rect.bottom = newBottom
                                invalidate()
                            }
                        } else {
                            // 모서리 이동 (기존 로직)
                            updateRect(event.x, event.y, rect, corner)
                            invalidate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                movingCorner = null
                handler.removeCallbacksAndMessages(null) // 길게 누르기 취소
                isLongPressDetected = false
            }
        }
        return true
    }

    // 모서리 감지 로직
    private fun getCorner(x: Float, y: Float): Pair<RectF, Corner>? {
        rects.forEach { rect ->
            val corner = when {
                isNearCorner(x, y, rect.left, rect.top) -> Corner.TOP_LEFT
                isNearCorner(x, y, rect.right, rect.top) -> Corner.TOP_RIGHT
                isNearCorner(x, y, rect.left, rect.bottom) -> Corner.BOTTOM_LEFT
                isNearCorner(x, y, rect.right, rect.bottom) -> Corner.BOTTOM_RIGHT
                rect.contains(x, y) -> Corner.CENTER // 사각형 내부를 터치한 경우 CENTER로 간주
                else -> null
            }
            if (corner != null) return Pair(rect, corner)
        }
        return null
    }

    private fun getSelectedRect(x: Float, y: Float): RectF? {
        return rects.firstOrNull { rect ->
            x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
        }
    }

    private fun showBottomSheet(selectedRect: RectF) {
        val bottomSheet = DeleteRectangleBottomSheet {
            // 사각형 삭제 로직
            rects.remove(selectedRect)
            invalidate()  // 뷰 업데이트
        }
        bottomSheet.show((context as FragmentActivity).supportFragmentManager, bottomSheet.tag)
    }

    private fun isNearCorner(x: Float, y: Float, cornerX: Float, cornerY: Float): Boolean {
        val threshold = 40
        return (x - cornerX).absoluteValue < threshold && (y - cornerY).absoluteValue < threshold
    }

    // 사각형 크기 조정 로직
    private fun updateRect(x: Float, y: Float, rect: RectF, corner: Corner) {
        val minWidth = 100f  // 사각형의 최소 너비
        val minHeight = 100f // 사각형의 최소 높이

        when (corner) {
            Corner.TOP_LEFT -> {
                // 좌상단 모서리 이동 시
                val newLeft = x.coerceAtMost(rect.right - minWidth)
                val newTop = y.coerceAtMost(rect.bottom - minHeight)
                rect.left = newLeft
                rect.top = newTop
            }

            Corner.TOP_RIGHT -> {
                // 우상단 모서리 이동 시
                val newRight = x.coerceAtLeast(rect.left + minWidth)
                val newTop = y.coerceAtMost(rect.bottom - minHeight)
                rect.right = newRight
                rect.top = newTop
            }

            Corner.BOTTOM_LEFT -> {
                // 좌하단 모서리 이동 시
                val newLeft = x.coerceAtMost(rect.right - minWidth)
                val newBottom = y.coerceAtLeast(rect.top + minHeight)
                rect.left = newLeft
                rect.bottom = newBottom
            }

            Corner.BOTTOM_RIGHT -> {
                // 우하단 모서리 이동 시
                val newRight = x.coerceAtLeast(rect.left + minWidth)
                val newBottom = y.coerceAtLeast(rect.top + minHeight)
                rect.right = newRight
                rect.bottom = newBottom
            }
            Corner.CENTER -> {

            }
        }
    }

    fun addNewRect() {
//        rects.add(RectF(100f, 200f, 400f, 600f)) // 새 사각형 위치 설정
        val newRect = findNonOverlappingRect()
        rects.add(newRect)
        invalidate() // 화면 갱신
    }

    private fun findNonOverlappingRect(): RectF {
        val padding = 20f // 사각형들 사이의 최소 간격
        var left: Float
        var top: Float
        var right: Float
        var bottom: Float

        var isOverlapping: Boolean

        do {
            // 랜덤하게 새 사각형의 위치 설정
            left = (0..(width - 300)).random().toFloat() // 새 사각형의 left 좌표
            top = (0..(height - 400)).random().toFloat() // 새 사각형의 top 좌표
            right = left + 300 // 새 사각형의 너비
            bottom = top + 400 // 새 사각형의 높이

            val newRect = RectF(left, top, right, bottom)

            // 기존 사각형들과 겹치는지 확인
            isOverlapping = rects.any { existingRect ->
                RectF.intersects(existingRect, newRect)
            }
        } while (isOverlapping) // 겹치는 경우 새로운 위치를 찾음

        return RectF(left, top, right, bottom)
    }


    private var touchStartX = 0f
    private var touchStartY = 0f

    private enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }

    // 다른 사각형과의 겹침을 방지하는 함수
    private fun isOverlappingWithOtherRects(
        left: Float, top: Float, right: Float, bottom: Float, currentRect: RectF
    ): Boolean {
        val newRect = RectF(left, top, right, bottom)
        return rects.any { rect ->
            rect != currentRect && RectF.intersects(rect, newRect)
        }
    }

    // 사각형이 뷰의 경계를 벗어나지 않도록 제한하는 함수
    private fun isWithinBounds(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return left >= 0 && top >= 0 && right <= width && bottom <= height
    }
}


