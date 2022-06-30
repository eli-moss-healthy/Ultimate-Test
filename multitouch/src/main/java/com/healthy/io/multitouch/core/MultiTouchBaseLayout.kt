package com.healthy.io.multitouch.core

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnLayout
import com.healthy.io.multitouch.api.MultiTouchApi
import com.healthy.io.multitouch.api.MultiTouchGestureListeners
import com.healthy.io.multitouch.getVectorAfterRotation
import com.healthy.io.multitouch.getVectorBeforeRotation

/*
 * Represents both original touch point and the point's representation in zoomed(scaled) and/or translated state.
 */
data class TransformedPoint(val originalPointF: PointF, val transformedPoint: PointF)

/**
 * This class extends the [MultiTouchBaseLayoutParent] class
 * and acts as the interacting layer for the user.
 * It exposes the public API and listeners,
 * and provides extra functionality, such as
 * mapping points on transformed views to corresponding
 * points on the views in their original state.
 *
 * @author Eli Moss
 * @date 2022
 */
abstract class MultiTouchBaseLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : MultiTouchBaseLayoutParent(context, attrs, defStyleAttr), MultiTouchGestureListeners, MultiTouchApi {

    /* api interface */

    // kotlin-compatible listener callbacks
    private var onScaleChanged: ((scale: Float) -> Unit)? = null
    private var onScalingEnded: ((scalingType: ScalingType) -> Unit)? = null
    private var onTranslated: ((dx: Float, dy: Float) -> Unit)? = null
    private var onTap: ((e: MotionEvent, touchPointMappedToOriginal: PointF) -> Unit)? = null
    private var onTouch: ((e: MotionEvent) -> Unit)? = null
    private var onDrag: ((p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean) -> Unit)? = null

    override fun onScaleChanged(func: ((scale: Float) -> Unit)?) {
        onScaleChanged = func
    }

    override fun onScalingEnded(func: ((scalingType: ScalingType) -> Unit)?) {
        onScalingEnded = func
    }

    override fun onTranslated(func: ((dx: Float, dy: Float) -> Unit)?) {
        onTranslated = func
    }

    override fun onTap(func: ((e: MotionEvent, touchPointMappedToOriginal: PointF) -> Unit)?) {
        onTap = func
    }

    override fun onTouch(func: ((e: MotionEvent) -> Unit)?) {
        onTouch = func
    }

    override fun onDrag(func: ((p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean) -> Unit)?) {
        onDrag = func
    }

    /* internal use interface */

    override fun onScaleChanged(scale: Float) {
        onScaleChanged?.invoke(scale)
    }

    override fun onScalingEnded(type: ScalingType) {
        onScalingEnded?.invoke(type)
    }

    override fun onTranslated(dx: Float, dy: Float) {
        onTranslated?.invoke(dx, dy)
    }

    override fun onTap(e: MotionEvent) {
        // touch point on screen
        val pointerIndex: Int = e.findPointerIndex(e.getPointerId(0))
        var pointBeforeMapping = PointF(e.getX(pointerIndex), e.getY(pointerIndex))

        currentView()?.run {
            val state = getContentState() as State
            if (state.rotationDegree != 0f) {
                pointBeforeMapping = getPointBeforeRotation(
                        x = pointBeforeMapping.x,
                        y = pointBeforeMapping.y,
                        rotationDegree = state.rotationDegree)
            }
            // map point to original and notify
            val mappedPoint = getMappedPoint(pointBeforeMapping)
            onTap?.invoke(e, mappedPoint)
        }
    }

    override fun onTouch(e: MotionEvent) {
        onTouch?.invoke(e)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent?, isCurrentlyInMultiTouch: Boolean) {
        currentView()?.let {
            val point1: PointF = getMappedPoint(PointF(e1.x, e1.y))
            val point2: PointF? = if (e2 != null) getMappedPoint(PointF(e2.x, e2.y)) else null

            val p1 = TransformedPoint(PointF(e1.x, e1.y), point1)
            val p2 = point2?.let { TransformedPoint(PointF(e2!!.x, e2.y), it) }

            onDrag?.invoke(p1, p2, isCurrentlyInMultiTouch)
        }
    }

    /* public/helper methods */

    /**
     * Maps the touch point on screen for the zoomed/scaled and/or translated view to the point
     * representing the coordinates on the original view (unscaled & untranslated).
     *
     * NOTE - rotation is not taken into account in this function
     * @see getPointBeforeRotation if rotation is not 0 degrees.
     * @see getPointAfterRotation if rotation is not 0 degrees.
     *
     * @param touchPoint the touch point, representing coordinates  on
     * @return point on original view (unscaled & untranslated), or the original touch point
     * in case the FrameLayout doesn't contain any children
     */
    override fun getMappedPoint(touchPoint: PointF): PointF {
        currentView()?.let {
            val ratioX = width / it.width
            val ratioY = height / it.height

            // map point on frame to point on child view
            val currViewX = touchPoint.x / ratioX
            val currViewY = touchPoint.y / ratioY

            val rectOriginal = RectF(0f, 0f, it.width.toFloat(), it.height.toFloat())

            val pts = floatArrayOf(currViewX, currViewY)
            mappingMatrix.getMappedTouchPoint(pts, it.contentRect()!!, rectOriginal)

            // convert point on child view to the underlying FrameLayout
            val xPointOnFrame = pts[0] * ratioX
            val yPointOnFrame = pts[1] * ratioY

            return PointF(xPointOnFrame, yPointOnFrame)
        }
        return touchPoint
    }
    
