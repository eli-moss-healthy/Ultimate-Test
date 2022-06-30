package com.healthy.io.multitouch.api

import android.graphics.PointF
import android.graphics.RectF
import android.view.View

/**
 * An interface representing all the
 * api-level extra functions / getters.
 *
 * @author Eli Moss
 * @date 2022
 */

interface MultiTouchApi {
    var dx: Float
    var dy: Float
    var scale: Float
    fun resetScale(animate: Boolean)
    fun isZoomed(): Boolean
    fun setTargetChildViewIndex(index: Int)
    fun getTargetChildViewIndex(): Int
    fun currentContentView(): View?
    fun currentContentRectangle(): RectF?
    fun isCurrentlyMultipleFingersDown(): Boolean
    fun getMappedPoint(touchPoint: PointF): PointF
    fun getPointBeforeRotation(x: Float, y: Float, rotationDegree: Float): PointF
    fun getPointAfterRotation(x: Float, y: Float, rotationDegree: Float): PointF
}
