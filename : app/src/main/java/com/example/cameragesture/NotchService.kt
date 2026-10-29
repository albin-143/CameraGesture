package com.example.cameragesture

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.abs

class NotchService : AccessibilityService() {

    private var overlay: View? = null

    // Size of the touch area at the top-center (camera notch). Adjust if needed.
    private val widthDp = 120
    private val heightDp = 56

    override fun onServiceConnected() {
        super.onServiceConnected()
        addOverlay()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        overlay?.let { try { getSystemService(WindowManager::class.java).removeView(it) } catch (_: Exception) {} }
        overlay = null
        super.onDestroy()
    }

    private fun fire(gesture: String) {
        overlay?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        Actions.run(this, Actions.get(this, gesture))
    }

    private fun addOverlay() {
        val d = resources.displayMetrics.density
        val minDist = 30 * d

        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean { fire("single_tap"); return true }
            override fun onDoubleTap(e: MotionEvent): Boolean { fire("double_tap"); return true }
            override fun onLongPress(e: MotionEvent) { fire("long_press") }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.rawX - e1.rawX
                val dy = e2.rawY - e1.rawY
                when {
                    abs(dx) > abs(dy) && abs(dx) > minDist -> fire(if (dx < 0) "swipe_left" else "swipe_right")
                    dy > minDist -> fire("swipe_down")
                    else -> return false
                }
                return true
            }
        })

        val v = View(this)
        v.setOnTouchListener { _, ev -> detector.onTouchEvent(ev); true }

        val lp = WindowManager.LayoutParams(
            (widthDp * d).toInt(), (heightDp * d).toInt(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        getSystemService(WindowManager::class.java).addView(v, lp)
        overlay = v
    }
}
