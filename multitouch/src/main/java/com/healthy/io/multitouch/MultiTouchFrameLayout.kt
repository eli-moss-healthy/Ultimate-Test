package com.healthy.io.multitouch

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.healthy.io.multitouch.api.MultiTouchApi
import com.healthy.io.multitouch.api.MultiTouchGestureListeners
import com.healthy.io.multitouch.api.Optionals
import com.healthy.io.multitouch.core.*


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
class MultiTouchFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : MultiTouchBaseLayout(context, attrs, defStyleAttr), Optionals, MultiTouchApi, MultiTouchGestureListeners {

    /// listeners - touch, gestures and multi touch transformations ///

    override fun onScaleChanged(func: ((scale: Float) -> Unit)?) = super.onScaleChanged(func)

    override fun onScalingEnded(func: ((scalingType: ScalingType) -> Unit)?)  = super.onScalingEnded(func)

    override fun onTranslated(func: ((dx: Float, dy: Float) -> Unit)?) = super.onTranslated(func)

    override fun onTap(func: ((e: MotionEvent, touchPointMappedToOriginal: PointF) -> Unit)?) = super.onTap(func)

    override fun onTouch(func: ((e: MotionEvent) -> Unit)?) = super.onTouch(func)

    override fun onDrag(func: ((p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean) -> Unit)?) = super.onDrag(func)

    /// api ///

    /* controls whether scaling is enabled */
    override var isZoomEnabled: Boolean = super.isZoomEnabled

    /* controls whether double tap gesture is enabled */
    override var isDoubleTapEnabled: Boolean = super.isDoubleTapEnabled

    /* controls the minimal scale value */
    override var minScale: Float = super.minScale

    /* controls the maximal scale value */
    override var maxScale: Float = super.maxScale

    /** Bounds Effect - the effect when trying to scale-out more than the minimum scale ("bounce back animation") **/

    /* controls whether the bounds effect is enabled */
    override var isBoundsLimitEffectEnabled: Boolean = super.isBoundsLimitEffectEnabled

    /* controls whether the bounds effect is enabled */
    override var boundsLimitEffectScaleFactor: Float = super.boundsLimitEffectScaleFactor

    /* controls whether dragging with one finger is enabled */
    override var isOneFingerDraggingEnabled: Boolean = super.isOneFingerDraggingEnabled

    /* control whether dragging with multiple fingers is enabled */
    override var isMultiFingerDraggingEnabled: Boolean = super.isMultiFingerDraggingEnabled

    /* controls whether rotation of the view is enabled */
    override var isRotationEnabled: Boolean = super.isRotationEnabled

    /**
     * current x-axis translation applied
     * NOTE - you cannot set a value to this member
     * @return Float indicating the current X-axis translation applied
     */
    override var dx: Float = super.dx

    /**
     * current y-axis translation applied
     * NOTE - you cannot set a value to this member
     * @return Float indicating the current Y-axis translation applied
     */
    override var dy: Float = super.dy

    /**
     * current scale (zoom) applied
     * Note that it cannot be 0, and it must be in the range between minScale and maxScale
     * @return Float indicating the current scale (zoom) applied
     */
    override var scale: Float = super.scale

    /**
     * Resets any zoom/scaling applied on the view to the default minimal scale (minScale).
     * Note that default min scale (minScale) can be changed.
     * @param animate indicating whether the scale change should be animated
     */
    override fun resetScale(animate: Boolean) = super.resetScale(animate)

    /**
     * Returns whether zoom/scaling is applied on the view
     * @return boolean indicating whether any zoom/scaling is currently applied on the view
     */
    override fun isZoomed(): Boolean = super.isZoomed()

    /**
     * Sets the child view on which all transformation (scale, translate, rotate) will be applied on
     * @param index indicating the child view to perform all transformation on
     */
    override fun setTargetChildViewIndex(index: Int) = super.setTargetChildViewIndex(index)

    /**
     * Returns index of child view on which all transformation (scale, translate, rotate) will be applied on
     * @return boolean indicating the child view that all transformations are applied on
     */
    override fun getTargetChildViewIndex(): Int = super.getTargetChildViewIndex()

    /**
     * Returns the child view (represented by [currentTargetViewIndex]) on which all
     * transformations (scale, translation, rotation) are currently being applied
     * @return View indicating the child view on which all transformations are applied on
     */
    override fun currentContentView(): View? = super.currentContentView()

    /**
     * Returns the current representation of the transformed
     * view (i.e child view, represented by [currentTargetViewIndex]) after all
     * the zoom/scale, translation and rotation transformations performed on the view
     * @return Rectangle representation of the transformed view
     */
    override fun currentContentRectangle(): RectF? = super.currentContentRectangle()

    /**
     * Returns whether more than one finger is on screen
     * @return boolean indicating whether more than one finger is on screen currently
     */
    override fun isCurrentlyMultipleFingersDown(): Boolean = super.isCurrentlyMultipleFingersDown()

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
    override fun getMappedPoint(touchPoint: PointF): PointF = super.getMappedPoint(touchPoint)

    /**
     * Finds the touch point coordinates on a rotated view if we revert/cancel the rotation
     * @param x axis touch point coordinate
     * @param y axis touch point coordinate
     * @param rotationDegree the applied rotation on the view
     * @return Point representing coordinates after applying rotation transformation
     */
    override fun getPointBeforeRotation(x: Float, y: Float, rotationDegree: Float): PointF = super.getPointBeforeRotation(x, y , rotationDegree)

    /**
     * Finds the point coordinates after applying a rotation transformation on a view
     * @param x axis touch point coordinate
     * @param y axis touch point coordinate
     * @param rotationDegree the applied rotation on the view
     * @return Point representing coordinates after applying rotation transformation
     */
    override fun getPointAfterRotation(x: Float, y: Float, rotationDegree: Float): PointF = super.getPointAfterRotation(x, y, rotationDegree)

}