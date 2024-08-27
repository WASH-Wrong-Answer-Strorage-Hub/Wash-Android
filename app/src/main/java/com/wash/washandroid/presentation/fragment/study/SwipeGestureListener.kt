package com.wash.washandroid.presentation.fragment.study

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class SwipeGestureListener(
    private val context: Context,
    private val onSwipeRight: () -> Unit,
    private val onSwipeLeft: () -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
    ): Boolean {
        if (e1 == null || e2 == null) return super.onFling(e1, e2, velocityX, velocityY)

        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipeLeft()
                } else {
                    onSwipeRight()
                }
                return true
            }
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }
}