    /**
     * Finds the touch point coordinates on a rotated view if we revert/cancel the rotation
     * @param x axis touch point coordinate
     * @param y axis touch point coordinate
     * @param rotationDegree the applied rotation on the view
     * @return Point representing coordinates after applying rotation transformation
     */
    override fun getPointBeforeRotation(x: Float, y: Float,
                               rotationDegree: Float): PointF {
        val dx = currentView()?.run { pivotX + translationX } ?: return PointF()
        val dy = currentView()?.run { pivotY + translationY } ?: return PointF()
        getVectorBeforeRotation(x, y, dx, dy, rotationDegree)
            .let {
                return PointF(it[0][0], -it[0][1]) // android-compatible y axis
            }
    }

    /**
     * Finds the point coordinates after applying a rotation transformation on a view
     * @param x axis touch point coordinate
     * @param y axis touch point coordinate
     * @param rotationDegree the applied rotation on the view
     * @return Point representing coordinates after applying rotation transformation
     */
    override fun getPointAfterRotation(x: Float, y: Float,
                              rotationDegree: Float): PointF {
        val dx = currentView()?.run { pivotX + translationX } ?: return PointF()
        val dy = currentView()?.run { pivotY + translationY } ?: return PointF()
        getVectorAfterRotation(x, y, dx, dy, rotationDegree)
            .let {
                return PointF(it[0][0], -it[1][0])
            }
    }

    /**
     * Returns the child view (represented by [currentTargetViewIndex]) on which all
     * transformations (scale, translation, rotation) are currently being applied
     * @return View indicating the child view on which all transformations are applied on
     */
    override fun currentContentView(): View? = currentView()

    /**
     * Returns the current representation of the transformed
     * view (i.e child view, represented by [currentTargetViewIndex]) after all
     * the zoom/scale, translation and rotation transformations performed on the view
     * @return Rectangle representation of the transformed view
     */
    override fun currentContentRectangle(): RectF? = currentView()?.contentRect()

    /**
     * Sets the child view on which all transformation (scale, translate, rotate) will be applied on
     * @param index indicating the child view to perform all transformation on
     */
    override fun setTargetChildViewIndex(index: Int) {
        currentTargetViewIndex = index
    }

    override fun getTargetChildViewIndex(): Int = currentTargetViewIndex

    /**
     * Returns whether zoom/scaling is applied on the view
     * @return boolean indicating whether any zoom/scaling is currently applied on the view
     */
    override fun isZoomed(): Boolean = currentView()?.scaleX != minScale

    /**
     * Returns whether more than one finger is on screen
     * @return boolean indicating whether more than one finger is on screen currently
     */
    override fun isCurrentlyMultipleFingersDown(): Boolean = isMultipleFingersDown

    /**
     * Resets any zoom/scaling applied on the view to the default minimal scale (minScale).
     * Note that default min scale (minScale) can be changed.
     * @param animate indicating whether the scale change should be animated
     */
    override fun resetScale(animate: Boolean) {
        currentView()?.let {
            if (animate) {
                animateZoomOut()
            } else {
                it.scaleBy(1f/it.scaleX)
                it.translateBy(-it.translationX, -it.translationY)
            }
        }
    }

    /**
     * current scale (zoom) applied
     * Note that it cannot be 0, and it must be in the range between minScale and maxScale
     * @return Float indicating the current scale (zoom) applied
     */
    override var scale: Float = currentView()?.scaleX ?: 1f
        set(newScale) {
            if (newScale == 0f || newScale < minScale || newScale > maxScale) return

            val currScale = currentView()?.scaleX ?: minScale
            doOnLayout {
                currentView()?.run {
                    scaleBy(newScale / currScale)
                    if (scaleX < 1f) centralizeView()
                }
            }
            field = currentView()?.scaleX ?: 1f
        }

    /**
     * current x-axis translation applied
     * NOTE - you cannot set a value to this member
     * @return Float indicating the current X-axis translation applied
     */
    override var dx: Float
        set(_) { }
        get() = currentView()?.translationX ?: 0f

    /**
     * current y-axis translation applied
     * NOTE - you cannot set a value to this member
     * @return Float indicating the current Y-axis translation applied
     */
    override var dy: Float
        set(_) { }
        get() = currentView()?.translationY ?: 0f
}
