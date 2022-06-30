package com.healthy.io.multitouch.api

import android.graphics.PointF
import android.view.MotionEvent
import com.healthy.io.multitouch.core.ScalingType
import com.healthy.io.multitouch.core.TransformedPoint

/**
 * An interface representing all the
 * api level listeners/callbacks
 *
 * @author Eli Moss
 * @date 2022
 */

interface MultiTouchGestureListeners {
    fun onScaleChanged(func: ((scale: Float) -> Unit)?)
    fun onScalingEnded(func: ((scalingType: ScalingType) -> Unit)?)
    fun onTranslated(func: ((dx: Float, dy: Float) -> Unit)?)
    fun onTap(func: ((e: MotionEvent, touchPointMappedToOriginal: PointF) -> Unit)?)
    fun onTouch(func: ((e: MotionEvent) -> Unit)?)
    fun onDrag(func: ((p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean) -> Unit)?)
}
