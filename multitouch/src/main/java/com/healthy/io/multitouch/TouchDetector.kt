package com.healthy.io.multitouch

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs

/**
 * Provides an abstraction layer to all
 * possible touch events and gestures.
 * Offers an efficient, easy and minimalistic way to implement
 * only the needed callbacks.
 *
 * @author Eli Moss
 * @date 2022
 */

inline fun View.detectTouches(func: TouchDetector.() -> Unit) {
    TouchDetector(this.context).apply {
        setOnTouchListener(this)
        func()
    }
}

class TouchDetector(context: Context) :
    GestureDetector.OnDoubleTapListener,
    GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener,
    View.OnTouchListener {

    private var gestureDetector: GestureDetector = GestureDetector(context, this)
    private var scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    private var onTouch: ((e: MotionEvent) -> Unit)? = null
    private var onScale: ((scaleFactor: Float, focusX: Float, focusY: Float, spanDelta: Float) -> Unit)? = null
    private var onScaleBegin: ((scaleFactor: Float, focusX: Float, focusY: Float) -> Unit)? = null
    private var onScaleEnd: ((scaleFactor: Float, focusX: Float, focusY: Float) -> Unit)? = null
    private var onTap: ((e: MotionEvent) -> Unit)? = null
    private var onScroll: ((e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float) -> Unit)? = null
    private var onDoubleTap: ((e: MotionEvent, focusX: Float, focusY: Float) -> Unit)? = null
    private var onFling: ((e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit)? = null
    private var onLongPress: ((e: MotionEvent) -> Unit)? = null

    ///

    fun onTouch(func: ((e: MotionEvent) -> Unit)?) {
        onTouch = func
    }

    fun onScale(func: ((scaleFactor: Float, focusX: Float, focusY: Float, spanDelta: Float) -> Unit)?) {
        onScale = func
    }

    fun onScaleBegin(func: ((scaleFactor: Float, focusX: Float, focusY: Float) -> Unit)?) {
        onScaleBegin = func
    }

    fun onScaleEnd(func: ((scaleFactor: Float, focusX: Float, focusY: Float) -> Unit)?) {
        onScaleEnd = func
    }

    fun onScroll(func: ((e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float) -> Unit)?) {
        onScroll = func
    }

    fun onTap(func: ((e: MotionEvent) -> Unit)?) {
        onTap = func
    }

    fun onDoubleTap(func: ((e: MotionEvent, focusX: Float, focusY: Float) -> Unit)?) {
        onDoubleTap = func
    }

    fun onFling(func: ((e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit)?) {
        onFling = func
    }

    fun onLongPress(func: ((e: MotionEvent) -> Unit)?) {
        onLongPress = func
    }

    /// Touch Listener

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        v.performClick()
        onTouch?.invoke(event)
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        return true
    }

    /// Scale Detector Listener

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (!detector.isInProgress) return false
        onScale?.invoke(detector.scaleFactor, detector.focusX, detector.focusY,
            abs(detector.currentSpan - detector.previousSpan))
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        onScaleBegin?.invoke(detector.scaleFactor, detector.focusX, detector.focusY)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        onScaleEnd?.invoke(detector.scaleFactor, detector.focusX, detector.focusY)
    }

    /// Gesture - OnDoubleTap Listener

    override fun onDoubleTap(e: MotionEvent): Boolean {
        onDoubleTap?.invoke(e, scaleDetector.focusX, scaleDetector.focusY)
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        onTap?.invoke(e)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean { return true }

    /// Gesture - OnGesture Listener

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        onScroll?.invoke(e1, e2, distanceX, distanceY)
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        onFling?.invoke(e1, e2, velocityX, velocityY)
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        onLongPress?.invoke(e)
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean { return true }

    override fun onDown(e: MotionEvent): Boolean { return true }

}
